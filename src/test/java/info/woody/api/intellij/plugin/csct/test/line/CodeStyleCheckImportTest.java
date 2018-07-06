package info.woody.api.intellij.plugin.csct.test.line;

import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;
import info.woody.api.intellij.plugin.csct.test.BaseUnitTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CodeStyleCheckImportTest extends BaseUnitTest {

//    CodeStyleCheckIssues.LINE_UNUSED_METHOD
//    CodeStyleCheckIssues.LINE_TEST_METHOD_PREFIX_WRONG
//    CodeStyleCheckIssues.LINE_GROOVY_PUBLIC_IN_METHOD
    @Test
    public void groovyMethod() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/main/groovy/info/woody/api/intellij/plugin/csct/sample/groovy/imports/SampleGroovyImportIssue.groovy";
        List<String> errors = getLineErrors();

        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_IMPORT_ASTERISK));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_NEVER_USED_IMPORTED));
    }
}
