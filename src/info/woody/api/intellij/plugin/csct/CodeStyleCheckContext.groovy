package info.woody.api.intellij.plugin.csct

import groovy.util.slurpersupport.GPathResult

class CodeStyleCheckContext {

    private String sourceDir
    private GPathResult rawXml

    private CodeStyleCheckContext() {}

    static CodeStyleCheckContext newInstance(File configurationFile, String sourceDir) {
        def context = new CodeStyleCheckContext()
        context.rawXml = new XmlSlurper().parse(configurationFile)
        context.sourceDir = sourceDir
        context
    }

    String MY_SOURCE_DIR() {
        this.sourceDir
    }

    List<String> FILES_TO_SKIP() {
        (this.rawXml.FileExclusion.text().replaceAll("(?i)[^a-z.\\n]", "").split("\\s*\\n\\s*") as List).findAll { return it.length() }
    }

    String FILENAME_PATTERN_TO_SKIP() {
        this.rawXml.FileExclusionPattern.text()
    }

    String GIT_FILES_TO_MERGE() {
        this.rawXml.FileInclusion.text()
    }
}
