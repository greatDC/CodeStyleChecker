package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Plugin configuration view.
 *
 * @author zhengwei.ren
 * @since 04/08/2018
 */
public class CodeStyleCheckConfigurationView {
    private boolean modified;
    private JPanel settingPanel;
    private JBCheckBox checkBoxExperimental;
    private JBCheckBox checkBoxTestMethodPrefixedByTest;
    private JTextField textFieldBadNamingSkipList;

    /**
     * Constructor.
     */
    public CodeStyleCheckConfigurationView() {
        CodeStyleCheckConfigurationState state = ServiceManager.getService(CodeStyleCheckConfigurationService.class).getState();
        checkBoxExperimental.addChangeListener(e -> {
            this.modified = true;
            state.setExperimentalEnabled(((JBCheckBox) e.getSource()).isSelected());
        });
        checkBoxTestMethodPrefixedByTest.addChangeListener(e -> {
            this.modified = true;
            state.setTestPrefixInTestForced(((JBCheckBox) e.getSource()).isSelected());
        });
        textFieldBadNamingSkipList.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update(e);
            }

            private void update(DocumentEvent e) {
                CodeStyleCheckConfigurationView.this.modified = true;
                String[] skipWords = textFieldBadNamingSkipList.getText().split(",");
                List<String> badNamingSkipList = Arrays.stream(skipWords)
                        .map(String::trim)
                        .filter(word -> !word.isEmpty())
                        .collect(toList());
                state.setBadNamingSkipList(badNamingSkipList);
            }
        });
    }

    public boolean isModified() {
        try {
            return modified;
        } finally {
            modified = false;
        }
    }

    /**
     * Get setting panel.
     *
     * @return The setting panel.
     */
    public JPanel getSettingPanel() {
        return settingPanel;
    }

    public JBCheckBox getCheckBoxExperimental() {
        return checkBoxExperimental;
    }

    public JBCheckBox getCheckBoxTestMethodPrefixedByTest() {
        return checkBoxTestMethodPrefixedByTest;
    }

    public JTextField getTextFieldBadNamingSkipList() {
        return textFieldBadNamingSkipList;
    }
}
