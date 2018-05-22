package info.woody.api.intellij.plugin.csct;

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
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDetailFileData;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReport;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
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
import java.util.stream.Collectors;

/**
 * https://www.jetbrains.org/intellij/sdk/docs/welcome.html
 */
public class CodeStyleCheckingTool extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here

        Optional<Module> module = Arrays.stream(ModuleManager.getInstance(e.getProject()).getModules())
                .filter(description -> "t-RetailAPI".equals(description.getName()) || "tRetailAPI".equals(description.getName())).findAny();
        if (!module.isPresent()) {
            Messages.showMessageDialog("You don't have tRetailAPI module in this project yet.", "", Messages.getWarningIcon());
            return;
        }

        Path projectPath = Paths.get(e.getProject().getBaseDir().getPath());
        Path configurationFilePath = Paths.get(projectPath + "/tRetailApiCodeStyleCheckingTool.xml");
        if (Files.notExists(configurationFilePath)) {
            int exitCode = Messages.showOkCancelDialog("You don't have tRetailApiCodeStyleCheckingTool.xml in project. Do you want to create one now?", "WARN", Messages.getWarningIcon());
            if (Messages.OK == exitCode) {
                createDefaultConfigurationFile(configurationFilePath);
            } else {
                return;
            }
        }

        Optional<ContentEntry> contentEntry = Arrays.stream(ModuleRootManager.getInstance(module.get()).getContentEntries())
                .filter(entry -> entry.getFile().getPath().endsWith("tRetailAPI")).findAny();
        if (!contentEntry.isPresent()) {
            Messages.showMessageDialog("The module tRetailAPI's content root is not set well.", "", Messages.getErrorIcon());
            return;
        }
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
        JTextPane summaryTextPane = (JTextPane) rootComponent.getClientProperty("summaryTextPane");
        JTextPane detailsTextPane = (JTextPane) rootComponent.getClientProperty("detailsTextPane");
        JComboBox authorComboBox = (JComboBox) rootComponent.getClientProperty("authorComboBox");

        rootComponent.putClientProperty("REPORT", report);

        // SUMMARY
        // how many files, issues and authors
        // issues grouped by error type
        StringBuilder summaryReportBuilder = new StringBuilder("<pre>");
        int globalErrorCount = report.getSummaryData().getGlobalErrorsGroupByFilePath().entrySet().stream().mapToInt(entry -> entry.getValue().size()).sum();
        int lineErrorCount = report.getSummaryData().getLineErrorsGroupByFilePath().entrySet().stream().mapToInt(entry -> entry.getValue().size()).sum();
        String authors = report.getSummaryData().getAuthorsKeySet().stream()
                //.sorted(author1, author2 -> report.getDetailDetailData().getMapAuthorsErrors().computeIfAbsent(author1, (v) -> 0))
                .map(author -> String.format("<center><a href='%s'>%s</a> delivered %d errors</center>", author, author, report.getDetailDetailData().getMapAuthorsErrors().computeIfAbsent(author, (v) -> 0)))
                .collect(Collectors.joining("<br>"));
        summaryReportBuilder
                .append("<br>").append(String.format("Thanks for:<br>%s", authors))
                .append("<br>").append(String.format("Totally %d issue(s) were found in %d file(s)", globalErrorCount + lineErrorCount, report.getSummaryData().getLineErrorsGroupByFilePath().size(), report.getSummaryData().getFileCountWithIssues() + report.getSummaryData().getFileCountWithoutIssues()))
                .append("<br>").append(String.format("Totally %d out of %d files were clear", report.getSummaryData().getFileCountWithoutIssues(), report.getSummaryData().getFileCountWithIssues() + report.getSummaryData().getFileCountWithoutIssues()))
                .append("<br>").append(String.format("Cleanness rate is %f", report.getSummaryData().getFileCountWithoutIssues() + 0.0d / report.getSummaryData().getFileCountWithIssues() + report.getSummaryData().getFileCountWithoutIssues()))
                .append("<hr>");

        report.getSummaryData().getFilesGroupByError().entrySet().stream().forEach(entry ->
            summaryReportBuilder
                    .append("<br>").append(String.format("%d ERRORS FOR: <font color=yellow>%s</font>", entry.getValue().size(), entry.getKey()))
                    //.append("<br>").append(entry.getValue().stream().map(item -> item.replaceFirst("^.+[\\\\/]", "")).collect(Collectors.joining("<br>")))
        );
        summaryTextPane.setText(summaryReportBuilder.append("</pre>").toString());
        summaryTextPane.setCaretPosition(0);

        // DETAIL grouped by author
        // file full path
        // issue marked with line number

        detailsTextPane.setText(null);
//        authorComboBox.removeAllItems();
//        report.getDetailDetailData().getMapAuthorsErrors().entrySet().stream().forEach(entry -> {
//            // String itemString = String.format("%s(%d)", detailDataEntry.getKey(), detailDataEntry.getValue().getFileDataList().stream().filter(data -> data.));
//            authorComboBox.addItem(StringUtils.leftPad(entry.getValue().toString(), 4).concat(" - ").concat(entry.getKey()));
//        });
//        authorComboBox.setSelectedIndex(-1);
//        authorComboBox.addItemListener(itemEvent -> {
//            String authorsKey = itemEvent.getItem().toString().split("-")[1].trim();
//            String details = report.getDetailDetailData().getFileDataList().stream()
//                    .filter(fileData -> fileData.getAuthorsKey().equals(authorsKey))
//                    .map(CodeStyleCheckDetailFileData::getReportContent).collect(Collectors.joining("<hr>"));
//            detailsTextPane.setText(String.format("<pre>%s</pre>", details));
//            detailsTextPane.setCaretPosition(0);
//        });
        summaryTextPane.addHyperlinkListener(hyperlinkEvent -> {
            if (hyperlinkEvent.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                return;
            }
            String authorsKey = hyperlinkEvent.getDescription();
            String details = report.getDetailDetailData().getFileDataList().stream()
                    .filter(fileData -> fileData.getAuthorsKey().equals(authorsKey))
                    .map(CodeStyleCheckDetailFileData::getReportContent).collect(Collectors.joining("<hr>"));
            detailsTextPane.setText(String.format("<pre>%s</pre>", details));
            detailsTextPane.setCaretPosition(0);
        });

        codeStyleCheckResultView.setTitle("Time: " + LocalDateTime.now());
        codeStyleCheckResultView.show(null);
    }

    private void createDefaultConfigurationFile(Path configurationFilePath) {
        try {
            File configurationFile = configurationFilePath.toFile();
            if (configurationFile.createNewFile()) {
                InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("DefaultConfiguration.xml");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
                Files.write(configurationFilePath, bufferedReader.lines().collect(Collectors.joining("\n")).getBytes("UTF8"));
                Messages.showMessageDialog("Configuration file is successfully created. Please run code check again.", "", Messages.getInformationIcon());
            } else {
                Messages.showMessageDialog("Configuration file is failed to create.", "", Messages.getErrorIcon());
            }
        } catch (IOException e1) {
            Messages.showMessageDialog("Configuration file is failed to create.", "", Messages.getErrorIcon());
        }
    }
}
