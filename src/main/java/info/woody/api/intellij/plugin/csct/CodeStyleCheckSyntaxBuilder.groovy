package info.woody.api.intellij.plugin.csct

import info.woody.api.intellij.plugin.csct.syntax.SimpleClassStructureClazz
import info.woody.api.intellij.plugin.csct.syntax.SimpleClassStructureImport
import info.woody.api.intellij.plugin.csct.syntax.SimpleClassStructureMethod
import info.woody.api.intellij.plugin.csct.syntax.SimpleClassStructureReference
import info.woody.api.intellij.plugin.csct.syntax.SimpleClassStructureType
import info.woody.api.intellij.plugin.csct.syntax.SimpleClassStructureSyntaxDetector as Judger

class CodeStyleCheckSyntaxBuilder {

    protected List<SimpleClassStructureReference> typeImports = new ArrayList<>()
    protected List<SimpleClassStructureReference> staticImports = new ArrayList<>()

    private static String stripStringPattern(line) {
        line.replaceAll('"(\\\\"|[^"])*?"', '""').replaceAll("'(\\\\'|[^'])*?'", "''")
    }

    private static String stripNonCodeContent(String content) {
    }

    private static String extractClazzName(String line) {
    }

    private static String extractMethodName(String line) {
    }

    private static String extractParameterNames(String line) {
    }

    static SimpleClassStructureClazz buildClazz(String content) {
        content = content
                .replaceAll('\\r?\\n', '\n')
                .replaceAll('(?s)[ \\t]*/[*].*?[*]/\\s*', '')
                .replaceAll('(?m)\\s*//.*', '\n')
        processClazzBlock(content)
    }

    private static SimpleClassStructureClazz processClazzBlock(String content) {
        String[] lines = content.split('\n')
        int lineStartIndex = -1
        int lineEndIndex = -1
        (lineStartIndex, lineEndIndex) = determineDefinitionRange(SimpleClassStructureType.CLAZZ, lines)
        SimpleClassStructureClazz clazz = new SimpleClassStructureClazz()
        clazz.setLineEndIndex(lineStartIndex)
        clazz.setLineEndIndex(lineEndIndex)
        clazz.setImportList(processImports(0, lineStartIndex, lines))
        clazz.setMethodList(processMethodBlocks(lineStartIndex, lineEndIndex, lines))
        clazz.setReferenceList(processFields(0, lineStartIndex, lines))
        clazz
    }

    private static List<SimpleClassStructureImport> processImports(int lineStartIndex, int lineEndIndex, String[] lines) {
        List<SimpleClassStructureImport> importList = []
        (lineStartIndex..<lineEndIndex).each { index ->
            String line = lines[index]
            if (Judger.isImportDefinition(line)) {
                importList.add(line)
            }
        }
        importList
    }

    private static List<SimpleClassStructureReference> processFields(int lineStartIndex, int lineEndIndex, String[] lines) {

    }

    private static List<SimpleClassStructureMethod> processMethodBlocks(int lineStartIndex, int lineEndIndex, String[] lines) {
        List<String> methodSignatureLines = []
        (lineStartIndex..<lineEndIndex).each { index ->
            String currentLine = lines[index]
            if (Judger.isMethodDefinition(currentLine)) {
                methodSignatureLines.add(currentLine)
            }
        }
        List<SimpleClassStructureMethod> methodList = []
        methodSignatureLines.each {
            (lineStartIndex, lineEndIndex) = determineDefinitionRange(SimpleClassStructureType.METHOD, lines)
            SimpleClassStructureMethod method = new SimpleClassStructureMethod()
            method.setLineStartIndex(lineStartIndex)
            method.setLineEndIndex(lineEndIndex)
            method.setStatementList(processStatementBlocks())
            method.setReferenceList(processParameterBlocks())
            method.setReferenceList(processVariableBlocks())
            methodList.add(method)
        }
        methodList
    }

    private static processStatementBlocks() {
        processStatementBlocks()
        processVariableBlocks()
    }
    private static processParameterBlocks(int lineStartIndex, int lineEndIndex, String[] lines) {
        (lineStartIndex, lineEndIndex) = determineDefinitionRange(SimpleClassStructureType.PARAMETER, "(", ")", lines)
        String parameterArea = (lineStartIndex..lineEndIndex).inject("") { result, index ->
            result + lines[index]
        }
        extractParameterNames(parameterArea)
    }
    private static processVariableBlocks() {

    }

    private static int[] determineDefinitionRange(SimpleClassStructureType type, String[] lines) {
        determineDefinitionRange(type, '{', '}', lines)
    }

    /**
     *
     * @param startSign `{`, `(`
     * @param endSign `}`, `)`
     * @param lines
     * @return
     */
    private static int[] determineDefinitionRange(SimpleClassStructureType type, String startSign, String endSign, String[] lines) {
        int lineCount = lines.length
        int lineStartIndex = -1
        if (lineStartIndex < 0 && [SimpleClassStructureType.CLAZZ, SimpleClassStructureType.METHOD].contains(type)) {
            lines.eachWithIndex { String line, int index ->
                if (((SimpleClassStructureType.CLAZZ == type && Judger.isTypeDefinition(line)) ||
                        (SimpleClassStructureType.METHOD == type && Judger.isMethodDefinition(line)))) {
                    lineStartIndex = index
                }
            }
        } else {
            for (int i = 0; i < lineCount; i++) {
                if (lines[i].contains(startSign)) {
                    lineStartIndex = i
                    break
                }
            }
        }
        if (lineStartIndex < 0) {
            throw CodeStyleCheckException("Cannot determine the definition range.")
        }

        int lineEndIndex
        boolean isDone = false
        List stack = []
        (lineStartIndex..<lineCount).each { index ->
            if (isDone) {
                return
            }
            stripStringPattern(lines[index]).findAll('[' + startSign + endSign +']').each {
                if (it == startSign) {
                    stack.push(startSign)
                } else if (it == endSign) {
                    if (index == lineStartIndex && !stack) {
                        return // to prevent first line starts with end sign(s)
                    }
                    stack.pop()
                } else {
                    throw CodeStyleCheckException("The file doesn`t conform with syntax.")
                }
                if (stack.size() == 0) {
                    lineEndIndex = index
                    isDone = true
                }
            }
        }

        [lineStartIndex, lineEndIndex]
    }
}
