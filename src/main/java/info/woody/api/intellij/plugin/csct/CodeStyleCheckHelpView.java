package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.project.Project;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

public class CodeStyleCheckHelpView {
    private JPanel helpPanel;
    private JList checkItemList;

    private Project project;

    CodeStyleCheckHelpView(Project project) {
        this.project = project;
        this.init();
    }

    public JPanel getPanel() {
        return this.helpPanel;
    }

    private void init() {

        DefaultListModel<String> checkItemListModel = new DefaultListModel<>();

        Arrays.stream(CodeStyleCheckIssues.class.getDeclaredFields()).filter(field -> Modifier.isPublic(field.getModifiers())
                && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())
        ).sorted((f1, f2) -> {
            try {
                return f1.get(null).toString().compareTo(f2.get(null).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return 0;
            }
        }).forEach(field -> {
            try {
                String element = field.get(null).toString();

                if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers()) && field.getName().matches("^(GLOBAL|LINE).*$")) {
                    checkItemListModel.addElement(element);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        checkItemList.setModel(checkItemListModel);
    }
}
