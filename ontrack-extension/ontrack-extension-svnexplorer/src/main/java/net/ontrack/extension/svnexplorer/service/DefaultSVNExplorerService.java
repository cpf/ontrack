package net.ontrack.extension.svnexplorer.service;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ontrack.core.model.*;
import net.ontrack.extension.api.property.PropertiesService;
import net.ontrack.extension.jira.JIRAService;
import net.ontrack.extension.jira.service.JIRAIssueNotFoundException;
import net.ontrack.extension.jira.service.model.JIRAIssue;
import net.ontrack.extension.jira.service.model.JIRAStatus;
import net.ontrack.extension.svn.SubversionExtension;
import net.ontrack.extension.svn.SubversionPathPropertyExtension;
import net.ontrack.extension.svn.service.SubversionService;
import net.ontrack.extension.svn.service.model.*;
import net.ontrack.extension.svn.support.SVNLogEntryCollector;
import net.ontrack.extension.svn.support.SVNUtils;
import net.ontrack.extension.svnexplorer.ProjectRootPathPropertyExtension;
import net.ontrack.extension.svnexplorer.SVNExplorerExtension;
import net.ontrack.extension.svnexplorer.SensibleFilesPropertyExtension;
import net.ontrack.extension.svnexplorer.model.*;
import net.ontrack.service.ManagementService;
import net.ontrack.tx.Transaction;
import net.ontrack.tx.TransactionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class DefaultSVNExplorerService implements SVNExplorerService {

    private final Logger logger = LoggerFactory.getLogger(SVNExplorerService.class);
    private final ManagementService managementService;
    private final PropertiesService propertiesService;
    private final SubversionService subversionService;
    private final JIRAService jiraService;
    private final TransactionService transactionService;

    @Autowired
    public DefaultSVNExplorerService(ManagementService managementService, PropertiesService propertiesService, SubversionService subversionService, JIRAService jiraService, TransactionService transactionService) {
        this.managementService = managementService;
        this.propertiesService = propertiesService;
        this.subversionService = subversionService;
        this.jiraService = jiraService;
        this.transactionService = transactionService;
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeLogSummary getChangeLogSummary(Locale locale, int branchId, int from, int to) {
        try (Transaction ignored = transactionService.start()) {
            // Gets the branch
            BranchSummary branch = managementService.getBranch(branchId);
            // Gets the build information
            SVNBuild buildFrom = getBuild(locale, from);
            SVNBuild buildTo = getBuild(locale, to);
            // OK
            return new ChangeLogSummary(
                    UUID.randomUUID().toString(),
                    branch,
                    buildFrom,
                    buildTo
            );
        }
    }

    protected Collection<ChangeLogReference> getChangeLogReferences(ChangeLogSummary summary) {// Function that extracts the path from a SVN location
        Function<SVNReference, String> pathFn = new Function<SVNReference, String>() {
            @Override
            public String apply(SVNReference reference) {
                return reference.getPath();
            }
        };

        // List of paths on both histories
        List<String> pathsFrom = Lists.transform(summary.getBuildFrom().getHistory().getReferences(), pathFn);
        List<String> pathsTo = Lists.transform(summary.getBuildTo().getHistory().getReferences(), pathFn);

        // Index in the upper history
        Pair<Integer, Integer> commonAncestor = null;
        for (int i = 0; i < pathsFrom.size(); i++) {
            String upperUrl = pathsFrom.get(i);
            // Index of this URL in the lower history
            int index = pathsTo.indexOf(upperUrl);
            // Found!
            if (index >= 0) {
                commonAncestor = Pair.of(i, index);
                break;
            }
        }

        // A common ancestor must be found
        if (commonAncestor == null) {
            throw new NoCommonAncestorException();
        }
        int fromAncestorIndex = commonAncestor.getLeft();
        int toAncestorIndex = commonAncestor.getRight();

        // Reference
        String referencePath = pathsFrom.get(fromAncestorIndex);
        long referenceStartRevision = summary.getBuildFrom().getHistory().getReferences().get(fromAncestorIndex).getRevision();
        long referenceEndRevision = summary.getBuildTo().getHistory().getReferences().get(toAncestorIndex).getRevision();

        // Ordering of revisions (we must have start > end)
        if (referenceStartRevision < referenceEndRevision) {
            long t = referenceStartRevision;
            referenceStartRevision = referenceEndRevision;
            referenceEndRevision = t;
        }

        // Reference
        return Collections.singletonList(new ChangeLogReference(referencePath, referenceStartRevision, referenceEndRevision));
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeLogRevisions getChangeLogRevisions(ChangeLogSummary summary) {

        // Reference
        Collection<ChangeLogReference> references = getChangeLogReferences(summary);

        // No difference?
        if (references.isEmpty()) {
            return ChangeLogRevisions.none();
        }

        // SVN transaction
        try (Transaction ignored = transactionService.start()) {
            List<ChangeLogRevision> revisions = new ArrayList<>();
            for (ChangeLogReference reference : references) {
                if (!reference.isNone()) {
                    // List of log entries
                    SVNLogEntryCollector logEntryCollector = new SVNLogEntryCollector();
                    // SVN change log
                    subversionService.log(
                            SVNUtils.toURL(subversionService.getURL(reference.getPath())),
                            SVNRevision.create(reference.getStart()),
                            SVNRevision.create(reference.getStart()),
                            SVNRevision.create(reference.getEnd() + 1),
                            true, // Stops on copy
                            false, // No path discovering (yet)
                            0L, // no limit
                            true, // Includes merged revisions
                            logEntryCollector
                    );
                    // Loops through all SVN log entries, taking the merged revisions into account
                    int level = 0;
                    for (SVNLogEntry svnEntry : logEntryCollector.getEntries()) {
                        long revision = svnEntry.getRevision();
                        if (SVNRevision.isValidRevisionNumber(revision)) {
                            // Conversion
                            ChangeLogRevision entry = createChangeLogRevision(level, svnEntry);
                            // Adds it to the list
                            revisions.add(entry);
                            // New parent?
                            if (svnEntry.hasChildren()) {
                                level++;
                            }
                        } else {
                            level--;
                        }
                    }
                }
            }
            // OK
            return new ChangeLogRevisions(revisions);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeLogIssues getChangeLogIssues(ChangeLogSummary summary, ChangeLogRevisions revisions) {
        // In a SVN/JIRA transaction
        try (Transaction ignored = transactionService.start()) {
            // Index of issues, sorted by keys
            Map<String, ChangeLogIssue> issues = new TreeMap<>();
            // For all revisions in this revision log
            for (ChangeLogRevision changeLogRevision : revisions.getList()) {
                long revision = changeLogRevision.getRevision();
                collectIssuesForRevision(issues, revision);
            }
            // List of issues
            List<ChangeLogIssue> issuesList = new ArrayList<>(issues.values());
            // TODO Validation
            // validationService.validate(changeLog, issuesList);
            // OK
            return new ChangeLogIssues(issuesList);

        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeLogFiles getChangeLogFiles(ChangeLogSummary summary, ChangeLogRevisions revisions) {
        // In a SVN/JIRA transaction
        try (Transaction ignored = transactionService.start()) {
            // Index of files, indexed by path
            Map<String, ChangeLogFile> files = new TreeMap<>();
            // For each revision
            for (ChangeLogRevision changeLogRevision : revisions.getList()) {
                // Takes into account only the unmerged revisions
                if (changeLogRevision.getLevel() == 0) {
                    long revision = changeLogRevision.getRevision();
                    collectFilesForRevision(files, revision);
                }
            }
            // List of files
            List<ChangeLogFile> filesList = new ArrayList<>(files.values());
            // OK
            return new ChangeLogFiles(filesList);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChangeLogInfo getChangeLogInfo(ChangeLogSummary summary, ChangeLogIssues issues, ChangeLogFiles files) {
        // List of patterns for sensible files
        String sensibleFilesPattern = propertiesService.getPropertyValue(Entity.BRANCH, summary.getBranch().getId(), SVNExplorerExtension.EXTENSION, SensibleFilesPropertyExtension.NAME);
        // Filtering function
        Predicate<ChangeLogFile> sensibleFilePredicate = getSensibleFilePredicateFn(sensibleFilesPattern);
        // JIRA issue statuses
        Map<String, ChangeLogInfoStatus> statuses = new TreeMap<>();
        for (ChangeLogIssue changeLogIssue : issues.getList()) {
            JIRAStatus status = changeLogIssue.getIssue().getStatus();
            String statusName = status.getName();
            ChangeLogInfoStatus infoStatus = statuses.get(statusName);
            if (infoStatus != null) {
                infoStatus.incr();
            } else {
                infoStatus = new ChangeLogInfoStatus(status);
                statuses.put(statusName, infoStatus);
            }
        }
        // Sensible files
        List<ChangeLogFile> sensibleFiles = Lists.newArrayList(
                Iterables.filter(
                        files.getList(),
                        sensibleFilePredicate
                )
        );
        // OK
        return new ChangeLogInfo(
                Lists.newArrayList(statuses.values()),
                sensibleFiles
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RevisionInfo getRevisionInfo(Locale locale, long revision) {
        // Gets information about the revision
        SVNRevisionInfo basicInfo = subversionService.getRevisionInfo(revision);
        ChangeLogRevision changeLogRevision = createChangeLogRevision(
                0,
                revision,
                basicInfo.getMessage(),
                basicInfo.getAuthor(),
                basicInfo.getDateTime()
        );
        // Looks for branches with the corresponding path
        Collection<Integer> branchIds = propertiesService.findEntityByPropertyValue(Entity.BRANCH, SubversionExtension.EXTENSION, SubversionPathPropertyExtension.PATH, basicInfo.getPath());
        // For each branch, looks for the earliest build that contains this revision
        Collection<RevisionInfoBuild> buildSummaries = new ArrayList<>();
        List<RevisionPromotions> revisionPromotionsPerBranch = new ArrayList<>();
        for (int branchId : branchIds) {
            // Gets the build SVN path pattern for the branch
            String buildPathPattern = propertiesService.getPropertyValue(Entity.BRANCH, branchId, SubversionExtension.EXTENSION, SubversionExtension.SUBVERSION_BUILD_PATH);
            if (StringUtils.isNotBlank(buildPathPattern)) {
                // Location for this revision
                SVNLocation initialLocation = new SVNLocation(basicInfo.getPath(), basicInfo.getRevision());
                // Stack of eligible locations
                Stack<SVNLocation> locations = new Stack<>();
                locations.push(initialLocation);
                // Earliest build
                Integer buildId = null;
                // Recursive search of eligible locations
                while (!locations.isEmpty()) {
                    // Gets the top element
                    SVNLocation location = locations.pop();
                    // Is it a build?
                    buildId = getEarliestBuild(branchId, location, buildPathPattern);
                    if (buildId != null) {
                        // Build found - not looking further
                        locations.clear();
                    }
                    // Not a build
                    else {
                        // List of copies from this location
                        Collection<SVNLocation> copies = subversionService.getCopiesFrom(location, SVNLocationSortMode.FROM_NEWEST);
                        // Adds them to the stack, from the most ancient to the newest
                        locations.addAll(copies);
                    }
                }
                // Build found
                if (buildId != null) {
                    // Gets the build information
                    BuildSummary buildSummary = managementService.getBuild(buildId);
                    // Gets the promotion levels & validation stamps
                    List<BuildPromotionLevel> promotionLevels = managementService.getBuildPromotionLevels(locale, buildId);
                    List<BuildValidationStamp> buildValidationStamps = managementService.getBuildValidationStamps(locale, buildId);
                    // Adds to the list
                    buildSummaries.add(
                            new RevisionInfoBuild(
                                    buildSummary,
                                    promotionLevels,
                                    buildValidationStamps
                            ));
                    // Gets the promotions for this branch
                    List<Promotion> promotions = getPromotionsForBranch(locale, branchId, buildId);
                    if (promotions != null && !promotions.isEmpty()) {
                        revisionPromotionsPerBranch.add(new RevisionPromotions(
                                managementService.getBranch(branchId),
                                promotions
                        ));
                    }
                }
            }
        }
        // OK
        return new RevisionInfo(
                changeLogRevision,
                buildSummaries,
                revisionPromotionsPerBranch
        );
    }

    @Override
    @Transactional(readOnly = true)
    public IssueInfo getIssueInfo(Locale locale, String key) {
        // Gets the details about the issue
        JIRAIssue issue = jiraService.getIssue(key);
        // Gets the list of revisions & their basic info (order from latest to oldest)
        List<ChangeLogRevision> revisions = Lists.transform(
                subversionService.getRevisionsForIssueKey(key),
                new Function<Long, ChangeLogRevision>() {
                    @Override
                    public ChangeLogRevision apply(Long revision) {
                        SVNRevisionInfo basicInfo = subversionService.getRevisionInfo(revision);
                        return createChangeLogRevision(
                                0,
                                revision,
                                basicInfo.getMessage(),
                                basicInfo.getAuthor(),
                                basicInfo.getDateTime()
                        );
                    }
                });
        // Gets the last revision (which is the first in the list)
        ChangeLogRevision firstRevision = revisions.get(0);
        RevisionInfo revisionInfo = getRevisionInfo(locale, firstRevision.getRevision());
        // OK
        return new IssueInfo(
                issue,
                subversionService.formatRevisionTime(issue.getUpdateTime()),
                revisionInfo,
                revisions
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BranchHistory getBranchHistory(int projectId, Locale locale) {
        try (Transaction ignored = transactionService.start()) {
            logger.debug("[branch-history] Start");
            // Gets the project details
            ProjectSummary project = managementService.getProject(projectId);
            // Gets the root path for this project
            String rootPath = propertiesService.getPropertyValue(Entity.PROJECT, projectId, SVNExplorerExtension.EXTENSION, ProjectRootPathPropertyExtension.NAME);
            if (StringUtils.isBlank(rootPath)) {
                throw new ProjectHasRootPathException(project.getName());
            }
            // Gets the latest revision on this root path
            long rootRevision = subversionService.getRepositoryRevision(SVNUtils.toURL(subversionService.getURL(rootPath)));
            SVNLocation rootLocation = new SVNLocation(rootPath, rootRevision);
            // Tree of locations
            SVNTreeNode rootNode = new SVNTreeNode(rootLocation);
            // Stack of locations
            Stack<SVNTreeNode> stack = new Stack<>();
            stack.add(rootNode);
            // Trimming the stack
            while (!stack.isEmpty()) {
                // Gets the top
                SVNTreeNode current = stack.pop();
                // Gets all the copies from this location
                Collection<SVNLocation> copies = subversionService.getCopiesFrom(
                        current.getLocation().withRevision(1),
                        SVNLocationSortMode.FROM_NEWEST);
                // No copy?
                if (copies.isEmpty()) {
                    // Trunk or branch
                    if (subversionService.isTrunkOrBranch(current.getLocation().getPath())) {
                        // Attaches to the parent
                        current.attachToParent();
                    }
                }
                // At least one copy
                else {
                    // Attach to the parent
                    current.attachToParent();
                    // For each copy
                    for (SVNLocation copy : copies) {
                        // Adds to the stack
                        stack.push(new SVNTreeNode(current, copy));
                    }
                }
            }

            // Pruning the closed branches
            rootNode.visitBottomUp(new SVNTreeNodeVisitor() {
                @Override
                public void visit(SVNTreeNode node) {
                    if (subversionService.isTrunkOrBranch(node.getLocation().getPath())) {
                        node.setClosed(subversionService.isClosed(node.getLocation().getPath()));
                    }
                    // Loops over children
                    node.filterChildren(new Predicate<SVNTreeNode>() {

                        @Override
                        public boolean apply(SVNTreeNode child) {
                            return !child.isClosed();
                        }
                    });
                }
            });

            // Prunes the tag-only locations
            rootNode.visitBottomUp(new SVNTreeNodeVisitor() {

                @Override
                public void visit(SVNTreeNode node) {
                    // Is this node a tag?
                    node.setTag(subversionService.isTag(node.getLocation().getPath()));
                    // Loops over children
                    node.filterChildren(new Predicate<SVNTreeNode>() {

                        @Override
                        public boolean apply(SVNTreeNode child) {
                            boolean allTags = child.all(new Predicate<SVNTreeNode>() {

                                @Override
                                public boolean apply(SVNTreeNode n) {
                                    return n.isTag();
                                }
                            });
                            return !allTags;
                        }
                    });
                }
            });

            // Collects history
            BranchHistoryLine root = collectHistory(locale, rootNode);

            // OK
            logger.debug("[branch-history] End");
            return new BranchHistory(
                    project,
                    root
            );
        }
    }

    private BranchHistoryLine collectHistory(Locale locale, SVNTreeNode node) {
        // Line itself
        BranchHistoryLine line = createBranchHistoryLine(locale, node.getLocation());
        // Collects lines
        for (SVNTreeNode childNode : node.getChildren()) {
            line.addLine(collectHistory(locale, childNode));
        }
        // OK
        return line;
    }

    private BranchHistoryLine createBranchHistoryLine(final Locale locale, SVNLocation location) {
        // Core
        BranchHistoryLine line = new BranchHistoryLine(
                subversionService.getReference(location),
                subversionService.isTag(location.getPath())
        );
        // Branch?
        Collection<Integer> branchIds = propertiesService.findEntityByPropertyValue(Entity.BRANCH, SubversionExtension.EXTENSION, SubversionPathPropertyExtension.PATH, location.getPath());
        if (branchIds.size() > 1) {
            throw new IllegalStateException("At most one branch should be eligible - configuration problem at branch level?");
        } else if (branchIds.size() == 1) {
            final int branchId = branchIds.iterator().next();
            BranchSummary branch = managementService.getBranch(branchId);
            line = line.withBranch(branch);
            // Latest build?
            BuildSummary latestBuild = managementService.getLastBuild(branchId);
            if (latestBuild != null) {
                line = line.withLatestBuild(latestBuild);
            }
            // Gets the list of promotions
            List<PromotionLevelSummary> promotionLevelList = managementService.getPromotionLevelList(branchId);
            promotionLevelList = new ArrayList<>(promotionLevelList);
            Collections.reverse(promotionLevelList);
            line = line.withPromotions(
                    Lists.transform(
                            promotionLevelList,
                            new Function<PromotionLevelSummary, Promotion>() {
                                @Override
                                public Promotion apply(PromotionLevelSummary promotionLevel) {
                                    return managementService.findLastPromotion(locale, promotionLevel.getId());
                                }
                            }
                    )
            );
        }
        // OK
        return line;
    }

    private List<Promotion> getPromotionsForBranch(final Locale locale, int branchId, final int buildId) {
        // List of promotions for this branch
        List<PromotionLevelSummary> promotionLevelList = managementService.getPromotionLevelList(branchId);
        return Lists.transform(
                promotionLevelList,
                new Function<PromotionLevelSummary, Promotion>() {
                    @Override
                    public Promotion apply(PromotionLevelSummary promotionLevel) {
                        return managementService.getEarliestPromotionForBuild(locale, buildId, promotionLevel.getId());
                    }
                }
        );
    }

    /**
     * Gets the earliest build that contains the given <code>location</code> on this branch.
     *
     * @param branchId    ID of the branch
     * @param location    Location for the revision to search the build for
     * @param pathPattern Path pattern for the build on this branch
     * @return ID of the earliest build that contains this revision, or <code>null</code> if none is found
     */
    private Integer getEarliestBuild(int branchId, SVNLocation location, String pathPattern) {
        if (SVNExplorerPathUtils.followsBuildPattern(location, pathPattern)) {
            // Gets the build name
            String buildName = SVNExplorerPathUtils.getBuildName(location, pathPattern);
            /**
             * If the build is defined by path@revision, the earliest build is the one
             * that follows this revision.
             */
            if (SVNExplorerPathUtils.isPathRevision(pathPattern)) {
                return managementService.findBuildAfterUsingNumericForm(branchId, buildName);
            }
            /**
             * In any other case (tag or tag prefix), the build must be looked exactly
             */
            else {
                return managementService.findBuildByName(branchId, buildName);
            }
        } else {
            return null;
        }
    }

    private Predicate<ChangeLogFile> getSensibleFilePredicateFn(String sensibleFilesPatterns) {
        // Empty?
        if (StringUtils.isBlank(sensibleFilesPatterns)) {
            return Predicates.alwaysFalse();
        }
        // List of patterns
        final List<Pattern> patterns = new ArrayList<>();
        // Split on lines
        String[] lines = StringUtils.split(sensibleFilesPatterns, "\n\r");
        // Creates the patterns
        for (String line : lines) {
            if (StringUtils.isNotBlank(line)) {
                Pattern pattern = Pattern.compile(StringUtils.trim(line));
                patterns.add(pattern);
            }
        }
        // OK
        return new Predicate<ChangeLogFile>() {
            @Override
            public boolean apply(ChangeLogFile file) {
                String path = file.getPath();
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(path).matches()) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    private void collectFilesForRevision(Map<String, ChangeLogFile> files, long revision) {
        SVNRevisionPaths revisionPaths = subversionService.getRevisionPaths(revision);
        for (SVNRevisionPath revisionPath : revisionPaths.getPaths()) {
            String path = revisionPath.getPath();
            // Existing file entry?
            ChangeLogFile changeLogFile = files.get(path);
            if (changeLogFile == null) {
                changeLogFile = new ChangeLogFile(path, subversionService.getBrowsingURL(path));
                files.put(path, changeLogFile);
            }
            // Adds the revision and the type
            ChangeLogFileChange change = new ChangeLogFileChange(
                    revisionPaths.getInfo(),
                    revisionPath.getChangeType(),
                    subversionService.getFileChangeBrowsingURL(path, revisionPaths.getInfo().getRevision())
            );
            changeLogFile.addChange(change);
        }
    }

    private void collectIssuesForRevision(Map<String, ChangeLogIssue> issues, long revision) {
        // Gets all issues attached to this revision
        List<String> issueKeys = subversionService.getIssueKeysForRevision(revision);
        // For each issue
        for (String issueKey : issueKeys) {
            // Gets its details if not indexed yet
            ChangeLogIssue changeLogIssue = issues.get(issueKey);
            if (changeLogIssue == null) {
                changeLogIssue = getChangeLogIssue(issueKey);
            }
            // Existing issue?
            if (changeLogIssue != null) {
                // Attaches the revision to this issue
                SVNRevisionInfo issueRevision = subversionService.getRevisionInfo(revision);
                changeLogIssue = changeLogIssue.addRevision(issueRevision);
                // Puts back into the cache
                issues.put(issueKey, changeLogIssue);
            }
        }
    }

    private ChangeLogIssue getChangeLogIssue(String issueKey) {
        // Gets the details about the JIRA issue
        try {
            JIRAIssue issue = jiraService.getIssue(issueKey);
            if (issue == null || StringUtils.isBlank(issue.getKey())) {
                return null;
            }
            // Creates the issue details for the change logs
            return new ChangeLogIssue(issue, subversionService.formatRevisionTime(issue.getUpdateTime()));
        } catch (JIRAIssueNotFoundException ex) {
            return null;
        }
    }

    private ChangeLogRevision createChangeLogRevision(int level, SVNLogEntry svnEntry) {
        return createChangeLogRevision(
                level,
                svnEntry.getRevision(),
                svnEntry.getMessage(),
                svnEntry.getAuthor(),
                new DateTime(svnEntry.getDate()));

    }

    private ChangeLogRevision createChangeLogRevision(int level, long revision, String message, String author, DateTime revisionDate) {
        // Formatted message
        String formattedMessage = jiraService.insertIssueUrlsInMessage(message);
        // Revision URL
        String revisionUrl = subversionService.getRevisionBrowsingURL(revision);
        // OK
        return new ChangeLogRevision(
                level,
                revision,
                author,
                subversionService.formatRevisionTime(revisionDate),
                message,
                revisionUrl,
                formattedMessage);
    }

    private SVNBuild getBuild(Locale locale, int buildId) {
        // Gets the build basic information
        BuildSummary build = managementService.getBuild(buildId);
        // Gets the build SVN tag
        String buildPath = getBuildPath(build.getBranch().getId(), build.getName());
        // Gets the history for this tag using the SubversionService
        SVNHistory history = subversionService.getHistory(buildPath);
        // OK
        return new SVNBuild(
                build,
                history,
                managementService.getBuildValidationStamps(locale, build.getId()),
                managementService.getBuildPromotionLevels(locale, build.getId())
        );
    }

    private String getBuildPath(int branchId, String buildName) {
        // Gets the SVN build path property for the branch
        String buildPathPattern = propertiesService.getPropertyValue(Entity.BRANCH, branchId, SubversionExtension.EXTENSION, SubversionExtension.SUBVERSION_BUILD_PATH);
        if (buildPathPattern == null) {
            throw new SVNExplorerNoBuildPathDefinedForBranchException();
        }
        // Replaces the * by the build name
        return buildPathPattern.replace("*", buildName);
    }
}
