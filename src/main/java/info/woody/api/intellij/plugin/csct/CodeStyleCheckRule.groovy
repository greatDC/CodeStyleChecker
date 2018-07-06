package info.woody.api.intellij.plugin.csct

import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDetailData
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDetailFileData
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckGlobalError
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckLineError
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReport
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckSummaryData
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckSummaryFileData
import groovy.io.FileType

import java.util.regex.Pattern

/**
 * <p>http://www.oracle.com/technetwork/java/codeconvtoc-136057.html</p>
 *
 * @author Woody
 * @since 01/06/2018
 */
abstract class CodeStyleCheckRule {

    public String MY_SOURCE_DIR = "" // source code folder
    public List<String> FILES_TO_SKIP = []
    public String FILENAME_PATTERN_TO_SKIP = '^.*(Controller).*$'
    // below file list could be created by git command 'git diff --name-only branch1 branch2'
    // Sample: "git diff --name-only HEAD origin/SPRINT_BOEING_727 | grep -e java$ -e groovy$"
    public String GIT_FILES_TO_MERGE = ''
    /**
     * Key is file absolute path and value is file detail of Map type.
     */
    protected Map STATISTICS_ALL_IN_ONE = [:]
    protected boolean IS_DEBUG = false
    protected int ALL_ISSUE_COUNT = 0
    protected int ALL_FILE_COUNT = 0
    protected List ALL_FILES = []
    protected List ALL_FILES_NAME = []
    protected List<File> TARGET_FILES = []
    protected Map STATISTICS_TYPE_REPORT = [:]
    protected Map STATISTICS_FILE_REPORT = [:]
    protected int FILE_ISSUE_COUNT = 0
    protected String PROD_FILE_NAME = null
    protected String PROD_FILE_ABSOLUTE_PATH = null
    protected int LINE_NUMBER = 0
    protected Map LINE_META
    protected List AUTHORS
    protected StringBuilder outputBuilder = new StringBuilder(9999)
    protected boolean ENABLE_CONSOLE_REPORT = false

    /**
     * Check the code smells.
     *
     * @return Issues report.
     */
    CodeStyleCheckReport doCheck() {
        File dir = new File(MY_SOURCE_DIR?:"")
        if (!dir.exists() || dir.isFile()) {
            throw new CodeStyleCheckException("`SourceDir` in configuration file has to be a valid file path: " + dir.getAbsolutePath());
        }
        Pattern patternFileNameToSkip = Pattern.compile(FILENAME_PATTERN_TO_SKIP?:"");
        List<String> filesToSkip = FILES_TO_SKIP ?: []
        List<String> gitFilesToMerge = (GIT_FILES_TO_MERGE ?: "").split('(?s)\\r?\\n').toList().collect {
            it.replaceAll('^.*[/\\\\]', '')
        }
        dir.eachFileRecurse(FileType.FILES) { File file ->
            String fileName = file.name
            if (fileName.endsWith("java") || fileName.endsWith("groovy")) {
                ALL_FILES << file
                ALL_FILES_NAME << fileName
                if (!filesToSkip.contains(fileName) && (gitFilesToMerge.contains(fileName)) &&
                        !fileName.matches(patternFileNameToSkip)) {
                    TARGET_FILES << file
                }
            }
        }
        TARGET_FILES.eachWithIndex { File file, int index ->
            findPotentialIssues(file, index + 1)
        }
        if (ENABLE_CONSOLE_REPORT) {
            printGlobalResult()
        }
        this.calculateStatistics()
    }

    /**
     * Check the file to report the found issues.
     *
     * @param file File to check.
     * @param fileNumber File number.
     */
    protected abstract void findPotentialIssues(File file, fileNumber);

    /**
     * Print the global warning message.
     *
     * @param error The error message.
     * @param args The arguments for error message template.
     */
    protected void printGlobalWarning(String error, String... args) {
        FILE_ISSUE_COUNT++
        if (ENABLE_CONSOLE_REPORT) {
            __println error
            if (STATISTICS_TYPE_REPORT[error]) STATISTICS_TYPE_REPORT[error] += 1
            else STATISTICS_TYPE_REPORT[error] = 1
            if (STATISTICS_FILE_REPORT[PROD_FILE_NAME]) {
                STATISTICS_FILE_REPORT[PROD_FILE_NAME]['ERRORS'] += 1
            } else {
                STATISTICS_FILE_REPORT[PROD_FILE_NAME] = [AUTHORS: AUTHORS, PROD_FILE_ABSOLUTE_PATH: PROD_FILE_ABSOLUTE_PATH, ERRORS: 1]
            }
        }
        // statistics all in one
        ALL_ISSUE_COUNT++
        CodeStyleCheckGlobalError globalError = new CodeStyleCheckGlobalError(error, args, PROD_FILE_ABSOLUTE_PATH)
        if (STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH]) {
            STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH]['GLOBAL_ERRORS'] << globalError
        } else if (!STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH]) {
            STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH] = [
                    AUTHORS                : AUTHORS,
                    PROD_FILE_NAME         : PROD_FILE_NAME,
                    PROD_FILE_ABSOLUTE_PATH: PROD_FILE_ABSOLUTE_PATH,
                    GLOBAL_ERRORS          : [globalError],
                    LINE_ERRORS            : []
            ]
        }
    }

    /**
     * Print the warning message.
     *
     * @param line The line content containing smells.
     * @param lineNumber The line number.
     * @param error The error message.
     * @param args The arguments for error message template.
     */
    protected void printWarning(String line, int lineNumber, String error, String... args) {
        FILE_ISSUE_COUNT++
        if (ENABLE_CONSOLE_REPORT) {
            __println String.valueOf(lineNumber).padRight(5).concat(line).concat("\t&lt;===\t${error}")
            if (STATISTICS_TYPE_REPORT[error]) STATISTICS_TYPE_REPORT[error] += 1
            else STATISTICS_TYPE_REPORT[error] = 1
            if (STATISTICS_FILE_REPORT[PROD_FILE_NAME]) {
                STATISTICS_FILE_REPORT[PROD_FILE_NAME]['ERRORS'] += 1
            } else {
                STATISTICS_FILE_REPORT[PROD_FILE_NAME] = [AUTHORS: AUTHORS, PROD_FILE_ABSOLUTE_PATH: PROD_FILE_ABSOLUTE_PATH, ERRORS: 1]
            }
        }
        // statistics all in one
        ALL_ISSUE_COUNT++
        CodeStyleCheckLineError lineError = new CodeStyleCheckLineError(line, lineNumber, error, args, PROD_FILE_ABSOLUTE_PATH)
        if (STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH]) {
            STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH]['LINE_ERRORS'] << lineError
        } else if (!STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH]) {
            STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH] = [
                    AUTHORS                : AUTHORS,
                    PROD_FILE_NAME         : PROD_FILE_NAME,
                    PROD_FILE_ABSOLUTE_PATH: PROD_FILE_ABSOLUTE_PATH,
                    GLOBAL_ERRORS          : [],
                    LINE_ERRORS            : [lineError]
            ]
        }
    }

    /**
     * Calculate statistics.
     * @return Instance of {@link CodeStyleCheckReport}.
     */
    private CodeStyleCheckReport calculateStatistics() {
        CodeStyleCheckSummaryData summaryData = new CodeStyleCheckSummaryData()
        CodeStyleCheckDetailData detailData = new CodeStyleCheckDetailData()
        CodeStyleCheckReport report = new CodeStyleCheckReport(summaryData, detailData, ALL_FILE_COUNT)
        STATISTICS_ALL_IN_ONE.each {
            Map fileDetailMap = it.value
            List authors = fileDetailMap.get('AUTHORS')
            String authorsKey = report.createAuthorsKey(authors)
            summaryData.authorsKeySet.add(authorsKey)
            String fileName = fileDetailMap.get('PROD_FILE_NAME')
            String filePath = it.key
            summaryData.fileDataList << new CodeStyleCheckSummaryFileData(fileName, filePath)
            List globalErrors = fileDetailMap.get('GLOBAL_ERRORS')
            if (globalErrors) summaryData.globalErrorsGroupByFilePath.put(filePath, globalErrors)
            List lineErrors = fileDetailMap.get('LINE_ERRORS')
            if (lineErrors) summaryData.lineErrorsGroupByFilePath.put(filePath, lineErrors)
            summaryData.fileCountWithIssues++
            CodeStyleCheckDetailFileData detailFileData = new CodeStyleCheckDetailFileData(fileName, filePath, authorsKey)
            detailFileData.globalErrorList = globalErrors
            detailFileData.lineErrorList = lineErrors
            detailData.fileDataList << detailFileData
            detailData.mapAuthorsErrors.put(authorsKey, 0)
        }

        [summaryData.globalErrorsGroupByFilePath, summaryData.lineErrorsGroupByFilePath].each {
            it.each {
                String filePath = it.key
                List errors = it.value
                errors.each {
                    String error = it.error
                    if (summaryData.filesGroupByError.get(error)) {
                        summaryData.filesGroupByError.get(error) << filePath
                    } else {
                        summaryData.filesGroupByError.put(error, [filePath])
                    }
                }
            }
        }

        detailData.fileDataList.each {
            int totalErrorCountInEachFile = it.getTotalErrorCount()
            if (totalErrorCountInEachFile > 0) {
                if (detailData.mapAuthorsErrors.containsKey(it.authorsKey)) {
                    int totalErrorCount = detailData.mapAuthorsErrors.get(it.authorsKey)
                    detailData.mapAuthorsErrors.put(it.authorsKey, totalErrorCount + totalErrorCountInEachFile)
                }
            }
        }

        summaryData.filesGroupByError = summaryData.filesGroupByError.sort { 0 - it.value.size() }

        report
    }

    /**
     * Print file summary.
     */
    protected void printFileResult() {
        __println "-" * 50
        if (0 == FILE_ISSUE_COUNT) {
            __print "Well done :)\t\t\t"
        } else {
            __print "Oops T_T\t\t\t"
        }
        __println "FILE_ISSUE_COUNT: ${FILE_ISSUE_COUNT}"
    }

    /**
     * Print report summary.
     */
    protected void printGlobalResult() {
        __println "\n" * 5
        __println "-" * 50
        __println "TOTAL FILES: ${TARGET_FILES.size()}, ALL FOUND ISSUES: ${ALL_ISSUE_COUNT}"
        __println "-" * 50
        STATISTICS_TYPE_REPORT.sort { 0 - it.value }.each { __println "${it.value.toString().padLeft(3)} errors for [${it.key}]" }
        __println()
        Map<String, Map<String, Object>> ranking = [:]
        STATISTICS_FILE_REPORT.each {
            String authors = it.value['AUTHORS'].sort { it.toString() }
            if (ranking[authors]) {
                ranking[authors]['ERRORS'] += it.value['ERRORS']
                ranking[authors]['FILES'] << it.key
            } else {
                ranking[authors] = [ERRORS: it.value['ERRORS'], FILES: [it.key]]
            }
        }
        ranking.sort { 0 - it.value['ERRORS'] }.each {
            __println "-" * 50
            __println "${it.key} delivered ${it.value['ERRORS']} code smell(s) in below files:"
            __println "-" * 50
            it.value['FILES'].each { __println it }
            __println()
        }
        null
    }

    /**
     * Replace all string literals with empty string.
     *
     * @param line The current line text.
     * @return The current line text without string literals.
     */
    protected static String stripStringPattern(line) {
        line.replaceAll('"(\\\\"|[^"])+?"', '""').replaceAll("'(\\\\'|[^'])+?'", "''")
    }

    /**
     * Enable debug info.
     *
     * @param topic The topic info for debug.
     * @return True when debug is enabled, otherwise false.
     */
    protected boolean debug(topic) {
        if (IS_DEBUG) {
            println "${LINE_NUMBER}: ${topic}"
            __println "${LINE_NUMBER}: ${topic}"
        }
        true
    }

    /**
     * Output the text and start a new line.
     *
     * @param text Text to output.
     */
    protected void __println(String text) {
        __print((null == text ? "" : text) + "\n")
    }

    /**
     * Output the text.
     *
     * @param text Text to output.
     */
    protected void __print(text) {
        outputBuilder.append(text)
    }
}
