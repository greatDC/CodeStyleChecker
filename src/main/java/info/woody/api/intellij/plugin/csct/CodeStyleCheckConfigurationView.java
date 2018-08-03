package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class CodeStyleCheckConfigurationView {
    private boolean modified;
    private JPanel settingPanel;
    private JBCheckBox checkBoxExperimental;

    public CodeStyleCheckConfigurationView() {
        checkBoxExperimental.addChangeListener(e -> {
            this.modified = true;
            ServiceManager.getService(CodeStyleCheckConfigurationService.class).getState().setExperimentalEnabled(((JBCheckBox)e.getSource()).isSelected());
        });
    }

    public boolean isModified() {
        try {
            return modified;
        } finally {
            modified = false;
        }
    }

    public JPanel getSettingPanel() {
        return settingPanel;
    }

    public JBCheckBox getCheckBoxExperimental() {
        return checkBoxExperimental;
    }
}
