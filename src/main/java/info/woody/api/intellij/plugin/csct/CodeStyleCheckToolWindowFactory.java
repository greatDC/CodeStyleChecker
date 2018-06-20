package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

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
        Content content;
        content = contentFactory.createContent(new CodeStyleCheckResultView(project).getPanel(), TAB_TITLE_RESULTS, false);
        contentManager.addContent(content);
        content = contentFactory.createContent(new CodeStyleCheckHelpView(project).getPanel(), TAB_TITLE_HELP, false);
        contentManager.addContent(content);
    }
}
