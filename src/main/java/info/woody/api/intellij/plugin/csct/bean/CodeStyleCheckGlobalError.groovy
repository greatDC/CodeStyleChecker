package info.woody.api.intellij.plugin.csct.bean

/**
 * Global error bean.
 * @author Woody
 * @since 20/06/2018
 */
class CodeStyleCheckGlobalError {
    String error
    String[] args
    String fileAbsolutePath

    /**
     * Constructor.
     *
     * @param error Error message.
     * @param args Error arguments.
     * @param fileAbsolutePath File absolute path.
     */
    CodeStyleCheckGlobalError(String error, String[] args, String fileAbsolutePath) {
        this.error = error
        this.args = args
        this.fileAbsolutePath = fileAbsolutePath
    }
}
