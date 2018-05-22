package info.woody.api.intellij.plugin.csct.bean

class CodeStyleCheckDetailFileData extends CodeStyleCheckSummaryFileData {
    private String reportContent = ""
    String authorsKey
    List<CodeStyleCheckGlobalError> globalErrorList = new ArrayList<>()
    List<CodeStyleCheckLineError> lineErrorList = new ArrayList<>()

    CodeStyleCheckDetailFileData(String fileName, String filePath, authorsKey) {
        super(fileName, filePath)
        this.authorsKey = authorsKey
    }

    int getErrorCount() {
        globalErrorList.size() + lineErrorList.size()
    }

    String getReportContent() {
        def errorCount = getErrorCount()
        if (/*!reportContent &&*/ errorCount > 0) {
            StringBuilder reportContentBuilder = new StringBuilder()
            reportContentBuilder.append("<a href='${this.filePath}' title='${this.filePath}'>${this.fileName}</a> has ${errorCount} error(s)")
            globalErrorList.each {
                reportContentBuilder.append("<br>").append("<font color=yellow>${String.format(it.error, it.args)}</font>")
            }
            lineErrorList.each {
                reportContentBuilder.append("<br>").append(String.valueOf(it.lineNumber).padRight(5)
                        .concat(it.line).concat("\t&lt;==\t<font color=yellow>${String.format(it.error, it.args)}</font>"))
            }
            reportContent = reportContentBuilder.toString()
        }
        reportContent
    }
}
