package info.woody.api.intellij.plugin.csct.test.line;

import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;
import info.woody.api.intellij.plugin.csct.core.CodeStyleCheckRuleImpl;
import info.woody.api.intellij.plugin.csct.test.BaseUnitTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Test class for {@link CodeStyleCheckRuleImpl} to test method patterns.
 *
 * @author Woody
 */
public class CodeStyleCheckMethodTest extends BaseUnitTest {

    @Test
    public void groovyMethod() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_FOLDER + "/src/main/groovy/info/woody/api/intellij/plugin/csct/sample/groovy/method/SampleGroovyMethodIssue.groovy";
        List<String> errors = getLineErrors();

        Assert.assertEquals(2, errors.stream().filter(error -> error.contains(CodeStyleCheckIssues.LINE_UNUSED_METHOD)).count());
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_GROOVY_PUBLIC_IN_METHOD));
    }

    @Test
    public void javaMethod() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_FOLDER + "/src/test/java/info/woody/api/intellij/plugin/csct/sample/java/field/SampleJavaMethodIssueTest.java";
        List<String> errors = getLineErrors();

        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_TEST_METHOD_PREFIX_WRONG));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_TEST_METHOD_SETUP));
    }
}
