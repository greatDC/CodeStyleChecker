package info.woody.api.intellij.plugin.csct.bean

class CodeStyleCheckLineError extends CodeStyleCheckGlobalError {
    String line
    int lineNumber

    CodeStyleCheckLineError(String line, int lineNumber, String error, String[] args) {
        super(error, args)
        this.line = line
        this.lineNumber = lineNumber
    }
}
