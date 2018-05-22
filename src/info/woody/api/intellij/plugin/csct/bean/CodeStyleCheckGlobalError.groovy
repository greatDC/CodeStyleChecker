package info.woody.api.intellij.plugin.csct.bean

class CodeStyleCheckGlobalError {
    String error
    String[] args
    CodeStyleCheckGlobalError(String error, String[] args) {
        this.error = error
        this.args = args
    }
}
