package info.woody.api.intellij.plugin.csct

import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues

import static info.woody.api.intellij.plugin.csct.util.Const.LINE_SEPARATOR
/**
 * <p>http://www.oracle.com/technetwork/java/codeconvtoc-136057.html</p>
 *
 * @author Woody
 * @since 29/06/2018
 */
class CodeStyleCheckRuleImpl extends CodeStyleCheckRule {

    protected static final int MAX_LINE_LENGTH_CAN_BE_CHECKED = 234
    private Map LINE_META

    /**
     * Check the file to report the found issues.
     *
     * @param file File to check.
     * @param fileNumber File number.
     */
    protected void findPotentialIssues(File file, fileNumber) {
        LINE_NUMBER = 0
        LINE_META = [:]
        FILE_ISSUE_COUNT = 0
        PROD_FILE_NAME = file.name
        ALL_FILE_COUNT++
        debug(PROD_FILE_NAME)
        boolean isTest = PROD_FILE_NAME.toLowerCase().contains("test") || file.path.matches('^.*src(/|\\\\)test\\1.*$')
        String content = file.getText('UTF-8')
        String[] lines = content.split("""\r?\n""")
        __println LINE_SEPARATOR * 3 + '*' * 50
        PROD_FILE_ABSOLUTE_PATH = file.getAbsolutePath()
        __println "${fileNumber}. ".padLeft(5, '0')
                .concat("<a name='file' href='${PROD_FILE_ABSOLUTE_PATH}'>${PROD_FILE_NAME}</a> from ${PROD_FILE_ABSOLUTE_PATH}")
        __println '*' * 50
        AUTHORS = []
        for (String line : lines) {
            if (line.length() > MAX_LINE_LENGTH_CAN_BE_CHECKED) {
                continue
            }
            boolean isDocPattern = line.trim().startsWith("*")
            if (isDocPattern && line.matches('^(?i).*created? by ([\\w.]+).*$')) { // extract author from comment
                AUTHORS << line.replaceAll('^(?i).*created? by ([\\w.]+).*$', '$1').trim()
            } else if (isDocPattern && line.contains('@author')) { // extract author from documentation
                AUTHORS << line.replaceAll('^(?i).*@author ([\\w.]+).*$', '$1').trim()
            }
        }
        checkGlobalIssues(content, lines, isTest)

        int totalLineCount = lines.length
        lines.eachWithIndex { line, index ->

            LINE_NUMBER = index + 1
            LINE_META = [:]

            String trimmedLine = line.trim()
            String secureLine = stripStringPattern(line)
            String trimmedSecureLine = secureLine.trim()
            int trimmedLineLength = trimmedLine.length()

            int lineLength = line.length()
            if (lineLength > MAX_LINE_LENGTH_CAN_BE_CHECKED) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_EXTRAORDINARY_LONG)
                return
            }
            if (trimmedLine == '}' && trimmedLine.startsWith('@')) {
                return // do nothing
            } else if (debug('DOCUMENTATION') && trimmedLine.matches('^/?[*].*$')) {
                checkDocumentation(lines, index, line, trimmedLine)
            } else if (debug('SINGLE LINE COMMENT') && line.matches('^\\s*//.*$')) {
                checkSingleLineComment(lines, totalLineCount, index, line, trimmedLine)
            } else if (debug('IMPORT STATEMENT') && trimmedLine.startsWith('import ')) {
                checkImport(content, line)
            } else if (debug('FIELD') && (trimmedSecureLine.matches('^(private|protected|public)[^{(]+$') ||
                    trimmedSecureLine.matches('^(private|protected|public).+=.+$')) &&
                    !trimmedSecureLine.matches('^.* (class|interface|enum|abstract) .*$')) {
                checkField(content, lines, index, line, trimmedLine, trimmedSecureLine, isTest)
            } else if (debug('CLASS') && trimmedLine.matches('^(private|protected|public)?[^(]*(class|interface|enum)[^();.\'="]+[{]$')) {
                checkClassInterfaceEnum(lines, index, line, trimmedLine, isTest)
            } else if (debug('CONSTRUCTOR') && !trimmedSecureLine.endsWith(";") && !trimmedSecureLine.contains("=")
                    && trimmedSecureLine.matches("^.*${PROD_FILE_NAME.replaceFirst('[.].*$', '')}[(].*")) {
                checkConstructor(lines, index, line)
            } else if (debug('METHOD') && secureLine.matches('^(\t| {3,5})((public|protected|private)( ))?[\\w$]+(<[^()]+>)? [\\w$]+[(][^+-=)]*[)]\\s*[{]$')) {
                checkMethod(content, lines, index, line, trimmedLine, isTest)
            } else if (debug('SINGLE { IN A LINE') && line.matches('^\\s*[{]\\s*$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_LEFT_CURLY_BRACE_LINE)
            } else if (trimmedLineLength) {
                if (debug('CHECK NullPointerException') && trimmedLine.matches('^.*[.]equals(IgnoreCase)?[(]([^)]+[.])?[A-Z_]{2,}[)].*$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CONSTANT_AS_LEFT_OPERAND)
                }
                if (debug('ENUM COMPARISON') && trimmedSecureLine.matches('^.+\\b\\w+Enum[.][A-Z_]+[.]equals.+$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_ENUM_COMPARE)
                }
                if (debug('TEST ASSERT') && isTest && ((PROD_FILE_NAME.endsWith('java') && trimmedSecureLine.split('\\W').contains('assert')) ||
                        (PROD_FILE_NAME.endsWith('groovy') && trimmedSecureLine.split('\\W').contains('Assert')))) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_ASSERT)
                }
                if (debug('LITERAL') && !isTest && !trimmedLine.startsWith('@')
                        && !(PROD_FILE_NAME.contains('RequestService') // exclude the pattern like 'XxxRequestService'
                        && PROD_FILE_NAME.endsWith('.groovy')) // request service written by Groovy
                ) {
                    if (trimmedLine.matches('(?i)^.+?"([a-z0-9._]+|\\W)".*$') // string pattern only
                            || trimmedSecureLine.replaceAll('\\[\\d+\\]', '') // remove index pattern for list element
                            .matches('^.*\\b([2-9]|[1-9]\\d+|\\d+[.]\\d+)\\b.*$') // number pattern only, excluding 0 and 1
                        // if statement
                        // equals
                        // =
                        // contains
                        // indexOf
                    ) {
                        printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CONSTANT_FOR_LITERAL)
                    }
                }
                if (debug('FORMAT') && trimmedSecureLine.matches('^.*(\\b(if|while|for|switch)[(]|\\w+[={]|[=}]\\w+|[)][{]).*$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_NOT_FORMATTED)
                }
                if (debug('BAD NAMING') && trimmedSecureLine.matches('^.*(\\w+(Str(ing)?|Redis)|(str(ing)?|redis)[A-Z]\\w+)(\\s*=.*)$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_BAD_VARIABLE_PATTERN)
                }
                if (debug('REQUESTPROPERTIES') && trimmedLine.contains('"requestProperties"')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CONSTANT_REQUESTPROPERTIES)
                }
                if (trimmedSecureLine.startsWith("def ")) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_GROOVY_DEF)
                }
                if (trimmedSecureLine.matches('^.*\\bprint(ln)?\\b.+$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_BAD_PRINT)
                }
                if (!PROD_FILE_NAME.contains("Controller.java") && lineLength > 140) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_EXCEED_140_CHARS)
                }
                if (debug('MERGE LINES') && !isTest && index > 1 && lines[index - 1].trim().length() > 0 &&
                        (!line.matches('^.*\\b(public|protected|private)\\b.*$') &&
                                !line.endsWith("{") && !lines[index - 1].endsWith("{") && // method
                                !line.contains("*") && !lines[index - 1].contains("*") && // multiline comment
                                !line.contains("@") && !lines[index - 1].contains("@") && // annotation
                                !line.contains("//") && !lines[index - 1].contains("//") && // single line comment
                                !(line.endsWith(";") && lines[index - 1].endsWith(";")) && // two statements
                                !lines[index - 1].endsWith(";") && // previous line is statement
                                !line.endsWith("}") && !lines[index - 1].endsWith("}") && // end curly brace
                                !lines[index - 1].trim().endsWith(":") && // colon
                                !lines[index - 1].trim().endsWith(";") && // method invocation
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
                if (debug('ENUM IMPORTING') && trimmedSecureLine.matches('^.*\\b[A-Z]\\w+[.]\\w+Enum[.][A-Z].*$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_ENUM_IMPORT)
                }
                if (debug('IDENTICAL EXPRESSIONS') && !isTest && index > 4 && index + 4 < totalLineCount) {
                    StringBuilder contextBuilder = new StringBuilder()
                    int endOffset = 4
                    if (index + 8 < totalLineCount) {
                        endOffset = 8
                    }
                    ((index)..(index + endOffset)).each {
                        String eachLine = lines[it]
                        if (eachLine.length() < MAX_LINE_LENGTH_CAN_BE_CHECKED && !eachLine.trim().matches('^(//|/?[*]).*$')) {
                            contextBuilder.append(eachLine).append(" ")
                        }
                    }
                    String contextLines = contextBuilder.toString(); // pattern: var.call()
                    if (contextLines.matches('^.*[^.@](\\b[a-z]\\w+[.]\\w+[(][^()]*[)]).*\\1.*$')) {
                        String duplicateExpression = contextLines.replaceAll('^.*[^.@](\\b[a-z]\\w+[.]\\w+[(][^()]*[)]).*\\1.*$', '$1')
                        if (line.contains(duplicateExpression) && !line.toLowerCase().contains("random")
                                && !line.toLowerCase().contains("stream") && !line.toLowerCase().contains("append")) {
                            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_IDENTICAL_EXPRESSIONS, duplicateExpression)
                        }
                    }
                }
                if (debug('FOR STATEMENT') && trimmedSecureLine.startsWith('for') &&
                        (trimmedSecureLine.contains('.length') || trimmedSecureLine.contains('.size()'))) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_REDUCE_MULTIPLE_CALCULATION)
                }
                if (debug('RETURN STATEMENT') && index > 2 && totalLineCount - LINE_NUMBER > 2 && lines[index - 1].trim().startsWith('if ') &&
                        trimmedLine.startsWith('return') && lines[index + 2].trim().startsWith('return')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_OPTIMIZE_RETURN)
                }
                if (debug('LINE MOVE UPPER') && trimmedLine.replaceAll('\\s', '').matches('^[(){]{2,}$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_MOVE_UPPER_ADVICE)
                }
                if (debug('SEMICOLON IN GROOVY') && PROD_FILE_NAME.endsWith('groovy') && trimmedLine.endsWith(';')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_REDUNDANT_GROOVY_SEMICOLON)
                }
                if (debug('PROPER NOUN NAMING') && trimmedSecureLine.matches('^(.*[a-z0-9][A-Z]{3,}\\w+).*$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_IMPROPER_ACRONYM)
                }
                if (debug('COMPARE WITH BOOLEAN LITERAL') &&
                        trimmedSecureLine.matches('^.*(==\\s*(true|false)|(true|false)\\s*==|==\\s*Boolean.(TRUE|FALSE)|Boolean.(TRUE|FALSE)\\s*==).*$')) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_BOOLEAN_LITERAL_COMPARE)
                }
                if (debug('CATCH CLAUSE') && !isTest && LINE_NUMBER > 8
                        && stripStringPattern(lines[index - 1].trim()).split('\\W+').contains('catch') // previous line is catch
                        && (!trimmedSecureLine.matches('^LOGGER[.](error|warn).*[,].*$')
                        && !lines[index + 1].matches('^LOGGER[.](error|warn).*[,].*$'))) {
                    printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_LOG_EXCEPTION)
                }
            }
        }
        if (ENABLE_CONSOLE_REPORT) {
            printFileResult()
        }
    }

    private void checkImport(String content, String line) {
        LINE_META.IMPORT = true
        if (line.contains('*')) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_IMPORT_ASTERISK)
        } else {
            String importedKeyword = line.replaceAll('^.+[.](\\w+)\\W*$', '$1')
            if (!content.replaceFirst('(?s)\\b' + importedKeyword + '\\b', '')
                    .matches('(?s)^.+\\b' + importedKeyword + '\\b.+$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_NEVER_USED_IMPORTED)
            }
        }
    }

    private void checkDocumentation(String[] lines, int index, String line, String trimmedLine) {
        LINE_META.DOCUMENTATION = true
        int nextLineIndex = index + 1
        String nextTrimmedLine = ''
        if (nextLineIndex < lines.length) {
            nextTrimmedLine = lines[nextLineIndex].trim()
        }
        if (trimmedLine == "/**" && (nextTrimmedLine == '*' || nextTrimmedLine == '*/' || nextTrimmedLine.startsWith('* @'))) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_NO_DOCUMENTATION_CONTENT)
        }
        String lineWithoutHtml = trimmedLine.replaceAll("<[^>]+>", "")
        if (trimmedLine.startsWith('/**') || trimmedLine.endsWith('*/') || trimmedLine.contains('@author')) {
            return
        } else if (lineWithoutHtml.contains('@since')) {
            if (!lineWithoutHtml.matches('''^.*\\b([012][0-9]|30|31)/(0[1-9]|1[0-2])/201[0-9]\\b.*$''')) {
                printWarning(lineWithoutHtml, LINE_NUMBER, CodeStyleCheckIssues.LINE_INCORRECT_CREATION_DATE_FORMAT)
            }
        } else if (trimmedLine == '*') {
            if (nextTrimmedLine == '*') {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_DOCUMENTATION_REDUNDANT_EMPTY_LINES)
            }
        } else if (!trimmedLine.replaceFirst('^[*] (@(param|throws) \\w+|@return)?', '').trim()
                .matches('^[0-9A-Z{<].*[.:,;!?>]$')) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_DOCUMENTATION_FORMAT)
        }
        if (lineWithoutHtml.replaceAll('[{][^}]+[}]', '').replaceAll('(Chinese|International)', '')
                .matches('^[*] (@\\w+\\s+\\w+\\s+)?[A-Z][a-z].*[A-Z][a-z].*$')) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CODE_IN_DOCUMENTATION)
        }
    }

    private void checkClassInterfaceEnum(String[] lines, int index, String line,  String trimmedLine, boolean isTest) {
        LINE_META.CLASS = true
        if (!isTest && !line.contains("interface") && !line.contains("abstract") && !line.contains("Base") && !line.contains("Const") && !line.contains("Constant") &&
                !ALL_FILES_NAME.contains(PROD_FILE_NAME.replaceAll('.(groovy|java)$', 'Test.java')) &&
                !ALL_FILES_NAME.contains(PROD_FILE_NAME.replaceAll('.(groovy|java)$', 'Test.groovy'))) {
            if (!lines[0].matches('^.*\\b(models?|beans?|pojos?|constants?)\\b.*$') && !PROD_FILE_NAME.contains('Base') &&
                    !trimmedLine.contains('enum ') && !trimmedLine.contains('interface ') && !trimmedLine.contains('Enum ') &&
                    !PROD_FILE_NAME.contains('Config') && !PROD_FILE_NAME.contains('Bean') && !PROD_FILE_NAME.contains('Exception')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_MISSING_UNIT_TEST)
            }
        }
        if (!(lines as List).subList(0, index).any { it.trim().startsWith("*") }) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CLASS_MISSING_DOCUMENTATION)
        }
        if (PROD_FILE_NAME.endsWith('.groovy') && trimmedLine.startsWith('public ')) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_GROOVY_PUBLIC_IN_CLASS)
        }
    }

    private void checkField(String content, String[] lines, int index, String line, String trimmedLine, String trimmedSecureLine, boolean isTest) {
        LINE_META.FIELD = true
        String fieldName = line.replaceAll('=.*$', '').trim().replaceAll('^.*\\b(\\w+)[\\s;]*$', '$1')
        String fieldAssignment = "${fieldName} = ${fieldName}"
        String codesAfterConstructor = content.substring(content.indexOf(fieldAssignment) + fieldAssignment.length())
        String codesAfterThisLine = (lines as List).subList(index + 1, lines.size() - 1).join('\n')
        if (debug('UNUSED FIELD') && !['@Mock', '@Spy'].contains(lines[index - 1].trim()) // don't check mocked field
                && !lines[0].matches('^.*\\b(models?|beans?|pojos?|constants?)\\b.*$')
                && !trimmedLine.contains(' serialVersionUID ')
                && !trimmedLine.startsWith("public")
                && !content.contains('get' + fieldName.capitalize())
                && !content.contains('set' + fieldName.capitalize())
                && (!codesAfterThisLine.matches('(?s)^.*\\b' + fieldName + '\\b.*$') // field never appears after declaration
                || (content.contains(fieldAssignment) && // field never appears after constructor
                !codesAfterConstructor.matches('(?s)^.*\\b' + fieldName + '\\b.*$')))) {
            if (!trimmedLine.startsWith('protected ') || !PROD_FILE_NAME.contains('Base')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_UNUSED_FIELD)
            }
        }
        if (debug('PRIVATE FIELD') && isTest && line.contains("protected ") && !PROD_FILE_NAME.contains('Base')) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_UNIT_TEST_PRIVATE_FIELD)
        }
        if (debug('REFERENCE FIELD') && !isTest && !line.contains("LOGGER") &&
            !lines[0].matches('^.*\\b(models?|beans?|pojos?|constants?)\\b.*$')) {
            if (PROD_FILE_NAME.contains("Controller.java") && !trimmedLine.startsWith("private")) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_FIELD_MODIFIER_FOR_CONTROLLER)
            } else if (!trimmedLine.startsWith("protected") && !trimmedLine.startsWith("public")) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_FIELD_MODIFIER_FOR_SERVICE)
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
        if (debug('STATIC') && trimmedLine.contains(" static ")) {
            if (!trimmedLine.contains(" final ")) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_MISSING_FINAL)
            } else if (!trimmedLine.contains(' serialVersionUID ') &&
                    trimmedLine.matches('(?i)^.* (string|boolean|int|integer|float|long) .*$') &&
                    fieldName != fieldName.toUpperCase()) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CONSTANT_NAME_CONVENTION)
            }
        }
        /*
        @Deprecated
        if (debug('GROOVY PUBLIC') && PROD_FILE_NAME.endsWith('.groovy') &&
                !trimmedLine.contains(' static ') && trimmedLine.startsWith('public ')) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_GROOVY_PUBLIC_IN_FIELD)
        }
        */
    }

    private void checkConstructor(String[] lines, int index, String line) {
        LINE_META.CONSTRUCTOR = true
        if (LINE_NUMBER > 3 && !lines[index - 2].trim().startsWith("*")) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_CONSTRUCTOR_MISSING_DOCUMENTATION)
        }
    }

    private void checkMethod(String content, String[] lines, int index, String line, String trimmedLine, boolean isTest) {
        LINE_META.METHOD = true
        if (LINE_NUMBER > 3 && !lines[index - 1].contains("@Override") && !lines[index - 3].contains("*")) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_METHOD_MISSING_DOCUMENTATION)
        }
        String methodName = trimmedLine.replaceAll('^.*\\b([\\w+$]+)[(].+$', '$1')
        if ((line.contains('private ') || line.contains('protected ')) && !PROD_FILE_NAME.contains('Base') && !PROD_FILE_NAME.contains('Common') &&
                !content.replaceFirst('\\b' + methodName.replace('$', '\\$') + '\\b', '').contains(methodName)) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_UNUSED_METHOD)
        }
        if (debug('TEST PREFIX') && isTest && lines[index - 1].trim().startsWith("@Test") && !trimmedLine.contains(" test")) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_TEST_METHOD_PREFIX_WRONG)
        }
        if (debug('GROOVY PUBLIC') && PROD_FILE_NAME.endsWith('.groovy') && trimmedLine.startsWith('public ')) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_GROOVY_PUBLIC_IN_METHOD)
        }
    }

    private void checkGlobalIssues(String content, String[] lines, boolean isTest) {
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
        if (!isTest && content.split("\\r?\\n").length > 500) {
            printGlobalWarning CodeStyleCheckIssues.GLOBAL_MORE_THAN_500_LINES
        }
        if (content.toLowerCase().matches('(?is)^.*\\b(todo|fixme|hack|xxx)\\b.*$')) {
            printGlobalWarning CodeStyleCheckIssues.GLOBAL_TODO_FIXME_HACK_XXX
        }
        if (content.contains('@Profile("International"') || content.contains('@Profile("Chinese"')) {
            String className = PROD_FILE_NAME.replaceAll('[.].+', '')
            if (!className.matches('.*(Chinese|International)Impl$')) {
                printGlobalWarning CodeStyleCheckIssues.GLOBAL_BAD_CLASS_NAMING_WITH_PROFILE
            }
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
            String TEST_FILE_NAME = PROD_FILE_NAME.replaceAll('[.](java|groovy)', 'Test.$1')
            File testFile = ALL_FILES.find { TEST_FILE_NAME == it.name }
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
    }

    private void checkSingleLineComment(String[] lines, int totalLineCount, int index, String line, String trimmedLine) {
        LINE_META.COMMENT = true
        if (trimmedLine.matches('^.*\\b(\\w+[.]\\w+[(]|\\w+ ?= ?\\w+[.]\\w+|if ?[(]|\\w+[(][)]).*$')) {
            printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_COMMENTED_OUT_CODES)
        } else {
            int nextLineIndex = index + 1
            if (nextLineIndex < totalLineCount) {
                String nextLine = lines[nextLineIndex].trim()
                if (!nextLine.startsWith('//') && !nextLine.startsWith('/*')) {
                    List<String> commentWords = trimmedLine.replace('//', '').trim().split('\\W').findAll {
                        String word = it.trim()
                        word.length() && !word.matches('^(then?|an?|to|s|its?|that|and|so|f?or|because|since|of)$')
                    }
                    int commentWordCountInCodes = 0
                    commentWords.each {
                        if (nextLine.toLowerCase().contains(it.toLowerCase())) {
                            commentWordCountInCodes++
                        }
                    }
                    int commentWordCountInComments = commentWords.size()
                    if ((commentWordCountInComments > 1) && (commentWordCountInCodes / commentWordCountInComments > 70 / 100)) {
                        printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_REDUNDANT_CODE_DESC)
                    }
                }
            }
            if (trimmedLine.matches('^\\s*//+[^ ].+$')) {
                printWarning(line, LINE_NUMBER, CodeStyleCheckIssues.LINE_SINGLE_LINE_COMMENT_FORMAT)
            }
        }
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
// indent badly
// disable the specified check items
// identical expression cannot be extracted if it's inside java stream
// add issue title for details of same issue category
// DONE - line number for details of same issue category should be padded
// auto expand folded lines
// compare with empty string
// compare with null, Groovy
// check xdist comment or documentation
// complex if expression is better to be assigned a well-named variable to improve the readability
// optimise acronym naming convention
