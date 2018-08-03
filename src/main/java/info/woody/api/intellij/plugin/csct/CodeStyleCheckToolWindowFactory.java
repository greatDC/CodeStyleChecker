package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDictionary;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.function.BiConsumer;

/**
 * Window factory to create windows.
 *
 * @author Woody
 * @since 20/06/2018
 */
public class CodeStyleCheckToolWindowFactory implements ToolWindowFactory {

    public static final String TAB_TITLE_RESULTS = "Results";
    public static final String TAB_TITLE_HELP = "Help";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        addContent(project, toolWindow.getContentManager(), contentFactory);
    }

    /**
     * Add contents for tool window.
     *
     * @param project        Virtual project.
     * @param contentManager Plugin content manager.
     * @param contentFactory Plugin content factory.
     */
    private void addContent(@NotNull Project project, ContentManager contentManager, ContentFactory contentFactory) {
        BiConsumer<JComponent, String> addContent = (component, displayName) ->
                contentManager.addContent(contentFactory.createContent(component, displayName, false));
        addContent.accept(new CodeStyleCheckResultView(project).getPanel(), TAB_TITLE_RESULTS);
        addContent.accept(new CodeStyleCheckHelpView(project).getPanel(), TAB_TITLE_HELP);
    }
}
