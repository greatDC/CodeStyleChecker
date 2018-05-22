package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorProviderManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.nio.file.Paths;

public class CodeStyleCheckResultView implements ToolWindowFactory {
    private JPanel consolePanel;
    private JTextPane detailsTextPane;
    private JTextPane summaryTextPane;
    private JComboBox authorComboBox;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        Content content = contentFactory.createContent(consolePanel, "", false);
        toolWindow.getContentManager().addContent(content);

        setupTextPane("summaryTextPane", summaryTextPane);
        setupTextPane("detailsTextPane", detailsTextPane);
        consolePanel.putClientProperty("authorComboBox", authorComboBox);

//         summaryTextPane.setText("<h1>Welcome<h1><hr/><p>Welcome<br/>Welcome Welcome<br/>Welcome Welcome Welcome<br/>Welcome * Welcome<hr/><pre><a href='#'>Click Me</a></pre></p>");
        detailsTextPane.addHyperlinkListener(e -> {
            if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                return;
            }
            String description = e.getDescription();
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(Paths.get(description).toFile());
            if (null != virtualFile && virtualFile.isValid()) {
                FileEditorProvider[] providers = FileEditorProviderManager.getInstance().getProviders(project, virtualFile);
                if (providers.length > 0) {
                    OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile);
                    Editor editor = FileEditorManager.getInstance(project).openTextEditor(openFileDescriptor, false);
                    //PropertiesComponent.getInstance().
//                    CaretModel caretModel = editor.getCaretModel();
//
//                    String text = summaryTextPane.getText();
//                    int fromIndex = text.indexOf(description);
//                    int caretPos = text.indexOf(description.replaceAll("^.*[\\\\/]", ""), fromIndex + description.length());
//                    summaryTextPane.setCaretPosition(caretPos);
                    //caretModel.moveToLogicalPosition(new LogicalPosition(lineNumber -1, 0));
                    //editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                }
            }
        });
    }

    private void setupTextPane(String idTextPane, JTextPane textPane) {
        consolePanel.putClientProperty(idTextPane, textPane);
        textPane.setEditable(false);
        textPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
    }
}
