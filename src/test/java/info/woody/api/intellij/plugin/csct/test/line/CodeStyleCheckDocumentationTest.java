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
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_GROOVY_NULL_CHECK));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_UNNECESSARY_EMPTY_CHECK));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_UNNECESSARY_NULL_CHECK));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_METHOD_VERB_STARTS));
        Assert.assertEquals(3, errors.stream().filter(CodeStyleCheckIssues.LINE_SNAKE_CASE_NAMING::equals).count());
    }

    @Test
    public void javaDocumentation() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/main/groovy/info/woody/api/intellij/plugin/csct/sample/java/documentation/SampleJavaDocumentationIssue.java";
        List<String> errors = getLineErrors();

        Assert.assertEquals(3, errors.stream().filter(error -> error.contains(CodeStyleCheckIssues.LINE_DOCUMENTATION_FORMAT)).count());
        Assert.assertEquals(3, errors.stream().filter(CodeStyleCheckIssues.LINE_NO_DOCUMENTATION_CONTENT::equals).count());
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_INCORRECT_CREATION_DATE_FORMAT));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_CODE_IN_DOCUMENTATION));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_EMPTY_STATEMENT));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_DOCUMENTATION_REDUNDANT_EMPTY_LINES));

        errors = getGlobalErrors();
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_CONSTRUCTOR_AFTER_METHOD));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_STATIC_AFTER_FIELD));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.GLOBAL_CONSTANT_AFTER_FIELD));
    }
}
