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
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDetailFileData;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReport;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static info.woody.api.intellij.plugin.csct.CodeStyleCheckingTool.DETAILS_TEXT_PANE;
import static info.woody.api.intellij.plugin.csct.CodeStyleCheckingTool.REPORT_INFO;
import static info.woody.api.intellij.plugin.csct.CodeStyleCheckingTool.SUMMARY_TEXT_PANE;

/**
 * Tool window to show the scanning result.
 *
 * @author Woody
 */
public class CodeStyleCheckResultView implements ToolWindowFactory {
    private JPanel consolePanel;
    private JTextPane detailsTextPane;
    private JTextPane summaryTextPane;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        Content content = contentFactory.createContent(consolePanel, "", false);
        toolWindow.getContentManager().addContent(content);

        setupTextPane(SUMMARY_TEXT_PANE, summaryTextPane);
        setupTextPane(DETAILS_TEXT_PANE, detailsTextPane);

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
                    // PropertiesComponent.getInstance().
//                    CaretModel caretModel = editor.getCaretModel();
//
//                    String text = summaryTextPane.getText();
//                    int fromIndex = text.indexOf(description);
//                    int caretPos = text.indexOf(description.replaceAll("^.*[\\\\/]", ""), fromIndex + description.length());
//                    summaryTextPane.setCaretPosition(caretPos);
                    // caretModel.moveToLogicalPosition(new LogicalPosition(lineNumber -1, 0));
                    // editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                }
            }
        });

        summaryTextPane.addHyperlinkListener(hyperlinkEvent -> {
            if (hyperlinkEvent.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                return;
            }
            CodeStyleCheckReport report = (CodeStyleCheckReport) consolePanel.getClientProperty(REPORT_INFO);
            String authorsKey = hyperlinkEvent.getDescription();
            String details = report.getDetailDetailData().getFileDataList().stream()
                    .filter(fileData -> fileData.getAuthorsKey().equals(authorsKey))
                    .map(CodeStyleCheckDetailFileData::getReportContent).collect(Collectors.joining("<hr>"));
            detailsTextPane.setText(String.format("<pre>%s</pre>", details));
            detailsTextPane.setCaretPosition(0);
        });
    }

    /**
     * Set up the text pane.
     *
     * @param idTextPane Text pane's ID.
     * @param textPane   Instance of text pane.
     */
    private void setupTextPane(String idTextPane, JTextPane textPane) {
        consolePanel.putClientProperty(idTextPane, textPane);
        textPane.setEditable(false);
        textPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
    }
}
