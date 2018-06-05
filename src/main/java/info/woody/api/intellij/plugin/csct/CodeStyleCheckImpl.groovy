package info.woody.api.intellij.plugin.csct

import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDetailData
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDetailFileData
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckGlobalError
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckLineError
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReport
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckSummaryData
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckSummaryFileData
import groovy.io.FileType

/**
 *
 * http://www.oracle.com/technetwork/java/codeconvtoc-136057.html
 *
 * @author Woody
 */
class CodeStyleCheckImpl {
    public String MY_SOURCE_DIR = "C:\\workstation\\workspace\\git\\gitOyster\\tRetailAPI\\src"
    public List<String> FILES_TO_SKIP = ("""
ConfigService.java
""".replaceAll("(?i)[^a-z.\\n]", "").split("\\s*\\n\\s*") as List).findAll { return it.length() }
    public String FILENAME_PATTERN_TO_SKIP = '^.*(Controller).*$'
/* below file list could be created by git command 'git diff --name-only branch1 branch2' */
/* git diff --name-only HEAD origin/SPRINT_BOEING_727 | grep -e java$ -e groovy$ */
    public String GIT_FILES_TO_MERGE = '''
src/main/java/com/openjaw/api/WebApplicationConfig.java
'''
    /**
     * Key is file absolute path and value is file detail of Map type.
     */
    private Map STATISTICS_ALL_IN_ONE = [:]
    private boolean IS_DEBUG = false
    private int ALL_ISSUE_COUNT = 0
    private int ALL_FILE_COUNT = 0
    private List ALL_FILES = []
    private List ALL_FILES_NAME = []
    private List TARGET_FILES = []
    private Map STATISTICS_TYPE_REPORT = [:]
    private Map STATISTICS_FILE_REPORT = [:]
    private int FILE_ISSUE_COUNT = 0
    private String PROD_FILE_NAME = null
    private String PROD_FILE_ABSOLUTE_PATH = null
    private int LINE_NUMBER = 0
    private Map LINE_META
    private List AUTHORS
    private StringBuilder outputBuilder = new StringBuilder(9999)

    /**
     * Check the code smells.
     *
     * @return Instance of current class.
     */
    CodeStyleCheckImpl doCheck() {
        File dir = new File(MY_SOURCE_DIR)
        dir.eachFileRecurse(FileType.FILES) { File file ->
            String fileName = file.name
            if (fileName.endsWith("java") || fileName.endsWith("groovy")) {
                ALL_FILES << file
                ALL_FILES_NAME << fileName
                if (!FILES_TO_SKIP.contains(fileName) && GIT_FILES_TO_MERGE.contains("/".concat(fileName)) &&
                        !fileName.matches(FILENAME_PATTERN_TO_SKIP)) {
                    TARGET_FILES << file
                }
            }
        }
        TARGET_FILES.eachWithIndex { File file, int index ->
            findPotentialIssues(file, index + 1)
        }
        this
    }

    /**
     * Check the file to report the found issues.
     *
     * @param file File to check.
     * @param fileNumber File number.
     */
    void findPotentialIssues(File file, fileNumber) {
        LINE_NUMBER = 0
        LINE_META = [:]
        FILE_ISSUE_COUNT = 0
        PROD_FILE_NAME = file.name
        ALL_FILE_COUNT++
        debug(PROD_FILE_NAME)
        String TEST_FILE_NAME = file.name.replaceAll('[.](java|groovy)', 'Test.$1')
        boolean isTest = PROD_FILE_NAME.toLowerCase().contains("test") || file.path.matches('^.*src(/|\\\\)test\\1.*$')
        String content = file.getText('UTF-8')
        String[] lines = content.split("""\r?\n""")
        __println "\n" * 3 + '*' * 50
        PROD_FILE_ABSOLUTE_PATH = file.getAbsolutePath()
        __println "${fileNumber}. ".padLeft(5, '0')
                .concat("<a name='file' href='${PROD_FILE_ABSOLUTE_PATH}'>${PROD_FILE_NAME}</a> from ${PROD_FILE_ABSOLUTE_PATH}")
        __println '*' * 50
        AUTHORS = []
        for (String line : lines) {
            boolean isDocPattern = line.trim().startsWith("*")
            if (isDocPattern && line.matches('^(?i).*created? by ([\\w.0]+).*$')) { // extract author from comment
                AUTHORS << line.replaceAll('^(?i).*created? by ([\\w.0]+).*$', '$1').trim()
            } else if (isDocPattern && line.contains('@author')) { // extract author from documentation
                AUTHORS << line.replaceAll('^(?i).*@author (.*)$', '$1').trim()
            }
        }
        if (!AUTHORS) {
            AUTHORS << "Anonymous"
        }
        if (content.matches('(?s)^.*(\\s*\\r?+\\n){3}.*$')) {
            printGlobalWarning "MORE THAN ONE EMPTY LINE WERE FOUND"
        }
        if (content.matches('(?s)^.*\\r?\\n\\s*\\r?\\n[}].*$')) {
            printGlobalWarning "EMPTY LINE WAS FOUND AT THE END OF LAST METHOD DEFINITION"
        }
        if (content.contains('final static')) {
            printGlobalWarning '"static final", but not "final static"'
        }
        if (content.charAt(content.length() - 1) != '\n') {
            printGlobalWarning 'LAST LINE SHOULD BE EMPTY.'
        }
        if (!isTest && content.split("""\r?\n""").length > 500) {
            printGlobalWarning 'CLASS LINES ARE MORE THAN 500.'
        }
        if (content.toLowerCase().contains(' todo ') || content.toLowerCase().contains(' fixme ')) {
            printGlobalWarning 'TODO/FIXME should be fixed ASAP.'
        }
        if (isTest) {
            int posRule = content.indexOf('@Rule')
            int posSpy = content.indexOf('@Spy')
            int posFirstMock = content.indexOf('@Mock')
            int posLastMock = content.lastIndexOf('@Mock')
            int posInjectMocks = content.indexOf('@InjectMocks')
            boolean isCorrectOrder = true
            for (List posPair : [[posRule, posSpy], [posSpy, posFirstMock], [posFirstMock, posLastMock], [posLastMock, posInjectMocks]]) {
                if ((posPair[0] != -1 && posPair[1] != -1) && (posPair[0] > posPair[1])) {
                    isCorrectOrder = false
                    break
                }
            }
            if (!isCorrectOrder) {
                printGlobalWarning 'Please organize fields order by @Rule, @Spy, @Mock, @InjectMocks'
            }
        }
        if (PROD_FILE_NAME.endsWith(".java") || PROD_FILE_NAME.endsWith(".groovy")) {
            List testFile = ALL_FILES.find { TEST_FILE_NAME == it.name }
            if (testFile) {
                String regexErrorCode = 'ErrorCodes[.][A-Z0-9_]+'
                List missingErrorCodes =
                        content.findAll(regexErrorCode).unique() - testFile.getText('UTF-8').findAll(regexErrorCode).unique()
                if (missingErrorCodes) {
                    String missingErrorCodesText = ""
                    missingErrorCodes.each { missingErrorCodesText += """\n\t- $it""" }
                    printGlobalWarning("YOU MISSED ASSERT FOR BELOW ERRORS:%s", missingErrorCodesText)
                }
            }
        }

        int totalLineCount = lines.length
        lines.eachWithIndex { line, index ->
            LINE_NUMBER = index + 1
            LINE_META = [:]

            String trimmedLine = line.trim()
            String secureLine = stripStringPattern(line)
            String trimmedSecureLine = secureLine.trim()
            int trimmedLineLength = trimmedLine.length()
            int lineLength = line.length()

            if (lineLength > 234) {
                printWarning(line, LINE_NUMBER, "The line is too long to be checked, please wrap properly then check again.")
                return
            }

            if (debug('DOCUMENTATION') && trimmedLine.matches('^\\s*+/?[*].*$')) {
                LINE_META.DOCUMENTATION = true
                String originLine = line
                line = trimmedLine.replaceAll("<[^>]+>", "")
                if (line.matches("[ */]*")) {
                } else if (line.contains('@author') || line.contains('@see')) {
                } else if (line.contains('@since')) {
                    if (!line.matches('''^.*\\b([012][0-9]|30|31)/(0[1-9]|1[0-2])/201[0-9]\\b.*$''')) {
                        printWarning(line, LINE_NUMBER, "Date format should be dd/mm/yyyy.")
                    }
                } else if (!trimmedLine.replaceFirst('^[^*]*[*] ([^@]*((@param|@throws) \\w+|@return))?', '')
                        .matches('^\\s*[0-9A-Z{].*[.:,;!?]$')) {
                    printWarning(originLine, LINE_NUMBER, "Documentation should start with a capital letter and end with '.:,;!?'")
                }
                if (line.replaceAll('[{][^}]+[}]', '').replaceAll('(Chinese|International)', '')
                        .matches('^.*@\\w+\\s+\\w+\\s+.*[A-Z][a-z].*[A-Z][a-z].*$')) {
                    printWarning(originLine, LINE_NUMBER,
                            "Only first letter is allowed to be a capital unless it's a proper noun. ".concat(
                                    "Please try to use {@link ...} or {@code ...} if it refers to code."))
                }
            } else if (debug('SINGLE LINE COMMENT') && line.matches('^\\s*//.*$')) {
                LINE_META.COMMENT = true
                if (trimmedLine.matches('^\\s*//[^ ].+$')) {
                    printWarning(line, LINE_NUMBER, "The single line comment should be formatted as '// comment content'.")
                }
            } else if (debug('SINGLE { IN A LINE') && line.matches('^\\s*[{]\\s*$')) {
                printWarning(line, LINE_NUMBER, "'{' cannot occupy a single line.")
            } else if (debug('IMPORT STATEMENT') && trimmedLine.startsWith('import ')) {
                LINE_META.IMPORT = true
                if (line.matches('^\\s*import\\b.+[*].*$')) {
                    printWarning(line, LINE_NUMBER, "The asterisk '*' was found in import statement.")
                }
                String importedKeyword = line.replaceAll('^.+[.](\\w+)\\W?$', '$1')
                if (!line.contains('*') && !content.replaceFirst('(?s)\\b' + importedKeyword + '\\b', '')
                        .matches('(?s)^.*\\b' + importedKeyword + '\\b.*$')) {
                    printWarning(line, LINE_NUMBER, "The item is imported but never used.")
                }
            } else if (debug('CHECK NullPointerException') &&
                    line.matches('^.*[.](equals|equalsIgnoreCase)[(]([^)]+[.])?[A-Z_]{2,}[)].*$')) {
                printWarning(line, LINE_NUMBER, "Please use const as left value to avoid NullPointerException.")
            } else if (debug('ENUM COMPARISON') && !LINE_META.FIELD && line.matches('^.+\\b\\w+Enum[.][A-Z_]+[.]equals.+$')) {
                printWarning(line, LINE_NUMBER, "use '==' instead of equals for enum comparison.")
            } else if (debug('FIELD') && line.matches('^\\s*(private|protected|public)[^<{(]+$') &&
                    !line.matches('^.*\\b(class|interface|enum)\\b.*$')) {
                LINE_META.FIELD = true
                String fieldName = line.replaceAll('=.*$', '').trim().replaceAll('^.*\\b(\\w+)[\\s;]*$', '$1')
                if (debug('UNUSED FIELD') && lines[index - 1].trim() != "@Mock" &&
                        PROD_FILE_NAME.matches('^.+(Impl|Service|Validator|Mapper|Process|Util|Interceptor|Helper).*$') &&
                        !trimmedLine.startsWith("public") &&
                        (!(lines as List).subList(index + 1, lines.size() - 1).join('\n').matches('(?s)^.*\\b' + fieldName + '\\b.*$') // field never appears after declaration
                                || (content.contains("${fieldName} = ${fieldName}") && // don't check @Mock field
                                !content.substring(content.indexOf("${fieldName} = ${fieldName}") + fieldName.length() * 2 + 3).matches('(?s)^.*\\b' + fieldName + '\\b.*$')))) {
                    printWarning(line, LINE_NUMBER, "You have unused field declaration")
                }
                if (debug('PRIVATE FIELD') && isTest && line.contains("protected ")) {
                    printWarning(line, LINE_NUMBER, "The field in unit test should be private.")
                }
                if (debug('REFERENCE FIELD') && !isTest && !line.contains("LOGGER")) {
                    if (PROD_FILE_NAME.contains("Controller.java")) {
                        if (!line.contains("private")) {
                            printWarning(line, LINE_NUMBER, "You might need private if it's not referred outside.")
                        }
                    } else {
                        if (!line.contains("protected") && !line.contains("public")) {
                            printWarning(line, LINE_NUMBER, "You might need protected if it's not referred outside.")
                        }
                    }
                }
                if (debug('NAMING') && (line.contains("Validator ") || line.contains("Service ") || line.contains("Converter ")) &&
                        !line.toLowerCase().matches('^.*\\b(\\w+(validator|service|converter)\\b) \\1.*$')) {
                    printWarning(line, LINE_NUMBER, "Name pattern should follow 'FullClassName fullClassName;'.")
                }
                if (debug('CHECK LOGGER') && line.contains(" logger ")) {
                    printWarning(line, LINE_NUMBER, "Please use LOGGER.")
                }
                if (debug('STATIC') && line.contains(" static ")) {
                    if (!line.contains(" final ")) {
                        printWarning(line, LINE_NUMBER, "Do you miss the keyword 'final' or have redundant keyword 'static'?")
                    } else if (line.matches('(?i)^.* (string|boolean|int|integer|float|long)\\b.*$') &&
                            fieldName != fieldName.toUpperCase()) {
                        printWarning(line, LINE_NUMBER, "All letters in const should be uppercase.")
                    }
                }
            } else if (debug('CLASS') && line.matches('^.*(private|protected|public)?[^(]*(class|interface|enum)[^(.\'="]*$')) {
                LINE_META.CLASS = true
                if (!isTest && PROD_FILE_NAME.matches('^.+(Impl|Service|Validator|Mapper|Process|Util).*$') &&
                        !line.contains("interface") && !line.contains("abstract") && !line.startsWith("Base") &&
                        !ALL_FILES_NAME.contains(PROD_FILE_NAME.replaceAll('.(groovy|java)$', 'Test.java')) &&
                        !ALL_FILES_NAME.contains(PROD_FILE_NAME.replaceAll('.(groovy|java)$', 'Test.groovy'))) {
                    printWarning(line, LINE_NUMBER, 'No unit test?!')
                }
                if (!(lines as List).subList(0, index).any { it.trim().startsWith("*") }) {
                    printWarning(line, LINE_NUMBER, "Do you have documentation for this class/interface/enum?")
                }
            } else if (debug('METHOD') && trimmedLine.matches('^.*\\w+(\\W{2}|<[^>]+>)? [_a-z]\\w+[(].+[{]$')) {
                LINE_META.METHOD = true
                if (LINE_NUMBER > 3) {
                    if (!lines[index - 1].contains("@Override") && !lines[index - 2].contains("*")) {
                        printWarning(line, LINE_NUMBER, "Do you have documentation for this method?")
                    }
                }
                String methodName = trimmedLine.replaceAll('^.*\\w+(\\W{2}|<[^>]+>)? ([a-z]\\w+)[(].+[{]$', '$2')
                if ((line.contains('private ') || line.contains('protected ')) &&
                        !content.replaceFirst('\\b' + methodName + '\\b', '').contains(methodName)) {
                    printWarning(line, LINE_NUMBER, "This method is never used.")
                }
            }
            if (debug('LITERAL') && !LINE_META.FIELD && !isTest && trimmedLineLength // exclude the field declaration
                    && !(PROD_FILE_NAME.contains('RequestService')
                    && PROD_FILE_NAME.endsWith('.groovy')) // request service written by Groovy
                    && !content.contains("interface ") // interface
                    && !line.contains(" static ") // static field
                    && !line.contains(" http:// ") // namespace
                    && !trimmedLine.startsWith('*') && !trimmedSecureLine.startsWith('//') // comment
                    && !trimmedLine.startsWith('@') // annotation
            ) {
                if (line.matches('(?i)^.+?("[a-z0-9.]+"|"[-+*/;.]+").*$') // string pattern only
                        || secureLine.replaceAll('\\[\\d+\\]', '') // remove string pattern and index pattern
                        .matches('^.*\\b([2-9]|[1-9]\\d+|\\d+[.]\\d+)\\b.*$') // number pattern only
                ) {
                    printWarning(line, LINE_NUMBER, "Literal could be extracted as a const.")
                }
            }
            if (debug('CODE STYLE') && !LINE_META.COMMENT && !LINE_META.DOCUMENTATION /* Not comments */
                    && !line.matches('^.*@\\w+.*$') /* Not annotation pattern */
                    && trimmedSecureLine.matches('^.*\\b((if|while|for|switch)[(]|([)]|try|else|catch|finally|\\w+)[{]|[}](else|catch|finally))\\b.*$')) {
                printWarning(line, LINE_NUMBER, "Is this line formatted well?")
            }
            if (debug('NAMING CONSTRAINT') && line.matches('^.*\\b(\\w+(Str(ing)?|Redis)|(str|redis)[A-Z]\\w+)\\s*=.*$')) {
                printWarning(line, LINE_NUMBER, "String or str or redis is a bad naming pattern for business logic process.")
            }
            if (debug('REQUESTPROPERTIES') && trimmedSecureLine.contains('"requestProperties"')) {
                printWarning(line, LINE_NUMBER, '"requestProperties" could be replaced by RequestParameters.REQUESTPROPERTIES.')
            }
            if (trimmedLine.startsWith("def ")) {
                printWarning(line, LINE_NUMBER, "Remove 'def' and use an explicit type.")
            }
            if (!LINE_META.COMMENT && !LINE_META.DOCUMENTATION && trimmedSecureLine.matches('^.*\\bprint(ln)?\\b.+$')) {
                printWarning(line, LINE_NUMBER, "Remove 'print' in your code.")
            }
            if (140 < lineLength && lineLength < 234 && !PROD_FILE_NAME.contains("Controller.java") &&
                    lineLength - secureLine.length() < 100) {
                printWarning(line, LINE_NUMBER, "This line exceeds 140 chars")
            }
            if (trimmedLine == ')' && lines[index - 1].trim() == '}') {
                printWarning(line, LINE_NUMBER, "Could previous line and this line be merged???")
            }
            if (debug('MERGE LINES') && !isTest && index > 1 && lines[index - 1].trim().length() > 0 && trimmedLineLength > 0 &&
                    (!line.matches('^.*\\b(public|protected|private)\\b.*$') &&
                            !line.endsWith("{") && !lines[index - 1].endsWith("{") && // method
                            !line.contains("*") && !lines[index - 1].contains("*") && // multiline comment
                            !line.contains("@") && !lines[index - 1].contains("@") && // annotation
                            !line.contains("//") && !lines[index - 1].contains("//") && // single line comment
                            !(line.endsWith(";") && lines[index - 1].endsWith(";")) && // two statements
                            !lines[index - 1].endsWith(";") && // previous line is statement
                            !line.endsWith("}") && !lines[index - 1].endsWith("}") && // end curly brace
                            !lines[index - 1].endsWith(":") && // colon
                            !line.matches('^\\s+break;?$') && // break statement
                            !lines[index - 1].trim().endsWith('->') && // Groovy's closure
                            !lines[index - 1].trim().contains('<<') && // Groovy's list operation
                            !lines[index - 1].trim().endsWith('++') && // Groovy's ++
                            !lines[index - 1].trim().endsWith(')') && // Groovy's method invocation
                            (!lines[index - 1].contains(":") && !line.contains(":")) && // Groovy's map
                            !trimmedLine.startsWith(".") && // method of pipeline pattern
                            !line.contains("import ") && !lines[index - 1].contains("import ") && // import statement
                            !line.contains(" = ") && !lines[index - 1].contains(" = ") && // assignment statement
                            !line.matches('^.*[\\w<>]+ \\w+.*$') // skip method parameter declaration
                    ) && (lines[index - 1] + trimmedLine).length() < 140) {
                if (!PROD_FILE_NAME.endsWith("groovy") || !(line.endsWith(")") && lines[index - 1].endsWith(")"))) {
                    printWarning(line, LINE_NUMBER, "Could previous line and this line be merged?")
                }
            }
            if (debug('ENUM IMPORTING') && !LINE_META.FIELD && !LINE_META.COMMENT && !LINE_META.DOCUMENTATION &&
                    secureLine.matches('^.*\\b[A-Z]\\w+[.]\\w+Enum.*$') && !line.contains("import ")) {
                printWarning(line, LINE_NUMBER, "Import enum type directly, e.g GenderEnum.MALE. Don't forget to clear useless import.")
            } else if (debug('IDENTICAL EXPRESSIONS') && !isTest && index > 5 && index + 5 < totalLineCount) {
                String contextLines = ""
                ((index)..(index + 5)).each {
                    contextLines += lines[it] + " "
                }
                if (contextLines.matches('^.*[^.@](\\b[a-z]\\w+[.]\\w+[(][^)]*[)]).*\\1.*$')) {
                    String duplicateExpression = contextLines.replaceAll('^.*[^.@](\\b[a-z]\\w+[.]\\w+[(][^()]*[)]).*\\1.*$', '$1')
                    if (line.contains(duplicateExpression) && !trimmedLine.startsWith("//") && !line.toLowerCase().contains("random")) {
                        printWarning(line, LINE_NUMBER, "Identical expressions used more than once could be extracted as a variable to eliminate duplication: %s", duplicateExpression)
                    }
                }
            }
            if (debug('FOR STATEMENT OPTIMISATION') && trimmedSecureLine.contains('for') &&
                    (trimmedSecureLine.contains('.length') || trimmedSecureLine.contains('.size()'))) {
                printWarning(line, LINE_NUMBER, "Please extract a variable to store the value of length/size.")
            }
            if (debug('LINE MOVE UPPER') && line.replaceAll('\\s', '').matches('^[(){]{2,}$')) {
                printWarning(line, LINE_NUMBER, "Could this line be moved upper?")
            }
            if (debug('SEMICOLON IN GROOVY') && PROD_FILE_NAME.endsWith('groovy') && trimmedLine.endsWith(';')) {
                printWarning(line, LINE_NUMBER, "Semicolon could be removed in Groovy.")
            }
            if (debug('GROOVY PUBLIC') && PROD_FILE_NAME.endsWith('.groovy') && line.matches('^.*public\\s+\\w+\\s+\\w+\\s*[(].*[)].*$')) {
                printWarning(line, LINE_NUMBER, "Keyword public is redundant for non-static field in Groovy.")
            }
            if (debug('PROPER NOUN NAMING') && !LINE_META.COMMENT && !LINE_META.DOCUMENTATION &&
                    trimmedSecureLine.matches('^(.*\\w*[a-z0-9][A-Z]{3,}\\w*).*$')) {
                printWarning(line, LINE_NUMBER, 'Please rename the variable containing acronym, e.g. getHTMLChar() -} getHtmlChar().')
            }
            if (debug('COMPARE WITH BOOLEAN LITERAL') && !LINE_META.COMMENT && !LINE_META.DOCUMENTATION &&
                    trimmedLine.matches('^.*(==\\s*(true|false)|(true|false)\\s*==|==\\s*Boolean.(TRUE|FALSE)|Boolean.(TRUE|FALSE)\\s*==).*$')) {
                printWarning(line, LINE_NUMBER, 'Never compare with Boolean literal!')
            }
        }
//    printFileResult()
    }

    /**
     * Print the global warning message.
     *
     * @param error The error message.
     * @param args The arguments for error message template.
     */
    void printGlobalWarning(String error, String... args) {
        FILE_ISSUE_COUNT++
//    __println error
//    if (STATISTICS_TYPE_REPORT[error]) STATISTICS_TYPE_REPORT[error] += 1
//    else STATISTICS_TYPE_REPORT[error] = 1
//    if (STATISTICS_FILE_REPORT[PROD_FILE_NAME]) {
//        STATISTICS_FILE_REPORT[PROD_FILE_NAME]['ERRORS'] += 1
//    } else {
//        STATISTICS_FILE_REPORT[PROD_FILE_NAME] = [AUTHORS: AUTHORS, PROD_FILE_ABSOLUTE_PATH: PROD_FILE_ABSOLUTE_PATH, ERRORS: 1]
//    }

        // statistics all in one
        ALL_ISSUE_COUNT++
        if (STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH]) {
            STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH]['GLOBAL_ERRORS'] << new CodeStyleCheckGlobalError(error, args)
        } else if (!STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH]) {
            STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH] = [
                    AUTHORS                : AUTHORS,
                    PROD_FILE_NAME         : PROD_FILE_NAME,
                    PROD_FILE_ABSOLUTE_PATH: PROD_FILE_ABSOLUTE_PATH,
                    GLOBAL_ERRORS          : [new CodeStyleCheckGlobalError(error, args)],
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
    void printWarning(String line, int lineNumber, String error, String... args) {
        FILE_ISSUE_COUNT++
//    __println String.valueOf(lineNumber).padRight(5).concat(line).concat("\t&lt;===\t${error}")
//    if (STATISTICS_TYPE_REPORT[error]) STATISTICS_TYPE_REPORT[error] += 1
//    else STATISTICS_TYPE_REPORT[error] = 1
//    if (STATISTICS_FILE_REPORT[PROD_FILE_NAME]) {
//        STATISTICS_FILE_REPORT[PROD_FILE_NAME]['ERRORS'] += 1
//    } else {
//        STATISTICS_FILE_REPORT[PROD_FILE_NAME] = [AUTHORS: AUTHORS, PROD_FILE_ABSOLUTE_PATH: PROD_FILE_ABSOLUTE_PATH, ERRORS: 1]
//    }

        // statistics all in one
        ALL_ISSUE_COUNT++
        if (STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH]) {
            STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH]['LINE_ERRORS'] << new CodeStyleCheckLineError(line, lineNumber, error, args)
        } else if (!STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH]) {
            STATISTICS_ALL_IN_ONE[PROD_FILE_ABSOLUTE_PATH] = [
                    AUTHORS                : AUTHORS,
                    PROD_FILE_NAME         : PROD_FILE_NAME,
                    PROD_FILE_ABSOLUTE_PATH: PROD_FILE_ABSOLUTE_PATH,
                    GLOBAL_ERRORS          : [],
                    LINE_ERRORS            : [new CodeStyleCheckLineError(line, lineNumber, error, args)]
            ]
        }
    }

    /**
     * Calculate statistics.
     * @return Instance of {@link CodeStyleCheckReport}.
     */
    CodeStyleCheckReport calculateStatistics() {
        CodeStyleCheckSummaryData summaryData = new CodeStyleCheckSummaryData()
        CodeStyleCheckDetailData detailData = new CodeStyleCheckDetailData()
        CodeStyleCheckReport report = new CodeStyleCheckReport(summaryData, detailData)
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
            if (!globalErrors && !lineErrors) {
                summaryData.fileCountWithoutIssues++
            } else {
                summaryData.fileCountWithIssues++
            }
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
            if (it.getTotalErrorCount() > 0) {
                if (detailData.mapAuthorsErrors.containsKey(it.authorsKey)) {
                    int errorCount = detailData.mapAuthorsErrors.get(it.authorsKey)
                    detailData.mapAuthorsErrors.put(it.authorsKey, errorCount + it.getTotalErrorCount())
                }
            }
        }

        summaryData.filesGroupByError = summaryData.filesGroupByError.sort { 0 - it.value.size() }

        report
    }

//void printFileResult() {
//    __println "-" * 50
//    if (0 == FILE_ISSUE_COUNT) {
//        __print "Well done :)\t\t\t"
//    } else {
//        __print "Oops T_T\t\t\t"
//    }
//    __println "FILE_ISSUE_COUNT: ${FILE_ISSUE_COUNT}"
//}
//
//void printGlobalResult() {
//    __println "\n" * 5
//    __println "-" * 50
//    __println "TOTAL FILES: ${TARGET_FILES.size()}, ALL FOUND ISSUES: ${ALL_ISSUE_COUNT}"
//    __println "-" * 50
//    STATISTICS_TYPE_REPORT.sort { 0 - it.value }.each { __println "${it.value.toString().padLeft(3)} errors for [${it.key}]" }
//    __println()
////    Map<String, Map<String, Object>> ranking = [:]
////    STATISTICS_FILE_REPORT.each {
////        String authors = it.value['AUTHORS'].sort { it.toString() }
////        if (ranking[authors]) {
////            ranking[authors]['ERRORS'] += it.value['ERRORS']
////            ranking[authors]['FILES'] << it.key
////        } else {
////            ranking[authors] = [ERRORS: it.value['ERRORS'], FILES: [it.key]]
////        }
////    }
////    ranking.sort { 0 - it.value['ERRORS'] }.each {
////        __println "-" * 50
////        __println "${it.key} delivered ${it.value['ERRORS']} code smell(s) in below files:"
////        __println "-" * 50
////        it.value['FILES'].each { __println it }
////        __println()
////    }
////    null
//}

    /**
     * Replace all string literals with empty string.
     *
     * @param line The current line text.
     * @return The current line text without string literals.
     */
    String stripStringPattern(line) {
        line.replaceAll('"(\\\\"|[^"])+?"', '""').replaceAll("'(\\\\'|[^'])+?'", "''")
    }

    /**
     * Enable debug info.
     *
     * @param topic The topic info for debug.
     * @return True when debug is enabled, otherwise false.
     */
    boolean debug(topic) {
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
    void __println(String text) {
        __print((null == text ? "" : text) + "\n")
    }

    /**
     * Output the text.
     *
     * @param text Text to output.
     */
    void __print(text) {
        outputBuilder.append(text)
    }
}

// NEW FEATURES
// Documentation is missing for class constructor
// LOGGER the exception from catch clause
// Format well check should strip string literal first
// getHTMLChar check should strip string literal first
// print check should check word only: exclude __print
// static final check should happen as line check
// don't check unit test if the package name ends with "bean" or "model"
// Detect the codes commented out
// Class should has authors
