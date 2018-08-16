package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDictionary;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckLineError;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReportData;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReportSummary;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckSummaryData;
import info.woody.api.intellij.plugin.csct.core.CodeStyleCheckContext;
import info.woody.api.intellij.plugin.csct.core.CodeStyleCheckRule;
import info.woody.api.intellij.plugin.csct.core.CodeStyleCheckRuleImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static info.woody.api.intellij.plugin.csct.util.Const.HTML_TAG_BR;
import static info.woody.api.intellij.plugin.csct.util.Const.HTML_TAG_HR;
import static info.woody.api.intellij.plugin.csct.util.Const.LINE_SEPARATOR;
import static info.woody.api.intellij.plugin.csct.util.EditorUtils.openFileInEditor;
import static info.woody.api.intellij.plugin.csct.util.RichTextMaker.newHighlight;
import static info.woody.api.intellij.plugin.csct.util.RichTextMaker.newLink;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

/**
 * This action will be triggered by the menu.
 * The tutorial could be found at below link.
 * <p>https://www.jetbrains.org/intellij/sdk/docs/welcome.html</p>
 *
 * @author Woody
 */
public class CodeStyleCheckScanAction extends AnAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeStyleCheckScanAction.class);

    public static final String SUMMARY_OVERVIEW_TEXT_PANE = "summaryOverviewTextPane";
    public static final String SUMMARY_FILE_TEXT_PANE = "summaryFileTextPane";
    public static final String SUMMARY_AUTHOR_TEXT_PANE = "summaryAuthorTextPane";
    public static final String DETAILS_TEXT_PANE = "detailsTextPane";
    public static final String REPORT_INFO = "REPORT_INFO";
    private static final String ACTION_ID_SAVE_ALL = "SaveAll";
    private static final String DEFAULT_CONFIGURATION_XML = "DefaultConfiguration.xml";
    private static final String DEFAULT_MODULE_CONTENT = "tRetailAPI";

    @Override
    public void actionPerformed(AnActionEvent e) {
        CodeStyleCheckDictionary.getDictionary();
        Optional<ContentEntry> contentEntry = Optional.ofNullable(null);
        Optional<Module> module = Arrays.stream(ModuleManager.getInstance(e.getProject()).getModules())
                .filter(description -> description.getName().matches("^t-?RetailAPI$")).findAny();
        if (module.isPresent()) {
            contentEntry = Arrays.stream(ModuleRootManager.getInstance(module.orElse(null)).getContentEntries())
                    .filter(entry -> entry.getFile().getPath().endsWith(DEFAULT_MODULE_CONTENT)).findAny();
        }
        Path projectPath = Paths.get(e.getProject().getBaseDir().getPath());
        Path configurationFilePath = Paths.get(projectPath + "/CodeStyleCheckTool.xml");
        File configurationFile = configurationFilePath.toFile();
        if (Files.notExists(configurationFilePath)) {
            int exitCode = Messages.showOkCancelDialog(
                    "The configuration file CodeStyleCheckTool.xml isn't in project. Do you want to create now?",
                    "", Messages.getWarningIcon());
            if (Messages.OK == exitCode && createConfigurationFile(configurationFilePath)) {
                openFileInEditor(configurationFile.getAbsolutePath(), e.getProject());
            }
            return;
        }
        ActionManager.getInstance().getAction(ACTION_ID_SAVE_ALL).actionPerformed(e);

        String defaultModuleRootDir = contentEntry.map(ContentEntry::getFile).map(VirtualFile::getPath).orElse(null);
        CodeStyleCheckContext context = CodeStyleCheckContext.newInstance(configurationFile, defaultModuleRootDir);
        if (null == context) {
            showWarningMessage("The configuration file is corrupted. Please delete it!");
            return;
        }
        if (null == context.MY_SOURCE_DIR()) {
            showWarningMessage("The path configured in `SourceDir` is incorrectly configured.");
            openFileInEditor(configurationFile.getAbsolutePath(), e.getProject());
            return;
        }
        ToolWindowManager.getInstance(e.getProject()).getToolWindow("Code scanning results").show(null);
        ApplicationManager.getApplication().executeOnPooledThread(() ->
            ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Code Style Check Progress") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setText("Code style is checking ...");
                    indicator.setIndeterminate(true);
                    BiFunction<String, Double, Boolean> updateProgress = (String title, Double progress) -> {
                        indicator.setText2(title);
                        indicator.setFraction(progress);
                        return indicator.isCanceled();
                    };
                    doCodeStyleCheck(e, configurationFile, context, updateProgress);
                }
            })
        );
    }

    private void doCodeStyleCheck(AnActionEvent e, File configurationFile, CodeStyleCheckContext context,
                                  BiFunction<String, Double, Boolean> updateProgress) {
        // check files
        CodeStyleCheckRule codeStyleCheck = new CodeStyleCheckRuleImpl();
        codeStyleCheck.PROGRESS = updateProgress;
        codeStyleCheck.MY_SOURCE_DIR = context.MY_SOURCE_DIR();
        codeStyleCheck.FILENAME_PATTERN_TO_SKIP = context.FILENAME_PATTERN_TO_SKIP();
        codeStyleCheck.FILES_TO_SKIP = context.FILES_TO_SKIP();
        codeStyleCheck.GIT_FILES_TO_MERGE = context.GIT_FILES_TO_MERGE();
        String sourcePresentation = e.getInputEvent().getSource().toString();
        if (sourcePresentation.startsWith("QUICK:")) {
            codeStyleCheck.GIT_FILES_TO_MERGE = sourcePresentation.replaceAll("QUICK:", "");
        }
        CodeStyleCheckReportData report = codeStyleCheck.doCheck();
        ToolWindow codeStyleCheckResultView = ToolWindowManager.getInstance(e.getProject()).getToolWindow("Code scanning results");
        ContentManager contentManager = codeStyleCheckResultView.getContentManager();
        Content resultsContent = contentManager.getContent(0);
        JComponent rootComponent = resultsContent.getComponent();
        JTextPane summaryOverviewTextPane = (JTextPane) rootComponent.getClientProperty(SUMMARY_OVERVIEW_TEXT_PANE);
        JTextPane summaryFileTextPane = (JTextPane) rootComponent.getClientProperty(SUMMARY_FILE_TEXT_PANE);
        JTextPane summaryAuthorTextPane = (JTextPane) rootComponent.getClientProperty(SUMMARY_AUTHOR_TEXT_PANE);
        JTextPane detailsTextPane = (JTextPane) rootComponent.getClientProperty(DETAILS_TEXT_PANE);
        rootComponent.putClientProperty(REPORT_INFO, report);

        // update the tool window
        SwingUtilities.invokeLater(() -> {
            CodeStyleCheckReportSummary reportSummary = generateSummaryReport(report, configurationFile);
            summaryOverviewTextPane.setText(reportSummary.getOverviewSummary());
            summaryFileTextPane.setText(reportSummary.getFileSummary());
            summaryAuthorTextPane.setText(reportSummary.getAuthorSummary());
            summaryFileTextPane.setCaretPosition(0);
            detailsTextPane.setText(null);
            contentManager.setSelectedContent(resultsContent);
            codeStyleCheckResultView.setTitle("Time: " + LocalDateTime.now());
            codeStyleCheckResultView.show(null);
        });
    }

    /**
     * Show a warning message dialog window.
     *
     * @param message The warning message.
     */
    private void showWarningMessage(String message) {
        Messages.showMessageDialog(message, "", Messages.getWarningIcon());
    }

    /**
     * Generate formatted report.
     *
     * @param report Report data.
     * @param configurationFile The configuration file.
     * @return The formatted report of HTML format.
     */
    private CodeStyleCheckReportSummary generateSummaryReport(CodeStyleCheckReportData report, File configurationFile) {
        CodeStyleCheckSummaryData summaryData = report.getSummaryData();
        int fileCount = report.getFileCount();
        int fileCountWithIssues = summaryData.getFileCountWithIssues();
        int fileCountWithoutIssues = fileCount - fileCountWithIssues;
        int globalErrorCount = summaryData.getGlobalErrorsGroupByFilePath().entrySet().stream()
                .mapToInt(entry -> entry.getValue().size()).sum();
        Map<String, List<CodeStyleCheckLineError>> lineErrorsGroupByFilePath = summaryData.getLineErrorsGroupByFilePath();
        int lineErrorCount = lineErrorsGroupByFilePath.entrySet().stream()
                .mapToInt(entry -> entry.getValue().size()).sum();
        int errorCount = globalErrorCount + lineErrorCount;
        String configurationFileName = configurationFile.getName();

        CodeStyleCheckReportSummary reportSummary = new CodeStyleCheckReportSummary();
        reportSummary.setOverviewSummary(new StringBuilder("<pre>")
                .append(String.format("The configuration file: %s",
                        newLink(configurationFile.getAbsolutePath(), configurationFileName, configurationFileName)))
                .append(HTML_TAG_BR).append(format("%d issue(s) were found in %d file(s).", errorCount, fileCountWithIssues, fileCount))
                .append(HTML_TAG_BR).append(format("Totally %d out of %d files were clear.", fileCountWithoutIssues, fileCount))
                .append(HTML_TAG_BR).append(format("Cleanness rate is %,.2f%%.", (fileCountWithoutIssues + 0.0d) / fileCount * 100))
                .append(HTML_TAG_HR).append("</pre>").toString()
        );

        String fileSummary = summaryData.getFilesGroupByError().entrySet().stream().map(entry -> {
            String size = String.valueOf(entry.getValue().size());
            String error = entry.getKey();
            return format("%s ERRORS FOR: %s", newLink("ISSUE#" + error, error, size), newHighlight(error));
        }).collect(joining(HTML_TAG_BR));
        reportSummary.setFileSummary(format("<pre>%s</pre>", fileSummary));

        String authorSummary = summaryData.getAuthorsKeySet().stream()
                .map(author -> format("<span style='text-align:center'>%s delivered %d issues.</span>",
                        newLink("AUTHOR#" + author, author, author),
                        report.getDetailData().getMapAuthorsErrors().computeIfAbsent(author, (v) -> 0)))
                .collect(joining(HTML_TAG_BR));
        reportSummary.setAuthorSummary(format("<pre>Thanks for:<br>%s</pre>", authorSummary));

        return reportSummary;
    }

    /**
     * Create the configuration file as to the given path.
     *
     * @param configurationFilePath The configuration file path.
     * @return {@code true} for successful file creation, otherwise {@code false}.
     */
    private boolean createConfigurationFile(Path configurationFilePath) {
        String errorMessage = "Configuration file is failed to create.";
        try {
            File configurationFile = configurationFilePath.toFile();
            if (configurationFile.createNewFile()) {
                InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIGURATION_XML);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
                Files.write(configurationFilePath, bufferedReader.lines().collect(joining(LINE_SEPARATOR)).getBytes(UTF_8.name()));
                Messages.showMessageDialog("Configuration file is successfully created. Please try again.",
                        "", Messages.getInformationIcon());
                VirtualFileManager.getInstance().asyncRefresh(null);
                return true;
            } else {
                Messages.showMessageDialog(errorMessage, "", Messages.getErrorIcon());
            }
        } catch (IOException e) {
            LOGGER.error(errorMessage, e);
            Messages.showMessageDialog(errorMessage, "", Messages.getErrorIcon());
        }
        return false;
    }
}
