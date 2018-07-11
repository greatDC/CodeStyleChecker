package info.woody.api.intellij.plugin.csct.test.line;

import info.woody.api.intellij.plugin.csct.CodeStyleCheckRule;
import info.woody.api.intellij.plugin.csct.CodeStyleCheckRuleImpl;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDetailData;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckDetailFileData;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckGlobalError;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckLineError;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReport;
import info.woody.api.intellij.plugin.csct.test.BaseUnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CodeStyleCheckMethodTest extends BaseUnitTest {

//    CodeStyleCheckIssues.LINE_UNUSED_METHOD
//    CodeStyleCheckIssues.LINE_TEST_METHOD_PREFIX_WRONG
//    CodeStyleCheckIssues.LINE_GROOVY_PUBLIC_IN_METHOD
    @Test
    public void groovyMethod() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/main/groovy/info/woody/api/intellij/plugin/csct/sample/groovy/method/SampleGroovyMethodIssue.groovy";
        List<String> errors = getLineErrors();

        Assert.assertEquals(2, errors.stream().filter(error -> error.contains(CodeStyleCheckIssues.LINE_UNUSED_METHOD)).count());
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_GROOVY_PUBLIC_IN_METHOD));
    }

    @Test
    public void javaMethod() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/test/java/info/woody/api/intellij/plugin/csct/sample/java/field/SampleJavaMethodIssueTest.java";
        List<String> errors = getLineErrors();

        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_TEST_METHOD_PREFIX_WRONG));
    }
}
