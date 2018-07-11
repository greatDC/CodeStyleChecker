package info.woody.api.intellij.plugin.csct.test.line;

import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;
import info.woody.api.intellij.plugin.csct.test.BaseUnitTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CodeStyleCheckOtherTest extends BaseUnitTest {

//LINE_CONSTANT_AS_LEFT_OPERAND
//LINE_ENUM_COMPARE
//LINE_ASSERT
//LINE_CONSTANT_FOR_LITERAL
//LINE_NOT_FORMATTED
//LINE_BAD_VARIABLE_PATTERN
//LINE_CONSTANT_REQUESTPROPERTIES
//LINE_GROOVY_DEF
//LINE_BAD_PRINT
//LINE_EXCEED_140_CHARS
//LINE_MERGE_LINES
//LINE_ENUM_IMPORT
//LINE_IDENTICAL_EXPRESSIONS
//LINE_REDUCE_MULTIPLE_CALCULATION
//LINE_OPTIMIZE_RETURN
//LINE_MOVE_UPPER_ADVICE
//LINE_REDUNDANT_GROOVY_SEMICOLON
//LINE_IMPROPER_ACRONYM
//LINE_BOOLEAN_LITERAL_COMPARE
//LINE_LOG_EXCEPTION

    @Test
    public void javaOthersTest() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/test/groovy/info/woody/api/intellij/plugin/csct/sample/java/other/SampleJavaOtherIssueTest.java";
        List<String> errors = getLineErrors();

        Assert.assertEquals(2, getErrorCount(errors, CodeStyleCheckIssues.LINE_CONSTANT_AS_LEFT_OPERAND));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_ENUM_COMPARE));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_ASSERT));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_CONSTANT_FOR_LITERAL));
        Assert.assertEquals(6, getErrorCount(errors, CodeStyleCheckIssues.LINE_NOT_FORMATTED));
        Assert.assertEquals(2, getErrorCount(errors, CodeStyleCheckIssues.LINE_BAD_VARIABLE_PATTERN));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_CONSTANT_REQUESTPROPERTIES));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_GROOVY_DEF));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_BAD_PRINT));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_EXCEED_140_CHARS));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_MERGE_LINES));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_ENUM_IMPORT));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_IDENTICAL_EXPRESSIONS));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_REDUCE_MULTIPLE_CALCULATION));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_OPTIMIZE_RETURN));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_MOVE_UPPER_ADVICE));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_REDUNDANT_GROOVY_SEMICOLON));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_IMPROPER_ACRONYM));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_BOOLEAN_LITERAL_COMPARE));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_LOG_EXCEPTION));
    }

    @Test
    public void groovyOthers() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/main/java/info/woody/api/intellij/plugin/csct/sample/groovy/other/SampleGroovyOtherIssue.groovy";
        List<String> errors = getLineErrors();

//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_CONSTANT_AS_LEFT_OPERAND));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_ENUM_COMPARE));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_ASSERT));
        Assert.assertEquals(3, getErrorCount(errors, CodeStyleCheckIssues.LINE_CONSTANT_FOR_LITERAL));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_NOT_FORMATTED));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_BAD_VARIABLE_PATTERN));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_CONSTANT_REQUESTPROPERTIES));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_GROOVY_DEF));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_BAD_PRINT));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_EXCEED_140_CHARS));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_MERGE_LINES));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_ENUM_IMPORT));
        Assert.assertEquals(2, getErrorCount(errors, CodeStyleCheckIssues.LINE_IDENTICAL_EXPRESSIONS));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_REDUCE_MULTIPLE_CALCULATION));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_OPTIMIZE_RETURN));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_MOVE_UPPER_ADVICE));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_REDUNDANT_GROOVY_SEMICOLON));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_IMPROPER_ACRONYM));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_BOOLEAN_LITERAL_COMPARE));
//        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_LOG_EXCEPTION));
    }

    @Test
    public void groovyOthersTest() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/test/java/info/woody/api/intellij/plugin/csct/sample/groovy/other/SampleGroovyOtherIssueTest.groovy";
        List<String> errors = getLineErrors();
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_ASSERT));
    }
}
