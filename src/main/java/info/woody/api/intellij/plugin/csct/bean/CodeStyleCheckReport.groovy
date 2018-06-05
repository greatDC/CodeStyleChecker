package info.woody.api.intellij.plugin.csct.bean

class CodeStyleCheckReport {
    CodeStyleCheckSummaryData summaryData
    CodeStyleCheckDetailData detailDetailData

    CodeStyleCheckReport(CodeStyleCheckSummaryData summaryData, CodeStyleCheckDetailData detailDetailData) {
        this.summaryData = summaryData
        this.detailDetailData = detailDetailData
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
