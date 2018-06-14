package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.project.Project;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

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

        globalErrorList.removeAll();
        lineErrorList.removeAll();

        DefaultListModel<String> globalListModel = new DefaultListModel<>();
        DefaultListModel<String> lineListModel = new DefaultListModel<>();

        Arrays.stream(CodeStyleCheckIssues.class.getDeclaredFields()).sorted(Comparator.comparing(Field::getName)).forEach(field -> {
            try {
                String element = field.get(null).toString();
                if (field.getName().startsWith("LINE")) {
                    lineListModel.addElement(element);
                } else if (field.getName().startsWith("GLOBAL")) {
                    globalListModel.addElement(element);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        globalErrorList.setModel(globalListModel);
        lineErrorList.setModel(lineListModel);
        globalErrorList.addListSelectionListener(e -> {
            System.out.println(e.getSource());
            System.out.println(e.getSource().getClass());
        });

    }
}
