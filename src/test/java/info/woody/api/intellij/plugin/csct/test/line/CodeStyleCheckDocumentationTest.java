package info.woody.api.intellij.plugin.csct.test.line;

import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;
import info.woody.api.intellij.plugin.csct.test.BaseUnitTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CodeStyleCheckDocumentationTest extends BaseUnitTest {

    @Test
    public void groovyDocumentation() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/main/groovy/info/woody/api/intellij/plugin/csct/sample/groovy/documentation/SampleGroovyDocumentationIssue.groovy";
        List<String> errors = getLineErrors();

        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_CLASS_MISSING_DOCUMENTATION));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_CONSTRUCTOR_MISSING_DOCUMENTATION));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_METHOD_MISSING_DOCUMENTATION));
    }

    @Test
    public void javaDocumentation() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/main/groovy/info/woody/api/intellij/plugin/csct/sample/java/documentation/SampleJavaDocumentationIssue.java";
        List<String> errors = getLineErrors();

        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_NO_DOCUMENTATION_CONTENT));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_DOCUMENTATION_FORMAT));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_INCORRECT_CREATION_DATE_FORMAT));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_CODE_IN_DOCUMENTATION));
    }
}
