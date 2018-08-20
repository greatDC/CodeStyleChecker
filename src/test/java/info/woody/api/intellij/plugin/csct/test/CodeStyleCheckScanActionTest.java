package info.woody.api.intellij.plugin.csct.test;

import info.woody.api.intellij.plugin.csct.CodeStyleCheckException;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckGlobalError;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReportData;
import info.woody.api.intellij.plugin.csct.core.CodeStyleCheckRuleImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Test class for {@link CodeStyleCheckRuleImpl} to test initialisation.
 *
 * @author Woody
 */
public class CodeStyleCheckScanActionTest extends BaseUnitTest {

    @Test
    public void success() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_FOLDER + "src/main/groovy/info/woody/api/intellij/plugin/csct/sample/java/global/SampleGroovyGlobalIssue.groovy";
        CodeStyleCheckReportData codeStyleCheckReportData = rule.doCheck();
        Assert.assertNotNull(codeStyleCheckReportData);
        Assert.assertNotNull(codeStyleCheckReportData.getSummaryData().getGlobalErrorsGroupByFilePath());
        Assert.assertNotEquals(0, codeStyleCheckReportData.getSummaryData().getGlobalErrorsGroupByFilePath().size());
    }

    @Test(expected = CodeStyleCheckException.class)
    public void emptySourceDir() {
        rule.MY_SOURCE_DIR = null;
        rule.doCheck();
    }

    @Test
    public void emptyGitFilesToMerge() {
        rule.GIT_FILES_TO_MERGE = null;
        CodeStyleCheckReportData codeStyleCheckReportData = rule.doCheck();
        Map<String, List<CodeStyleCheckGlobalError>> globalErrorsGroupByFilePath =
                codeStyleCheckReportData.getSummaryData().getGlobalErrorsGroupByFilePath();
        Assert.assertNotNull(globalErrorsGroupByFilePath);
        Assert.assertEquals(0, globalErrorsGroupByFilePath.size());
    }

    @Test
    public void emptyFilesToSkip() {
        rule.FILES_TO_SKIP = null;
        CodeStyleCheckReportData codeStyleCheckReportData = rule.doCheck();
        Assert.assertNotNull(codeStyleCheckReportData);
    }

    @Test
    public void emptyFileNamePatternToSkip() {
        rule.FILENAME_PATTERN_TO_SKIP = null;
        CodeStyleCheckReportData codeStyleCheckReportData = rule.doCheck();
        Assert.assertNotNull(codeStyleCheckReportData);
    }

    @Test
    public void errorFileNamePatternToSkip() {
        rule.FILENAME_PATTERN_TO_SKIP = "a$$]";
        CodeStyleCheckReportData codeStyleCheckReportData = rule.doCheck();
        Assert.assertNotNull(codeStyleCheckReportData);
    }
}
