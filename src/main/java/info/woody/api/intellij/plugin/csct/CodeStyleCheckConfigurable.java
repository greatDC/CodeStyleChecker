package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * Plugin configuration.
 *
 * @author zhengwei.ren
 * @since 04/08/2018
 */
public class CodeStyleCheckConfigurable implements Configurable {

    private CodeStyleCheckConfigurationView configurationView = new CodeStyleCheckConfigurationView();

    @Nls
    @Override
    public String getDisplayName() {
        return "Code Style Check Tool";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return configurationView.getSettingPanel();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
    }

    @Override
    public void reset() {
        CodeStyleCheckConfigurationService configurationService = ServiceManager.getService(CodeStyleCheckConfigurationService.class);
        CodeStyleCheckConfigurationState state = configurationService.getState();
        configurationView.getCheckBoxExperimental().setSelected(state.isExperimentalEnabled());
        configurationView.getCheckBoxTestMethodPrefixedByTest().setSelected(state.isTestPrefixInTestForced());
        configurationView.getTextFieldBadNamingSkipList().setText(state.getBadNamingSkipList().stream().collect(joining(", ")));
    }
}
