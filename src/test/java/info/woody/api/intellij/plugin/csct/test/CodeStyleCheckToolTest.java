package info.woody.api.intellij.plugin.csct.test;

import info.woody.api.intellij.plugin.csct.CodeStyleCheckException;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckGlobalError;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReport;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class CodeStyleCheckToolTest extends BaseUnitTest {

    @Test
    public void success() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "src/main/groovy/info/woody/api/intellij/plugin/csct/sample/java/global/SampleGroovyGlobalIssue.groovy";
        CodeStyleCheckReport codeStyleCheckReport = rule.doCheck();
        Assert.assertNotNull(codeStyleCheckReport);
        Assert.assertNotNull(codeStyleCheckReport.getSummaryData().getGlobalErrorsGroupByFilePath());
        Assert.assertNotEquals(0, codeStyleCheckReport.getSummaryData().getGlobalErrorsGroupByFilePath().size());
    }

    @Test(expected = CodeStyleCheckException.class)
    public void emptySourceDir() {
        rule.MY_SOURCE_DIR = null;
        rule.doCheck();
    }

    @Test
    public void emptyGitFilesToMerge() {
        rule.GIT_FILES_TO_MERGE = null;
        CodeStyleCheckReport codeStyleCheckReport = rule.doCheck();
        Map<String, List<CodeStyleCheckGlobalError>> globalErrorsGroupByFilePath = codeStyleCheckReport.getSummaryData().getGlobalErrorsGroupByFilePath();
        Assert.assertNotNull(globalErrorsGroupByFilePath);
        Assert.assertEquals(0, globalErrorsGroupByFilePath.size());
    }

    @Test
    public void emptyFilesToSkip() {
        rule.FILES_TO_SKIP = null;
        CodeStyleCheckReport codeStyleCheckReport = rule.doCheck();
        Assert.assertNotNull(codeStyleCheckReport);
    }

    @Test
    public void emptyFileNamePatternToSkip() {
        rule.FILENAME_PATTERN_TO_SKIP = null;
        CodeStyleCheckReport codeStyleCheckReport = rule.doCheck();
        Assert.assertNotNull(codeStyleCheckReport);
    }

    @Test
    public void errorFileNamePatternToSkip() {
        rule.FILENAME_PATTERN_TO_SKIP = "a$$]";
        CodeStyleCheckReport codeStyleCheckReport = rule.doCheck();
        Assert.assertNotNull(codeStyleCheckReport);
    }
}
