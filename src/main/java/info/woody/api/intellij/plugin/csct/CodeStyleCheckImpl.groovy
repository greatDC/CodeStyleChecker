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

/**
 * <p>http://www.oracle.com/technetwork/java/codeconvtoc-136057.html</p>
 *
 * @author Woody
 */
class CodeStyleCheckImpl {
    public String MY_SOURCE_DIR = "C:\\workstation\\workspace\\git\\gitOyster\\tRetailAPI\\src"
    public List<String> FILES_TO_SKIP = ("""
ConfigService.java
""".replaceAll("(?i)[^a-z.\\n]", "").split("\\s*\\n\\s*") as List).findAll { return it.length() }
    public String FILENAME_PATTERN_TO_SKIP = '^.*(Controller).*$'
    // below file list could be created by git command 'git diff --name-only branch1 branch2'
    // Sample: "git diff --name-only HEAD origin/SPRINT_BOEING_727 | grep -e java$ -e groovy$"
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
    private boolean ENABLE_CONSOLE_REPORT = false

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
        if (ENABLE_CONSOLE_REPORT) {
            printGlobalResult()
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
            printGlobalWarning CodeStyleCheckIssues.GLOBAL_NO_AUTHORS
            AUTHORS << "Anonymous"
        }
        if (content.matches('(?s)^.*(\\s*\\r?+\\n){3}.*$')) {
            printGlobalWarning CodeStyleCheckIssues.GLOBAL_CONSECUTIVE_EMPTY_LINES
        }
        if (content.matches('(?s)^.*\\r?\\n\\s*\\r?\\n[}].*$')) {
            printGlobalWarning CodeStyleCheckIssues.GLOBAL_LAST_METHOD_HAS_TAILING_EMPTY_LINE
        }
        if (lines.find { stripStringPattern(it).contains('final static') }) {
            printGlobalWarning CodeStyleCheckIssues.GLOBAL_STATIC_FINAL
        }
        if (content.charAt(content.length() - 1) != '\n') {
            printGlobalWarning CodeStyleCheckIssues.GLOBAL_FILE_END_EMPTY_LINE
        }
        if (!isTest && content.split("""\r?\n""").length > 500) {
            printGlobalWarning CodeStyleCheckIssues.GLOBAL_MORE_THAN_500_LINES
        }
        if (content.toLowerCase().matches('(?i)^.*\\b(todo|fixme)\\b.*$')) {
            printGlobalWarning CodeStyleCheckIssues.GLOBAL_TODO_FIXME
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
                printGlobalWarning CodeStyleCheckIssues.GLOBAL_MOCKITO_ORDER
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
                    printGlobalWarning(CodeStyleCheckIssues.GLOBAL_MISS_ERROR_CODE_TEST, missingErrorCodesText)
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
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_EXTRAORDINARY_LONG)
                return
            }

            if (debug('DOCUMENTATION') && trimmedLine.matches('^/?[*].*$')) {
                LINE_META.DOCUMENTATION = true
                String originLine = line
                line = trimmedLine.replaceAll("<[^>]+>", "")
                if (line.matches("[ */]*")) {
                } else if (line.contains('@author') || line.contains('@see')) {
                } else if (line.contains('@since')) {
                    if (!line.matches('''^.*\\b([012][0-9]|30|31)/(0[1-9]|1[0-2])/201[0-9]\\b.*$''')) {
                        printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_INCORRECT_CREATION_DATE_FORMAT)
                    }
                } else if (!trimmedLine.replaceFirst('^[*] ([^@]*((@param|@throws) \\w+|@return))?', '').trim()
                        .matches('^[0-9A-Z{<].*[.:,;!?>]$')) {
                    printWarning(originLine, LINE_NUMBER, CodeStyleCheckIssues.LINE_DOCUMENTATION_FORMAT)
                }
                if (line.replaceAll('[{][^}]+[}]', '').replaceAll('(Chinese|International)', '')
                        .matches('^.*@\\w+\\s+\\w+\\s+.*[A-Z][a-z].*[A-Z][a-z].*$')) {
                    printWarning(originLine, LINE_NUMBER, CodeStyleCheckIssues.LINE_CODE_IN_DOCUMENTATION)
                }
                String nextLine = lines[index + 1].trim()
                if (trimmedLine == "/**" && (nextLine == '*' || nextLine == '*/' || nextLine.startsWith('* @'))) {
                    printWarning(originLine, LINE_NUMBER, CodeStyleCheckIssues.LINE_NO_DOCUMENTATION_CONTENT)
                }
            } else if (debug('SINGLE LINE COMMENT') && line.matches('^\\s*//.*$')) {
                LINE_META.COMMENT = true
                if (trimmedLine.matches('^.*\\b(\\w+[.]\\w+[(]|\\w+ ?= ?\\w+[.]\\w+|if ?[(]|\\w+[(][)]).*$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_COMMENTED_OUT_CODES)
                } else if (trimmedLine.matches('^\\s*//+[^ ].+$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_SINGLE_LINE_COMMENT_FORMAT)
                }
            } else if (debug('SINGLE { IN A LINE') && line.matches('^\\s*[{]\\s*$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_LEFT_CURLY_BRACE_LINE)
            } else if (debug('IMPORT STATEMENT') && trimmedLine.startsWith('import ')) {
                LINE_META.IMPORT = true
                if (line.matches('^\\s*import\\b.+[*].*$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_IMPORT_ASTERISK)
                }
                String importedKeyword = line.replaceAll('^.+[.](\\w+)\\W?$', '$1')
                if (!line.contains('*') && !content.replaceFirst('(?s)\\b' + importedKeyword + '\\b', '')
                        .matches('(?s)^.*\\b' + importedKeyword + '\\b.*$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_NEVER_USED_IMPORTED)
                }
            } else if (debug('CHECK NullPointerException') &&
                    line.matches('^.*[.](equals|equalsIgnoreCase)[(]([^)]+[.])?[A-Z_]{2,}[)].*$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CONSTANT_AS_LEFT_OPERAND)
            } else if (debug('ENUM COMPARISON') && !LINE_META.FIELD && line.matches('^.+\\b\\w+Enum[.][A-Z_]+[.]equals.+$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_ENUM_COMPARE)
            } else if (debug('FIELD') && trimmedSecureLine.matches('^(private|protected|public)[^{(]+$') &&
                    !line.matches('^.*\\b(class|interface|enum)\\b.*$')) {
                LINE_META.FIELD = true
                String fieldName = line.replaceAll('=.*$', '').trim().replaceAll('^.*\\b(\\w+)[\\s;]*$', '$1')
                String fieldAssignment = "${fieldName} = ${fieldName}"
                String codesAfterConstructor = content.substring(content.indexOf(fieldAssignment) + fieldAssignment.length())
                String codesAfterThisLine = (lines as List).subList(index + 1, lines.size() - 1).join('\n')
                if (debug('UNUSED FIELD') && !['@Mock', '@Spy'].contains(lines[index - 1].trim()) // don't check mocked field
                        // && PROD_FILE_NAME.matches('^.+(Impl|Service|Validator|Mapper|Process|Util|Interceptor|Helper).*$')
                        && !trimmedLine.startsWith("public")
                        && (!codesAfterThisLine.matches('(?s)^.*\\b' + fieldName + '\\b.*$') // field never appears after declaration
                                || (content.contains(fieldAssignment) && // field never appears after constructor
                                !codesAfterConstructor.matches('(?s)^.*\\b' + fieldName + '\\b.*$')))) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_UNUSED_FIELD)
                }
                if (debug('PRIVATE FIELD') && isTest && line.contains("protected ")) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_UNIT_TEST_PRIVATE_FIELD)
                }
                if (debug('REFERENCE FIELD') && !isTest && !line.contains("LOGGER")) {
                    if (!lines[0].matches('^.*\\b(models?|beans?|constants?)\\b.*$')) {
                        if (PROD_FILE_NAME.contains("Controller.java")) {
                            if (!line.contains("private")) {
                                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_FIELD_MODIFIER_FOR_CONTROLLER)
                            }
                        } else {
                            if (!line.contains("protected") && !line.contains("public")) {
                                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_FIELD_MODIFIER_FOR_SERVICE)
                            }
                        }
                    }
                }
                if (debug('NAMING') && (line.contains("Validator ") || line.contains("Service ") || line.contains("Converter ")) &&
                        !line.toLowerCase().matches('^.*\\b(\\w+(validator|service|converter)\\b) \\1.*$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_FIELD_NAME_CONVENTION)
                }
                if (debug('CHECK LOGGER') && line.contains(" logger ")) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_LOGGER_NAME_CONVENTION)
                }
                if (debug('CHECK LOGGER') && trimmedSecureLine.toUpperCase().contains(" LOGGER ")
                        && !trimmedSecureLine.contains(PROD_FILE_NAME.replaceFirst('[.]\\w+$', ''))) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_LOGGER_TARGET_CLASS)
                }
                if (debug('STATIC') && line.contains(" static ")) {
                    if (!line.contains(" final ")) {
                        printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_MISSING_FINAL)
                    } else if (line.matches('(?i)^.* (string|boolean|int|integer|float|long)\\b.*$') &&
                            fieldName != fieldName.toUpperCase()) {
                        printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CONSTANT_NAME_CONVENTION)
                    }
                }
            } else if (debug('CLASS') && line.matches('^.*(private|protected|public)?[^(]*(class|interface|enum)[^(.\'="]*$')) {
                LINE_META.CLASS = true
                if (!isTest && PROD_FILE_NAME.matches('^.+(Impl|Service|Validator|Mapper|Process|Util).*$') &&
                        !line.contains("interface") && !line.contains("abstract") && !line.startsWith("Base") &&
                        !ALL_FILES_NAME.contains(PROD_FILE_NAME.replaceAll('.(groovy|java)$', 'Test.java')) &&
                        !ALL_FILES_NAME.contains(PROD_FILE_NAME.replaceAll('.(groovy|java)$', 'Test.groovy'))) {
                    if (!lines[0].matches('^.*\\b(models?|beans?)\\b.*$')) {
                        printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_MISSING_UNIT_TEST)
                    }
                }
                if (!(lines as List).subList(0, index).any { it.trim().startsWith("*") }) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CLASS_MISSING_DOCUMENTATION)
                }
            } else if (debug('METHOD') && trimmedSecureLine.matches('^[^})({]*\\w+(\\W{2}|<[^>]+>)? [_a-z]\\w+[(].+[{]$')) {
                LINE_META.METHOD = true
                if (LINE_NUMBER > 3) {
                    if (!lines[index - 1].contains("@Override") && !lines[index - 2].contains("*")) {
                        printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_METHOD_MISSING_DOCUMENTATION)
                    }
                }
                String methodName = trimmedLine.replaceAll('^.*\\w+(\\W{2}|<[^>]+>)? ([a-z]\\w+)[(].+[{]$', '$2')
                if ((line.contains('private ') || line.contains('protected ')) &&
                        !content.replaceFirst('\\b' + methodName + '\\b', '').contains(methodName)) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_UNUSED_METHOD)
                }
            } else if (debug('CONSTRUCTOR') && trimmedSecureLine.matches("^.*${PROD_FILE_NAME.replaceFirst('[.].*$', '')}[(].*")) {
                LINE_META.CONSTRUCTOR = true
                if (LINE_NUMBER > 3) {
                    if (!lines[index - 2].trim().startsWith("*")) {
                        printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CONSTRUCTOR_MISSING_DOCUMENTATION)
                    }
                }
            } else if (isTest && ((PROD_FILE_NAME.endsWith('java') && trimmedSecureLine.split('\\W').contains('assert')) ||
                    (PROD_FILE_NAME.endsWith('groovy') && trimmedSecureLine.split('\\W').contains('Assert')))) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_ASSERT)
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
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CONSTANT_FOR_LITERAL)
                }
            }
            if (debug('CODE STYLE') && !LINE_META.COMMENT && !LINE_META.DOCUMENTATION /* Not comments */
                    && !line.matches('^.*@\\w+.*$') /* Not annotation pattern */
                    && trimmedSecureLine.matches('^.*\\b((if|while|for|switch)[(]|([)]|try|else|catch|finally|\\w+)[{]|[}](else|catch|finally))\\b.*$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_NOT_FORMATTED)
            }
            if (debug('NAMING CONSTRAINT') && line.matches('^.*\\b(\\w+(Str(ing)?|Redis)|(str|redis)[A-Z]\\w+)\\s*=.*$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_BAD_VARIABLE_PATTERN)
            }
            if (debug('REQUESTPROPERTIES') && trimmedSecureLine.contains('"requestProperties"')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CONSTANT_REQUESTPROPERTIES)
            }
            if (trimmedSecureLine.startsWith("def ")) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_GROOVY_DEF)
            }
            if (!LINE_META.COMMENT && !LINE_META.DOCUMENTATION && trimmedSecureLine.matches('^.*\\bprint(ln)?\\b.+$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_BAD_PRINT)
            }
            if (!PROD_FILE_NAME.contains("Controller.java") &&
                    lineLength > 140 /*140 < lineLength && lineLength < 234 && lineLength - secureLine.length() < 100*/) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_EXCEED_140_CHARS)
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
                            trimmedLine != "'''" && // Groovy multiline string
                            !trimmedLine.contains(' << ') && // Groovy's List.add(e)
                            !lines[index - 1].trim().endsWith('->') && // Groovy's closure
                            !lines[index - 1].trim().contains('<<') && // Groovy's list operation
                            !lines[index - 1].trim().endsWith('++') && // Groovy's ++
                            !lines[index - 1].trim().endsWith(')') && // Groovy's method invocation
                            (!lines[index - 1].contains(":") && !line.contains(":")) && // Groovy's map
                            !trimmedLine.startsWith(".") && // method of pipeline pattern
                            !trimmedLine.startsWith("if") && // if statement
                            !trimmedLine.matches('^(String|int|Map|List|Set)\\b.*$') && // declaration or assignment
                            !line.contains("import ") && !lines[index - 1].contains("import ") && // import statement
                            !line.contains(" = ") && !lines[index - 1].contains(" = ") && // assignment statement
                            !line.matches('^.*[\\w<>]+ \\w+.*$') // skip method parameter declaration
                    ) && (lines[index - 1] + trimmedLine).length() < 140) {
                if (!PROD_FILE_NAME.endsWith("groovy") || !(line.endsWith(")") && lines[index - 1].endsWith(")"))) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_MERGE_LINES)
                }
            }
            if (debug('ENUM IMPORTING') && !LINE_META.FIELD && !LINE_META.COMMENT && !LINE_META.DOCUMENTATION &&
                    secureLine.matches('^.*\\b[A-Z]\\w+[.]\\w+Enum.*$') && !line.contains("import ")) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_ENUM_IMPORT)
            }
            if (debug('IDENTICAL EXPRESSIONS') && !isTest && index > 5 && index + 5 < totalLineCount) {
                String contextLines = ""
                ((index)..(index + 5)).each {
                    contextLines += lines[it] + " "
                }
                if (contextLines.matches('^.*[^.@](\\b[a-z]\\w+[.]\\w+[(][^)]*[)]).*\\1.*$')) {
                    String duplicateExpression = contextLines.replaceAll('^.*[^.@](\\b[a-z]\\w+[.]\\w+[(][^()]*[)]).*\\1.*$', '$1')
                    if (line.contains(duplicateExpression) && !trimmedLine.startsWith("//") && !line.toLowerCase().contains("random")) {
                        printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_MULTIPLE_IDENTICAL_EXPRESSIONS, duplicateExpression)
                    }
                }
            }
            if (debug('FOR STATEMENT') && !LINE_META.COMMENT && !LINE_META.DOCUMENTATION && trimmedSecureLine.contains('for') &&
                    (trimmedSecureLine.contains('.length') || trimmedSecureLine.contains('.size()'))) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_REDUCE_MULTIPLE_CALCULATION)
            }
            if (debug('LINE MOVE UPPER') && !LINE_META.COMMENT && !LINE_META.DOCUMENTATION &&
                    line.replaceAll('\\s', '').matches('^[(){]{2,}$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_MOVE_UPPER_ADVICE)
            }
            if (debug('SEMICOLON IN GROOVY') && !LINE_META.COMMENT && !LINE_META.DOCUMENTATION
                    && PROD_FILE_NAME.endsWith('groovy') && trimmedLine.endsWith(';')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_REDUNDANT_GROOVY_SEMICOLON)
            }
            if (debug('GROOVY PUBLIC') && PROD_FILE_NAME.endsWith('.groovy') && line.matches('^.*public\\s+\\w+\\s+\\w+\\s*[(].*[)].*$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_REDUNDANT_GROOVY_PUBLIC)
            }
            if (debug('PROPER NOUN NAMING') && !LINE_META.COMMENT && !LINE_META.DOCUMENTATION &&
                    trimmedSecureLine.matches('^(.*\\w*[a-z0-9][A-Z]{3,}\\w*).*$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_IMPROPER_ACRONYM)
            }
            if (debug('COMPARE WITH BOOLEAN LITERAL') && !LINE_META.COMMENT && !LINE_META.DOCUMENTATION &&
                    trimmedSecureLine.matches('^.*(==\\s*(true|false)|(true|false)\\s*==|==\\s*Boolean.(TRUE|FALSE)|Boolean.(TRUE|FALSE)\\s*==).*$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_BOOLEAN_LITERAL_COMPARE)
            }
            if (debug('CATCH CLAUSE') && LINE_NUMBER > 8 && !LINE_META.COMMENT && !LINE_META.DOCUMENTATION
                    && stripStringPattern(lines[index - 1].trim()).split('\\W+').contains('catch')
                    && !trimmedSecureLine.matches('^LOGGER.error[(][^,]+[)].*$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_LOG_EXCEPTION)
            }
        }
        if (ENABLE_CONSOLE_REPORT) {
            printFileResult()
        }
    }

    /**
     * Print the global warning message.
     *
     * @param error The error message.
     * @param args The arguments for error message template.
     */
    void printGlobalWarning(String error, String... args) {
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
    void printFileResult() {
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
    void printGlobalResult() {
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
// DONE - Documentation is missing for class constructor
// DONE - LOGGER the exception from catch clause
// DONE - Format well check should strip string literal first
// DONE - getHTMLChar check should strip string literal first
// DONE - print check should check word only: exclude __print
// IN PLAN - static final check should happen as line check
// DONE - don't check unit test if the package name ends with "bean" or "model"
// DONE - Detect the codes commented out
// DONE - Class should has authors
// DONE - Add new check for assert / Assert
// DONE - No documentation content for method
// Empty line should exist between documentation description and @param, @return, @exception
// DONE - The class for LOGGER should be same as the current class
// static should come before non-static
//

