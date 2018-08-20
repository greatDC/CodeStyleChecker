package info.woody.api.intellij.plugin.csct.test.global;

import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;
import info.woody.api.intellij.plugin.csct.test.BaseUnitTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CodeStyleCheckGlobalIssueTest extends BaseUnitTest {

    @Test
    public void javaGlobalIssue() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_FOLDER + "/src/main/java/info/woody/api/intellij/plugin/csct/sample/java/global/SampleJavaGlobalIssue.java";
        List<String> errors = getGlobalErrors();
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_FILE_END_EMPTY_LINE));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_NO_AUTHORS));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_CONSECUTIVE_EMPTY_LINES));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_STATIC_FINAL));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_TODO_FIXME_HACK_XXX));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_LAST_METHOD_HAS_TAILING_EMPTY_LINE));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_MORE_THAN_500_LINES));

        if (false) {
            // TODO missing test for below cases
            Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_MISS_ERROR_CODE_TEST));
            Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_MOCKITO_ORDER));
        }
    }

    @Test
    public void groovyGlobalIssue() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_FOLDER + "/src/main/groovy/info/woody/api/intellij/plugin/csct/sample/groovy/global/SampleGroovyGlobalIssue.groovy";
        List<String> errors = getGlobalErrors();
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_FILE_END_EMPTY_LINE));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_NO_AUTHORS));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_CONSECUTIVE_EMPTY_LINES));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_STATIC_FINAL));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_TODO_FIXME_HACK_XXX));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_LAST_METHOD_HAS_TAILING_EMPTY_LINE));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_MORE_THAN_500_LINES));

        if (false) {
            // TODO missing test for below cases
            Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_MISS_ERROR_CODE_TEST));
            Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_MOCKITO_ORDER));
        }
    }
}
