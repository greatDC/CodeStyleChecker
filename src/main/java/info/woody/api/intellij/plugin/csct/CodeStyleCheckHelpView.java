package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.project.Project;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

public class CodeStyleCheckHelpView {
    private JPanel helpPanel;
    private JList lineErrorList;
    private JTextPane solutionTextPane;
    private JList globalErrorList;

    private Project project;

    CodeStyleCheckHelpView(Project project) {
        this.project = project;
        this.init();
    }

    public JPanel getPanel() {
        return this.helpPanel;
    }

    private void init() {

    }
}
