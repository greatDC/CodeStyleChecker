package info.woody.api.intellij.plugin.csct.bean

import info.woody.api.intellij.plugin.csct.util.Const

import static info.woody.api.intellij.plugin.csct.util.Const.HTML_TAG_BR
import static info.woody.api.intellij.plugin.csct.util.RichTextMaker.escapeContent
import static info.woody.api.intellij.plugin.csct.util.RichTextMaker.newHighlight
import static info.woody.api.intellij.plugin.csct.util.RichTextMaker.newLink

/**
 * File detail info.
 *
 * @author Woody
 */
class CodeStyleCheckDetailFileData extends CodeStyleCheckSummaryFileData {
    private static final int PADDING_WIDTH = 6
    private String reportContent = ""
    String authorsKey
    List<CodeStyleCheckGlobalError> globalErrorList = new ArrayList<>()
    List<CodeStyleCheckLineError> lineErrorList = new ArrayList<>()

    /**
     * Constructor.
     *
     * @param fileName The file name.
     * @param filePath The file path.
     * @param authorsKey Unique key generated by authors.
     */
    CodeStyleCheckDetailFileData(String fileName, String filePath, String authorsKey) {
        super(fileName, filePath)
        this.authorsKey = authorsKey
    }

    /**
     * Get the error count for the current file.
     *
     * @return The error count for the current file.
     */
    int getTotalErrorCount() {
        globalErrorList.size() + lineErrorList.size()
    }

    /**
     * Generate file report.
     *
     * @return The formatted report.
     */
    String getReportForAuthor() {
        int totalErrorCount = getTotalErrorCount()
        if (/*!reportContent &&*/ totalErrorCount > 0) {
            StringBuilder reportContentBuilder = new StringBuilder()
            reportContentBuilder.append("${newLink("${filePath}", fileName, fileName)} has ${totalErrorCount} error(s)")
            Closure<StringBuilder> lineBuilder = { reportContentBuilder.append(HTML_TAG_BR) }
            globalErrorList.each {
                lineBuilder().append(newHighlight(String.format(it.error, it.args)))
            }
            lineErrorList.each {
                String lineNumber = it.lineNumber.toString()
                String padding = lineNumber.padRight(PADDING_WIDTH).replace(lineNumber, '')
                lineBuilder().append(newLink("${filePath}#${lineNumber}", lineNumber, lineNumber)).append(padding)
                        .append(": ${escapeContent(it.line.trim())} &lt;= ${newHighlight(String.format(it.error, it.args))}")
            }
            reportContent = lineBuilder().toString()
        }
        reportContent
    }

    /**
     * Get global issue report.
     *
     * @param errorList Error list.
     * @return Global issue report.
     */
    static String getReportForGlobalIssue(List<CodeStyleCheckGlobalError> errorList) {
        StringBuilder reportContentBuilder = new StringBuilder()
        Closure<StringBuilder> lineBuilder = { reportContentBuilder.append(HTML_TAG_BR) }
        errorList.groupBy {
            it.fileAbsolutePath
        }.sort {
            it.key
        }.each {
            String fileName = it.key.replaceFirst('^.*[/\\\\]', '')
            it.value.each {
                String filePath = it.fileAbsolutePath
                reportContentBuilder.append(newLink("${filePath}", fileName, fileName))
                        .append(" &lt;= ${newHighlight(String.format(it.error, it.args))}")
            }
            lineBuilder()
        }
        reportContentBuilder.toString()
    }

    /**
     * Get line issue report.
     * 
     * @param errorList Error list.
     * @return Line issue report.
     */
    static String getReportForLineIssue(List<CodeStyleCheckLineError> errorList) {
        StringBuilder reportContentBuilder = new StringBuilder()
        Closure<StringBuilder> lineBuilder = { reportContentBuilder.append(HTML_TAG_BR) }
        errorList.groupBy {
            it.fileAbsolutePath
        }.sort {
            it.key
        }.each {
            String fileName = it.key.replaceFirst('^.*[/\\\\]', '')
            reportContentBuilder.append(fileName).append(HTML_TAG_BR)
            it.value.each {
                String filePath = it.fileAbsolutePath
                String lineNumber = it.lineNumber.toString()
                String padding = lineNumber.padRight(PADDING_WIDTH).replace(lineNumber, '')
                reportContentBuilder.append(newLink("${filePath}#${lineNumber}", lineNumber, lineNumber)).append(padding)
                        .append(": ${escapeContent(it.line.trim())} &lt;= ${newHighlight(String.format(it.error, it.args))}")
                lineBuilder()
            }
            lineBuilder()
        }
        reportContentBuilder.toString()
    }
}
