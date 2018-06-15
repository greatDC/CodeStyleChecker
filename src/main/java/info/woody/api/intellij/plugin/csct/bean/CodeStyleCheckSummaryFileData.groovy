package info.woody.api.intellij.plugin.csct.bean

/**
 * Summary info for scanned issues.
 *
 * @author Woody
 */
class CodeStyleCheckSummaryFileData {
    String fileName
    String filePath

    /**
     * @param fileName File name.
     * @param filePath File path.
     */
    CodeStyleCheckSummaryFileData(String fileName, String filePath) {
        this.fileName = fileName
        this.filePath = filePath
    }
}
