package info.woody.api.intellij.plugin.csct.test.line;

import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;
import info.woody.api.intellij.plugin.csct.test.BaseUnitTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CodeStyleCheckCommentAndCurlyBraceTest extends BaseUnitTest {

//    CodeStyleCheckIssues.LINE_UNUSED_METHOD
//    CodeStyleCheckIssues.LINE_TEST_METHOD_PREFIX_WRONG
//    CodeStyleCheckIssues.LINE_GROOVY_PUBLIC_IN_METHOD
    @Test
    public void javaCommentAndCurlyBrace() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/main/groovy/info/woody/api/intellij/plugin/csct/sample/java/comment/SampleJavaCommentAndCurlyBraceIssue.java";
        List<String> errors = getLineErrors();

        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_SINGLE_LINE_COMMENT_FORMAT));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_REDUNDANT_CODE_DESC));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_LEFT_CURLY_BRACE_LINE));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_COMMENTED_OUT_CODES));
    }
}
