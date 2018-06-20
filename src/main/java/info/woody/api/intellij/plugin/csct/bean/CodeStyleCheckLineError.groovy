package info.woody.api.intellij.plugin.csct.bean

/**
 * Line error bean.
 * @author Woody
 * @since 20/06/2018
 */
class CodeStyleCheckLineError extends CodeStyleCheckGlobalError {
    String line
    int lineNumber

    /**
     * Constructor.
     *
     * @param line Line content.
     * @param lineNumber Line number.
     * @param error Error message.
     * @param args Error arguments.
     */
    CodeStyleCheckLineError(String line, int lineNumber, String error, String[] args) {
        super(error, args)
        this.line = line
        this.lineNumber = lineNumber
    }
}
