package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This action will be triggered by the editor's pop menu.
 *
 * @author Woody
 */
public class CodeStyleCheckQuickDiagnoseAction extends AnAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeStyleCheckQuickDiagnoseAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        e.getInputEvent().setSource("QUICK:" + e.getData(CommonDataKeys.PSI_FILE).getVirtualFile().getPath());
        ActionManager.getInstance().getAction(CodeStyleCheckScanAction.class.getSimpleName()).actionPerformed(e);
    }

    @Override
    public void update(@NotNull final AnActionEvent e) {
        super.update(e);
        String extension = e.getData(CommonDataKeys.PSI_FILE).getVirtualFile().getExtension();
        if ("java".equals(extension) || "groovy".equals(extension)) {
            e.getPresentation().setEnabled(true);
        } else {
            e.getPresentation().setVisible(false);
        }
    }
}
