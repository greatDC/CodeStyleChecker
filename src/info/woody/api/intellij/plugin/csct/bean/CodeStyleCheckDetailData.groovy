package info.woody.api.intellij.plugin.csct.bean

class CodeStyleCheckDetailData {
    Map<String, Integer> mapAuthorsErrors = new LinkedHashMap()
    List<CodeStyleCheckDetailFileData> fileDataList = new ArrayList<>()
}
