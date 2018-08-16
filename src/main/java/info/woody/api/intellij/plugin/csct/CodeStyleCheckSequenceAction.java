package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import info.woody.api.intellij.plugin.csct.syntax.SimpleClassStructureClazz;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;

public class CodeStyleCheckSequenceAction extends AnAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeStyleCheckSequenceAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        System.out.println(LocalDateTime.now());
        System.out.println("public void actionPerformed(@NotNull AnActionEvent anActionEvent)");
        Path path = Paths.get("/Users/renzhengwei/Workstation/Workspace/git/CodeStyleChecker/src/main/java/info/woody/api/intellij/plugin/csct/CodeStyleCheckSyntaxBuilder.groovy");
        SimpleClassStructureClazz clazz = null;
        try {
            clazz = CodeStyleCheckSyntaxBuilder.buildClazz(new String(Files.readAllBytes(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(clazz);
    }

    @Override
    public void update(@NotNull final AnActionEvent anActionEvent) {
        super.update(anActionEvent);
        anActionEvent.setInjectedContext(false);
        final Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
        final Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);


        String name = Thread.currentThread().getName();
        if ("worked".equals(name)) {
            Thread.currentThread().setName("reset");
            return;
        } else {
            Thread.currentThread().setName("worked");
        }

        System.out.println(LocalDateTime.now());
        Arrays.stream(ProjectRootManager.getInstance(project).getContentSourceRoots()).forEach(root -> {
//            try {
//                System.out.println(new String(Files.readAllBytes(Paths.get(root.getCanonicalPath()))));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            System.out.println(root.getCanonicalPath());
        });


        CaretModel caretModel = editor.getCaretModel();
        Document document = editor.getDocument();
        String[] lines = document.getText().split("\r?\n");
        int lineCount = lines.length;
        for (int i = 0; i < lineCount; i++) {

        }




//        if (caretModel.getLogicalPosition().line % 2 == 0) {
//
//        }
//        anActionEvent.getPresentation().setVisible((project != null && editor != null && caretModel.getCaretCount() > 0));
        //anActionEvent.getPresentation().setVisible(caretModel.getLogicalPosition().line % 2 == 0);
    }

    @Override
    public boolean isInInjectedContext() {
        return false;
    }

    private boolean isInClassScope(String[] lines, int lineIndex) {
        return false;
    }

    private boolean isInMethodScope(String[] lines, int lineIndex) {
        return false;
    }
}