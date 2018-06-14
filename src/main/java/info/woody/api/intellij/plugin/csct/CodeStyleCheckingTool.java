package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReport;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckSummaryData;
import org.apache.commons.lang.StringEscapeUtils;

import javax.swing.JComponent;
import javax.swing.JTextPane;
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
import java.util.Optional;

import static info.woody.api.intellij.plugin.csct.Const.LINE_SEPARATOR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

/**
 * This action will be triggered by the menu. The tutorial could be found at below link.
 * <p>https://www.jetbrains.org/intellij/sdk/docs/welcome.html</p>
 *
 * @author Woody
 */
public class CodeStyleCheckingTool extends AnAction {

    public static final String SUMMARY_TEXT_PANE = "summaryTextPane";
    public static final String DETAILS_TEXT_PANE = "detailsTextPane";
    public static final String REPORT_INFO = "REPORT_INFO";
    private static final String ACTION_ID_SAVE_ALL = "SaveAll";
    private static final String DEFAULT_CONFIGURATION_XML = "DefaultConfiguration.xml";

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (false) {
            Optional<Module> module = Arrays.stream(ModuleManager.getInstance(e.getProject()).getModules())
                    .filter(description -> description.getName().matches("^t-?RetailAPI$")).findAny();
            if (!module.isPresent()) {
                Messages.showMessageDialog("You don't have tRetailAPI module in this project yet.", "", Messages.getWarningIcon());
                return;
            }

            Optional<ContentEntry> contentEntry = Arrays.stream(ModuleRootManager.getInstance(module.orElse(null)).getContentEntries())
                    .filter(entry -> entry.getFile().getPath().endsWith("tRetailAPI")).findAny();
            if (!contentEntry.isPresent()) {
                Messages.showMessageDialog("The module tRetailAPI's content root is not set well.", "", Messages.getErrorIcon());
                return;
            }
        }// don't check for a specific module
        Optional<ContentEntry> contentEntry = Optional.ofNullable(null);

        Path projectPath = Paths.get(e.getProject().getBaseDir().getPath());
        Path configurationFilePath = Paths.get(projectPath + "/tRetailApiCodeStyleCheckingTool.xml");
        if (Files.notExists(configurationFilePath)) {
            int exitCode = Messages.showOkCancelDialog(
                    "You don't have tRetailApiCodeStyleCheckingTool.xml in project. Do you want to create one now?",
                    "", Messages.getWarningIcon());
            if (Messages.OK == exitCode) {
                createDefaultConfigurationFile(configurationFilePath);
            } else {
                return;
            }
        }

        ActionManager.getInstance().getAction(ACTION_ID_SAVE_ALL).actionPerformed(e);

        String tRetailApiModuleRootDir = contentEntry.map(ContentEntry::getFile).map(VirtualFile::getPath).orElse(null);
        CodeStyleCheckContext context = CodeStyleCheckContext.newInstance(configurationFilePath.toFile(), tRetailApiModuleRootDir);
        CodeStyleCheckImpl codeStyleCheck = new CodeStyleCheckImpl();
        codeStyleCheck.MY_SOURCE_DIR = context.MY_SOURCE_DIR();
        codeStyleCheck.FILENAME_PATTERN_TO_SKIP = context.FILENAME_PATTERN_TO_SKIP();
        codeStyleCheck.FILES_TO_SKIP = context.FILES_TO_SKIP();
        codeStyleCheck.GIT_FILES_TO_MERGE = context.GIT_FILES_TO_MERGE();
        CodeStyleCheckReport report = codeStyleCheck.doCheck().calculateStatistics();

        ToolWindow codeStyleCheckResultView = ToolWindowManager.getInstance(e.getProject()).getToolWindow("Code scanning results");
        JComponent rootComponent = codeStyleCheckResultView.getContentManager().getSelectedContent().getComponent();
        JTextPane summaryTextPane = (JTextPane) rootComponent.getClientProperty(SUMMARY_TEXT_PANE);
        JTextPane detailsTextPane = (JTextPane) rootComponent.getClientProperty(DETAILS_TEXT_PANE);
        rootComponent.putClientProperty(REPORT_INFO, report);

        // update the tool window
        summaryTextPane.setText(generateSummaryReport(report));
        summaryTextPane.setCaretPosition(0);
        detailsTextPane.setText(null);
        codeStyleCheckResultView.setTitle("Time: " + LocalDateTime.now());
        codeStyleCheckResultView.show(null);
    }

    /**
     * Generate formatted report.
     *
     * @param report Report data.
     * @return The formatted report of HTML format.
     */
    private String generateSummaryReport(CodeStyleCheckReport report) {
        StringBuilder summaryReportBuilder = new StringBuilder("<pre>");
        CodeStyleCheckSummaryData summaryData = report.getSummaryData();
        int globalErrorCount = summaryData.getGlobalErrorsGroupByFilePath().entrySet().stream()
                .mapToInt(entry -> entry.getValue().size()).sum();
        int lineErrorCount = summaryData.getLineErrorsGroupByFilePath().entrySet().stream()
                .mapToInt(entry -> entry.getValue().size()).sum();
        String authors = summaryData.getAuthorsKeySet().stream()
                //.sorted(author1, author2 -> report.getDetailDetailData().getMapAuthorsErrors().computeIfAbsent(author1, (v) -> 0))
                .map(author -> String.format("<span style='text-align:center'><a href='%s'>%s</a> delivered %d errors</span>",
                        author, author, report.getDetailDetailData().getMapAuthorsErrors().computeIfAbsent(author, (v) -> 0)))
                .collect(joining("<br>"));
        int fileCountWithoutIssues = summaryData.getFileCountWithoutIssues();
        int fileCountWithIssues = summaryData.getFileCountWithIssues();
        int fileCount = fileCountWithIssues + fileCountWithoutIssues;
        summaryReportBuilder
                .append("<br>").append(String.format("Thanks for:<br>%s", authors))
                .append("<br>").append(String.format("Totally %d issue(s) were found in %d file(s)", globalErrorCount + lineErrorCount,
                summaryData.getLineErrorsGroupByFilePath().size(), fileCount))
                .append("<br>").append(String.format("Totally %d out of %d files were clear", fileCountWithoutIssues, fileCount))
                .append("<br>").append(String.format("Cleanness rate is %f", (fileCountWithoutIssues + 0.0d) / (fileCount)))
                .append("<hr>");

        summaryData.getFilesGroupByError().entrySet().stream().forEach(entry ->
                summaryReportBuilder.append("<br>").append(String.format("%d ERRORS FOR: <font color='red'>%s</font>",
                        entry.getValue().size(), StringEscapeUtils.escapeHtml(entry.getKey())))
        );
        return summaryReportBuilder.append("</pre>").toString();
    }

    /**
     * Create the configuration file as to the given path.
     *
     * @param configurationFilePath The configuration file path.
     */
    private void createDefaultConfigurationFile(Path configurationFilePath) {
        try {
            File configurationFile = configurationFilePath.toFile();
            if (configurationFile.createNewFile()) {
                InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_CONFIGURATION_XML);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
                Files.write(configurationFilePath, bufferedReader.lines().collect(joining(LINE_SEPARATOR)).getBytes(UTF_8.name()));
                Messages.showMessageDialog("Configuration file is successfully created. Please try again.",
                        "", Messages.getInformationIcon());
            } else {
                Messages.showMessageDialog("Configuration file is failed to create.", "", Messages.getErrorIcon());
            }
        } catch (IOException e) {
            Messages.showMessageDialog("Configuration file is failed to create.", "", Messages.getErrorIcon());
        }
    }
}
