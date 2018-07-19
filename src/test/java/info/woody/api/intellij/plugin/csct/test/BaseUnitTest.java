package info.woody.api.intellij.plugin.csct.test;

import info.woody.api.intellij.plugin.csct.CodeStyleCheckRule;
import info.woody.api.intellij.plugin.csct.CodeStyleCheckRuleImpl;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDetailData;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDetailFileData;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckGlobalError;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckLineError;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReport;
import org.junit.Assert;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BaseUnitTest {

    public static final String SAMPLE_PROJECT_SRC = "/Users/renzhengwei/Workstation/Workspace/git/Examples/src/";

    protected CodeStyleCheckRule rule = new CodeStyleCheckRuleImpl();

    @Before
    public void setUpBase() {
        rule.PROGRESS = (a, b) -> false;
        rule.MY_SOURCE_DIR = SAMPLE_PROJECT_SRC;
        rule.FILES_TO_SKIP = new ArrayList<>();
        rule.FILENAME_PATTERN_TO_SKIP = "^.*(Controller).*$";
    }

    protected List<String> getGlobalErrors() {
        return getErrors(true);
    }

    protected List<String> getLineErrors() {
        return getErrors(false);
    }

    private List<String> getErrors(boolean isGlobal) {
        CodeStyleCheckReport report = rule.doCheck();
        CodeStyleCheckDetailData detailData = report.getDetailData();
        Assert.assertNotNull(detailData);
        List<CodeStyleCheckDetailFileData> fileDataList = detailData.getFileDataList();
        assertNonEmptyList(fileDataList);
        if (isGlobal) {
            List<CodeStyleCheckGlobalError> errorList = fileDataList.get(0).getGlobalErrorList();
            assertNonEmptyList(errorList);
            return errorList.stream().map(CodeStyleCheckGlobalError::getError).collect(Collectors.toList());
        } else {
            List<CodeStyleCheckLineError> errorList = fileDataList.get(0).getLineErrorList();
            assertNonEmptyList(errorList);
            return errorList.stream().map(CodeStyleCheckLineError::getError).collect(Collectors.toList());
        }
    }

    protected long getErrorCount(List<String> errors, String errorMessage) {
        return errors.stream().filter(error -> error.contains(errorMessage)).count();
    }

    private void assertNonEmptyList(List<?> list) {
        Assert.assertNotNull(list);
        Assert.assertNotEquals(0, list.size());
    }
}
