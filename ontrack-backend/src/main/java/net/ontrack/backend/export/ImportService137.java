package net.ontrack.backend.export;

import com.google.common.collect.Lists;
import net.ontrack.backend.dao.*;
import net.ontrack.core.model.ProjectData;
import net.ontrack.core.model.ProjectSummary;
import net.ontrack.service.ManagementService;
import org.codehaus.jackson.JsonNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Qualifier("1.37")
public class ImportService137 implements ImportService {

    private final ManagementService managementService;
    protected final ProjectDao projectDao;
    protected final BranchDao branchDao;
    protected final PromotionLevelDao promotionLevelDao;
    protected final ValidationStampDao validationStampDao;
    protected final BuildDao buildDao;
    protected final PromotedRunDao promotedRunDao;
    protected final ValidationRunDao validationRunDao;
    protected final ValidationRunStatusDao validationRunStatusDao;
    protected final EventDao eventDao;
    protected final CommentDao commentDao;
    protected final PropertyDao propertyDao;
    protected final BuildCleanupDao buildCleanupDao;

    @Autowired
    public ImportService137(ManagementService managementService, ProjectDao projectDao, BranchDao branchDao, PromotionLevelDao promotionLevelDao, ValidationStampDao validationStampDao, BuildDao buildDao, PromotedRunDao promotedRunDao, ValidationRunDao validationRunDao, ValidationRunStatusDao validationRunStatusDao, EventDao eventDao, CommentDao commentDao, PropertyDao propertyDao, BuildCleanupDao buildCleanupDao) {
        this.managementService = managementService;
        this.projectDao = projectDao;
        this.branchDao = branchDao;
        this.promotionLevelDao = promotionLevelDao;
        this.validationStampDao = validationStampDao;
        this.buildDao = buildDao;
        this.promotedRunDao = promotedRunDao;
        this.validationRunDao = validationRunDao;
        this.validationRunStatusDao = validationRunStatusDao;
        this.eventDao = eventDao;
        this.commentDao = commentDao;
        this.propertyDao = propertyDao;
        this.buildCleanupDao = buildCleanupDao;
    }

    @Override
    @Transactional
    public ProjectSummary doImport(ProjectData projectData) {
        // Context
        ImportContext context = new ImportContext();
        // Project
        int projectId = createProject(projectData, context);
        // OK
        return managementService.getProject(projectId);
    }

    protected int createProject(ProjectData projectData, ImportContext context) {
        // TODO Split for reuseability?
        // Reads & creates the project
        int id = projectData.getData().path("project").path("id").asInt();
        String name = projectData.getData().path("project").path("name").asText();
        String description = projectData.getData().path("project").path("description").asText();
        int projectId = projectDao.createProject(name, description);
        context.forProject(id, projectId);
        // Branches
        JsonNode branchesNode = projectData.getData().path("branches");
        for (JsonNode branchNode : branchesNode) {
            int oldBranchId = branchNode.path("id").asInt();
            String branchName = branchNode.path("name").asText();
            String branchDescription = branchNode.path("description").asText();
            int newBranchId = branchDao.createBranch(projectId, branchName, branchDescription);
            context.forBranch(oldBranchId, newBranchId);
        }
        // Promotion levels
        JsonNode promotionLevelsNode = projectData.getData().path("promotionLevels");
        List<JsonNode> promotionLevelsNodeList = Lists.newArrayList(promotionLevelsNode);
        Collections.sort(promotionLevelsNodeList, new Comparator<JsonNode>() {
            @Override
            public int compare(JsonNode o1, JsonNode o2) {
                int level1 = o1.path("levelNb").asInt();
                int level2 = o2.path("levelNb").asInt();
                return level1 - level2;
            }
        });
        for (JsonNode promotionLevelNode : promotionLevelsNodeList) {
            int oldPromotionLevelId = promotionLevelNode.path("id").asInt();
            int oldBranchId = promotionLevelNode.path("branch").asInt();
            String promotionLevelName = promotionLevelNode.path("name").asText();
            String promotionLevelDescription = promotionLevelNode.path("description").asText();
            int newBranchId = context.forBranch(oldBranchId);
            int newPromotionLevelId = promotionLevelDao.createPromotionLevel(newBranchId, promotionLevelName, promotionLevelDescription);
            context.forPromotionLevel(oldPromotionLevelId, newPromotionLevelId);
        }
        // Validation stamps
        JsonNode validationStampNodes = projectData.getData().path("validationStamps");
        List<JsonNode> validationStampNodeList = Lists.newArrayList(validationStampNodes);
        Collections.sort(validationStampNodeList, new Comparator<JsonNode>() {
            @Override
            public int compare(JsonNode o1, JsonNode o2) {
                int level1 = o1.path("orderNb").asInt();
                int level2 = o2.path("orderNb").asInt();
                return level1 - level2;
            }
        });
        for (JsonNode validationStampNode : validationStampNodeList) {
            int oldValidationStampId = validationStampNode.path("id").asInt();
            int oldBranchId = validationStampNode.path("branch").asInt();
            String validationStampName = validationStampNode.path("name").asText();
            String validationStampDescription = validationStampNode.path("description").asText();
            int newBranchId = context.forBranch(oldBranchId);
            int newValidationStampId = validationStampDao.createValidationStamp(newBranchId, validationStampName, validationStampDescription);
            context.forValidationStamp(oldValidationStampId, newValidationStampId);
        }
        // Builds
        JsonNode buildNodes = projectData.getData().path("builds");
        List<JsonNode> buildNodeList = Lists.newArrayList(buildNodes);
        Collections.sort(buildNodeList, new Comparator<JsonNode>() {
            @Override
            public int compare(JsonNode o1, JsonNode o2) {
                int id1 = o1.path("id").asInt();
                int id2 = o2.path("id").asInt();
                return id1 - id2;
            }
        });
        for (JsonNode buildNode : buildNodeList) {
            int oldBuildId = buildNode.path("id").asInt();
            int oldBranchId = buildNode.path("branch").asInt();
            String buildName = buildNode.path("name").asText();
            String buildDescription = buildNode.path("description").asText();
            int newBranchId = context.forBranch(oldBranchId);
            int newBuildId = buildDao.createBuild(newBranchId, buildName, buildDescription);
            context.forBuild(oldBuildId, newBuildId);
        }
        // Promoted runs
        JsonNode promotedRunNodes = projectData.getData().path("promotedRuns");
        for (JsonNode promotedRunNode : promotedRunNodes) {
            int oldBuildId = promotedRunNode.path("build").asInt();
            int oldPromotionLevelId = promotedRunNode.path("promotionLevel").asInt();
            String promotedRunDescription = promotedRunNode.path("description").asText();
            String promotedRunAuthor = promotedRunNode.path("author").asText();
            DateTime promotedRunCreation = new DateTime(promotedRunNode.path("creation").asLong(), DateTimeZone.UTC);
            int newBuildId = context.forBuild(oldBuildId);
            int newPromotionLevelId = context.forPromotionLevel(oldPromotionLevelId);
            promotedRunDao.createPromotedRun(newBuildId, newPromotionLevelId, promotedRunAuthor, null, promotedRunCreation, promotedRunDescription);
        }
        // TODO Validation runs
        // TODO Validation run statuses
        // TODO Comments
        // TODO Properties
        // TODO Build clean-up policy
        // Project ID
        return projectId;
    }

}
