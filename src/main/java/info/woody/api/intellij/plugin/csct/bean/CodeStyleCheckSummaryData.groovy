package info.woody.api.intellij.plugin.csct.bean

/**
 * Summary data.
 * @author Woody
 * @since 15/06/2018
 */
class CodeStyleCheckSummaryData {
    Set<String> authorsKeySet = new TreeSet<>({ String a, String b -> a.compareTo b })
    List<CodeStyleCheckSummaryFileData> fileDataList = new ArrayList<>()
    int fileCountWithIssues
    int fileCountWithoutIssues
    Map<String, List<CodeStyleCheckGlobalError>> globalErrorsGroupByFilePath = new LinkedHashMap<>()
    Map<String, List<CodeStyleCheckLineError>> lineErrorsGroupByFilePath = new LinkedHashMap<>()
    Map<String, List<String>> filesGroupByError = new LinkedHashMap<>()
}
