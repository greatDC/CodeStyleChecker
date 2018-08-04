package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;

import javax.swing.*;

/**
 * Help view in the tool window.
 *
 * @author Woody
 * @since 15/06/2018
 */
public class CodeStyleCheckHelpView {
    private JPanel helpPanel;
    private JBList checkItemList;
    private Project project;

    /**
     * Constructor.
     *
     * @param project Virtual project.
     */
    CodeStyleCheckHelpView(Project project) {
        this.project = project;
        this.init();
    }

    /**
     * Get the main panel content.
     *
     * @return Panel.
     */
    public JPanel getPanel() {
        return this.helpPanel;
    }

    /**
     * Initialization.
     */
    private void init() {
        DefaultListModel<String> checkItemListModel = new DefaultListModel<>();
        CodeStyleCheckIssues.ALL_CHECK_ITEMS().entrySet().stream().forEach(entry -> checkItemListModel.addElement(entry.getValue()));
        checkItemList.setModel(checkItemListModel);
    }
}
