package info.woody.api.intellij.plugin.csct.test.line;

import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;
import info.woody.api.intellij.plugin.csct.test.BaseUnitTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CodeStyleCheckFieldTest extends BaseUnitTest {
//    CodeStyleCheckIssues.LINE_GROOVY_PUBLIC_IN_FIELD
//    CodeStyleCheckIssues.LINE_UNUSED_FIELD
//    CodeStyleCheckIssues.LINE_FIELD_NAME_CONVENTION
//    CodeStyleCheckIssues.LINE_LOGGER_NAME_CONVENTION
//    CodeStyleCheckIssues.LINE_LOGGER_TARGET_CLASS
//    CodeStyleCheckIssues.LINE_CONSTANT_NAME_CONVENTION
//    CodeStyleCheckIssues.LINE_FIELD_MODIFIER_FOR_SERVICE
//    CodeStyleCheckIssues.LINE_FIELD_MODIFIER_FOR_CONTROLLER

    @Test
    public void groovyField() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/main/groovy/info/woody/api/intellij/plugin/csct/sample/groovy/field/SampleGroovyFieldIssueService.groovy";
        List<String> errors = getLineErrors();

        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_GROOVY_PUBLIC_IN_FIELD));
    }

    @Test
    public void javaField() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/main/java/info/woody/api/intellij/plugin/csct/sample/java/field/SampleJavaFieldIssueService.java";
        List<String> errors = getLineErrors();

        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_UNUSED_FIELD)); // TODO test with more examples
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_FIELD_NAME_CONVENTION));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_LOGGER_NAME_CONVENTION));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_LOGGER_TARGET_CLASS));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_CONSTANT_NAME_CONVENTION));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_FIELD_MODIFIER_FOR_SERVICE));
        // Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_FIELD_MODIFIER_FOR_CONTROLLER));
    }
}
