package info.woody.api.intellij.plugin.csct.bean

/**
 * Global error bean.
 * @author Woody
 * @since 20/06/2018
 */
class CodeStyleCheckGlobalError {
    String error
    String[] args

    /**
     * Constructor.
     *
     * @param error Error message.
     * @param args Error arguments.
     */
    CodeStyleCheckGlobalError(String error, String[] args) {
        this.error = error
        this.args = args
    }
}
