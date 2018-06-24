package info.woody.api.intellij.plugin.csct.bean

/**
 * Report.
 *
 * @author Woody
 * @since 15/06/2018
 */
class CodeStyleCheckReport {
    CodeStyleCheckSummaryData summaryData
    CodeStyleCheckDetailData detailData
    int fileCount

    /**
     * Constructor.
     * @param summaryData Summary data.
     * @param detailData Detail data.
     * @param fileCount File count.
     */
    CodeStyleCheckReport(CodeStyleCheckSummaryData summaryData, CodeStyleCheckDetailData detailData, int fileCount) {
        this.summaryData = summaryData
        this.detailData = detailData
        this.fileCount = fileCount
    }

    /**
     * Create authors key from the given author list.
     * @param authors Author list.
     * @return Constructed authors key.
     */
    String createAuthorsKey(List<String> authors) {
        authors?.sort { it }.join(", ")
    }
}
