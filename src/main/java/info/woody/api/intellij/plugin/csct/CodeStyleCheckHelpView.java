package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class CodeStyleCheckHelpView {
    private JPanel helpPanel;
    private JBList checkItemList;

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

        Arrays.stream(CodeStyleCheckIssues.class.getDeclaredFields()).filter(field -> {
            int modifiers = field.getModifiers();
            return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
        }).sorted((f1, f2) -> {
            try {
                return f1.get(null).toString().compareTo(f2.get(null).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return 0;
            }
        }).forEach(field -> {
            try {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)
                        && Modifier.isFinal(modifiers) && field.getName().matches("^(GLOBAL|LINE).*$")) {
                    String element = field.get(null).toString();
                    checkItemListModel.addElement(element);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        checkItemList.setModel(checkItemListModel);
    }
}
