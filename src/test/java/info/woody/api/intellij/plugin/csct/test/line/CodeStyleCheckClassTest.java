package info.woody.api.intellij.plugin.csct.test.line;

import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;
import info.woody.api.intellij.plugin.csct.test.BaseUnitTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CodeStyleCheckClassTest extends BaseUnitTest {

    @Test
    public void groovyClass() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_FOLDER + "/src/main/groovy/info/woody/api/intellij/plugin/csct/sample/groovy/clazz/SampleGroovyClassIssue.groovy";
        List<String> errors = getLineErrors();
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_GROOVY_PUBLIC_IN_CLASS));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_MISSING_UNIT_TEST));
    }

    @Test
    public void javaClass() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_FOLDER + "/src/test/java/info/woody/api/intellij/plugin/csct/sample/java/clazz/SampleJavaClassIssueTest.java";
        List<String> errors = getLineErrors();
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_UNIT_TEST_PRIVATE_FIELD));
    }
}
