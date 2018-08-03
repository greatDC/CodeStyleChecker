package info.woody.api.intellij.plugin.csct;

import com.intellij.execution.application.ApplicationConfigurable;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

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
        //return configurationView.isModified();
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
    }

    @Override
    public void reset() {
        CodeStyleCheckConfigurationService configurationService = ServiceManager.getService(CodeStyleCheckConfigurationService.class);
        configurationView.getCheckBoxExperimental().setSelected(configurationService.getState().isExperimentalEnabled());
    }
}
