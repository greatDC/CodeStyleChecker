package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import java.lang.reflect.Modifier;
import java.util.Arrays;

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

        Arrays.stream(CodeStyleCheckIssues.class.getDeclaredFields()).filter(field -> {
            int modifiers = field.getModifiers();
            return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
        }).sorted((f1, f2) -> {
            try {
                char field1Char = f1.getName().charAt(0);
                char field2Char = f2.getName().charAt(0);
                int charCompareResult = Character.compare(field1Char, field2Char);
                return  (charCompareResult == 0) ? f1.get(null).toString().compareTo(f2.get(null).toString()) : charCompareResult;
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
