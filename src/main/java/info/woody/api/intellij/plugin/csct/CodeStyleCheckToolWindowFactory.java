package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

public class CodeStyleCheckToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        addContent(project, toolWindow.getContentManager(), contentFactory);
    }

    private void addContent(@NotNull Project project, ContentManager contentManager, ContentFactory contentFactory) {
        Content content = null;
        content = contentFactory.createContent(new CodeStyleCheckResultView(project).getPanel(), "Results", false);
        contentManager.addContent(content);
        content = contentFactory.createContent(new CodeStyleCheckHelpView(project).getPanel(), "Help", false);
        contentManager.addContent(content);
    }
}
