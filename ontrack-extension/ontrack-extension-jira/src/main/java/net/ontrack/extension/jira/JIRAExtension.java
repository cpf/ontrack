package net.ontrack.extension.jira;

import net.ontrack.extension.api.configuration.ConfigurationExtension;
import net.ontrack.extension.api.property.PropertyExtensionDescriptor;
import net.ontrack.extension.api.support.ExtensionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class JIRAExtension extends ExtensionAdapter {

    public static final String EXTENSION = "jira";
    public static final Pattern ISSUE_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9]*\\-[0-9]+");
    private final JIRAIssuePropertyExtension issuePropertyExtension;
    private final JIRAConfigurationExtension configurationExtension;

    @Autowired
    public JIRAExtension(JIRAIssuePropertyExtension issuePropertyExtension, JIRAConfigurationExtension configurationExtension) {
        super(EXTENSION);
        this.issuePropertyExtension = issuePropertyExtension;
        this.configurationExtension = configurationExtension;
    }

    @Override
    public List<? extends ConfigurationExtension> getConfigurationExtensions() {
        return Collections.singletonList(configurationExtension);
    }

    @Override
    public List<? extends PropertyExtensionDescriptor> getPropertyExtensionDescriptors() {
        return Collections.singletonList(issuePropertyExtension);
    }
}
