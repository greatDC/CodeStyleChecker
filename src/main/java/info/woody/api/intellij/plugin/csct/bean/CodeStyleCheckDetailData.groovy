package info.woody.api.intellij.plugin.csct.bean

/**
 * Detail data.
 * @author Woody
 * @since 15/06/2018
 */
class CodeStyleCheckDetailData {
    Map<String, Integer> mapAuthorsErrors = new LinkedHashMap()
    List<CodeStyleCheckDetailFileData> fileDataList = new ArrayList<>()
}
