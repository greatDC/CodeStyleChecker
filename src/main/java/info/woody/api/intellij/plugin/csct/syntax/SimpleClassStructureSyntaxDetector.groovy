package info.woody.api.intellij.plugin.csct.syntax

class SimpleClassStructureSyntaxDetector {

    static boolean isMethodDefinition(String line) {
        return  !line.contains(' new ') && line.replaceAll('@\\w+', '')
                .replaceAll('<[^<>]+>', '').replaceAll('<[^<>]+>', '')
                .replaceAll('<[^<>]+>', '').replaceAll('(\\[|\\])', '')
                .matches('^([\\w]+\\s+)+[\\w$]+\\s*[(][\\w, ]*([)] *[{])?$')
    }

    static boolean isImportDefinition(String line) {
        return line.trim().startsWith('import ')
    }

    static boolean isTypeDefinition(String line) {
        return line.matches('^[^.(]*\\b(class|interface|enum)\\b.*$')
    }

    static boolean isForDefinition(String line) {
        return line.trim().matches('^for\\b.*$')
    }

    static boolean isWhileDoWhileDefinition(String line) {
        return line.trim().matches('^(while|do)\\b.*$')
    }

    static boolean isVariableDefinition(String line) {
        String lineWithoutGenericType = line.replaceAll('<[^<>]+>', '').replaceAll('<[^<>]+>', '').replaceAll('<[^<>]+>', '')
        return lineWithoutGenericType.matches('^(([A-Z][a-z]+)+|def|int|boolean) \\w+;?$') ||
                lineWithoutGenericType.matches('^(\\w+)\\s+(\\w+) ?=.*$')
    }
}
