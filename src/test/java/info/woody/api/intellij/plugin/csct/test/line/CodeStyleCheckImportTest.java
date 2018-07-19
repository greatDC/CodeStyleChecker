package info.woody.api.intellij.plugin.csct.test.line;

import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckIssues;
import info.woody.api.intellij.plugin.csct.test.BaseUnitTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Test class for {@link info.woody.api.intellij.plugin.csct.CodeStyleCheckRuleImpl} to test import patterns.
 *
 * @author Woody
 */
public class CodeStyleCheckImportTest extends BaseUnitTest {

    @Test
    public void groovyMethod() {
        rule.GIT_FILES_TO_MERGE = SAMPLE_PROJECT_SRC + "/src/main/groovy/info/woody/api/intellij/plugin/csct/sample/groovy/imports/SampleGroovyImportIssue.groovy";
        List<String> errors = getLineErrors();

        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_IMPORT_ASTERISK));
        Assert.assertTrue(errors.contains(CodeStyleCheckIssues.LINE_NEVER_USED_IMPORTED));
    }
}
