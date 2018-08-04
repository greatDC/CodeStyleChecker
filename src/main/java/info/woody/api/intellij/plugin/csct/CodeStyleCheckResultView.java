package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.vcs.log.util.StopWatch;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDetailFileData;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckGlobalError;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckLineError;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReport;
import info.woody.api.intellij.plugin.csct.util.CodeStyleCheckEnum.SummaryLinkType;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import java.util.List;
import java.util.stream.Collectors;

import static info.woody.api.intellij.plugin.csct.CodeStyleCheckTool.DETAILS_TEXT_PANE;
import static info.woody.api.intellij.plugin.csct.CodeStyleCheckTool.REPORT_INFO;
import static info.woody.api.intellij.plugin.csct.CodeStyleCheckTool.SUMMARY_TEXT_PANE;
import static info.woody.api.intellij.plugin.csct.util.Const.HTML_TAG_BR;
import static info.woody.api.intellij.plugin.csct.util.Const.SIGN_HASH;
import static info.woody.api.intellij.plugin.csct.util.EditorUtils.openFileInEditor;

/**
 * Scanning result view in the tool window.
 *
 * @author Woody
 * @since 15/06/2018
 */
public class CodeStyleCheckResultView {
    private JPanel consolePanel;
    private JTextPane detailsTextPane;
    private JTextPane summaryTextPane;

    private Project project;

    /**
     * Constructor.
     *
     * @param project Project.
     */
    CodeStyleCheckResultView(Project project) {
        this.project = project;
        this.init();
    }

    /**
     * Get main panel containing all UI elements.
     *
     * @return The main panel component.
     */
    public JPanel getPanel() {
        return this.consolePanel;
    }

    /**
     * Initiate components.
     */
    private void init() {
        setupTextPane(SUMMARY_TEXT_PANE, summaryTextPane);
        setupTextPane(DETAILS_TEXT_PANE, detailsTextPane);

        detailsTextPane.addHyperlinkListener(e -> {
            if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                return;
            }
            String description = e.getDescription();
            String path;
            int lineIndex = 0;
            if (description.contains(SIGN_HASH)) {
                path = description.replaceFirst("#.+$", "");
                lineIndex = Integer.valueOf(description.replaceFirst("^.+#", "")) - 1;
            } else {
                path = description;
            }
            Editor editor = openFileInEditor(path, project);
            if (editor != null) {
                editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(lineIndex, 0));
                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                editor.getSelectionModel().selectLineAtCaret();
            }
        });

        summaryTextPane.addHyperlinkListener(hyperlinkEvent -> {
            if (hyperlinkEvent.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                return;
            }
            String description = hyperlinkEvent.getDescription();
            if (description.contains(SIGN_HASH)) {
                String[] hrefMeta = description.split(SIGN_HASH);
                String linkType = hrefMeta[0];
                String linkValue = hrefMeta[1];

                CodeStyleCheckReport report = (CodeStyleCheckReport) consolePanel.getClientProperty(REPORT_INFO);
                List<CodeStyleCheckDetailFileData> fileDataList = report.getDetailData().getFileDataList();
                if (SummaryLinkType.ISSUE.name().equals(linkType)) {
                    String issueItem = linkValue;
                    StopWatch stopWatch = StopWatch.start("outer");
                    List<CodeStyleCheckGlobalError> allGlobalIssues = fileDataList.parallelStream()
                            .map(CodeStyleCheckDetailFileData::getGlobalErrorList).flatMap(List::stream)
                            .filter(codeStyleCheckGlobalError -> codeStyleCheckGlobalError.getError().equals(issueItem))
                            .collect(Collectors.toList());
                    List<CodeStyleCheckLineError> allLineIssues = fileDataList.parallelStream()
                            .map(CodeStyleCheckDetailFileData::getLineErrorList).flatMap(List::stream)
                            .filter(codeStyleCheckLineError -> codeStyleCheckLineError.getError().equals(issueItem))
                            .collect(Collectors.toList());
                    stopWatch.report();
                    String details = "";
                    if (!allGlobalIssues.isEmpty()) {
                        details = CodeStyleCheckDetailFileData.getReportForGlobalIssue(allGlobalIssues);
                    } else if (!allLineIssues.isEmpty()) {
                        details = CodeStyleCheckDetailFileData.getReportForLineIssue(allLineIssues);
                    }
                    detailsTextPane.setText(String.format("<pre>%s</pre>", details));
                    detailsTextPane.setCaretPosition(0);
                } else if (SummaryLinkType.AUTHOR.name().equals(linkType)) {
                    String authorsKey = linkValue;
                    String details = fileDataList.parallelStream()
                            .filter(fileData -> fileData.getAuthorsKey().equals(authorsKey))
                            .map(CodeStyleCheckDetailFileData::getReportForAuthor).collect(Collectors.joining(HTML_TAG_BR));
                    detailsTextPane.setText(String.format("<pre>%s</pre>", details));
                    detailsTextPane.setCaretPosition(0);
                }
            } else {
                openFileInEditor(description, project);
            }
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
