package info.woody.api.intellij.plugin.csct.bean

class CodeStyleCheckReport {
    CodeStyleCheckSummaryData summaryData
    CodeStyleCheckDetailData detailDetailData

    CodeStyleCheckReport(CodeStyleCheckSummaryData summaryData, CodeStyleCheckDetailData detailDetailData) {
        this.summaryData = summaryData
        this.detailDetailData = detailDetailData
    }

    String createAuthorsKey(List<String> authors) {
        authors?.sort { it }.join(", ")
    }
}
