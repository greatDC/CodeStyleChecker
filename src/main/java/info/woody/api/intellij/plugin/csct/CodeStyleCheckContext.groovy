package info.woody.api.intellij.plugin.csct

import groovy.util.slurpersupport.GPathResult

/**
 * Context data.
 * @author Woody
 * @since 15/06/2018
 */
class CodeStyleCheckContext {

    private String sourceDir
    private GPathResult rawXml

    /**
     * Constructor.
     */
    private CodeStyleCheckContext() {}

    /**
     * Create an instance of type {@link CodeStyleCheckContext}.
     *
     * @param configurationFile Configuration file.
     * @param sourceDir Source code directory.
     * @return Context information; {@code null} if some errors happen.
     */
    static CodeStyleCheckContext newInstance(File configurationFile, String sourceDir) {
        try {
            CodeStyleCheckContext context = new CodeStyleCheckContext()
            context.rawXml = new XmlSlurper().parse(configurationFile)
            context.sourceDir = sourceDir
            String configuredSourceDir = context.rawXml.SourceDir.text()
            if (new File(configuredSourceDir).exists()) {
                context.sourceDir = configuredSourceDir
            }
            return context
        } catch (e) {
            throw e
        }
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
