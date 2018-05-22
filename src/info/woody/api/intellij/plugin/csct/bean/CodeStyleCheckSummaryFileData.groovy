package info.woody.api.intellij.plugin.csct.bean

class CodeStyleCheckSummaryFileData {
    String fileName
    String filePath

    CodeStyleCheckSummaryFileData(String fileName, String filePath) {
        this.fileName = fileName
        this.filePath = filePath
    }
}
