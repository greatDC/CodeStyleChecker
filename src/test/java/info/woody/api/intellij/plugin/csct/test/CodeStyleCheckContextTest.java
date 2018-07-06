package info.woody.api.intellij.plugin.csct.test;

import info.woody.api.intellij.plugin.csct.CodeStyleCheckContext;
import info.woody.api.intellij.plugin.csct.CodeStyleCheckRule;
import info.woody.api.intellij.plugin.csct.CodeStyleCheckRuleImpl;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckGlobalError;
import info.woody.api.intellij.plugin.csct.bean.CodeStyleCheckReport;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static info.woody.api.intellij.plugin.csct.test.BaseUnitTest.SAMPLE_PROJECT_SRC;

public class CodeStyleCheckContextTest {

    private CodeStyleCheckRule rule = new CodeStyleCheckRuleImpl();

    @Test(expected = FileNotFoundException.class)
    public void noFilesCodeStyleCheckContext() {
        CodeStyleCheckContext.newInstance(new File("src/test/resources/CodeStyleCheckContextErrorTest.xml"), null);
    }

    @Test
    public void newInstance$CodeStyleCheckContext() {
        CodeStyleCheckContext context = CodeStyleCheckContext.newInstance(new File(SAMPLE_PROJECT_SRC + "main/resources/SampleCodeStyleCheckContext.xml"), null);
        String gitFilesToMerge = Stream.of(
                "src/main/java/info/woody/api/intellij/plugin/csct/CodeStyleCheckContext.groovy",
                "src/main/java/info/woody/api/intellij/plugin/csct/CodeStyleCheckRule.groovy",
                "src/main/java/info/woody/api/intellij/plugin/csct/CodeStyleCheckRuleImpl.groovy",
                "src/test/java/info/woody/api/intellij/plugin/csct/CodeStyleCheckRuleTest.java")
                .collect(Collectors.joining("#"));
        Assert.assertEquals("^.*(Controller).*$", context.FILENAME_PATTERN_TO_SKIP());
        Assert.assertNotNull(context.FILES_TO_SKIP());
        Assert.assertArrayEquals(Stream.of("ErrorCodes.java", "ConfigService.java", "TestHelper.java").toArray(), context.FILES_TO_SKIP().toArray());
        Assert.assertEquals("/Users/renzhengwei/Workstation/Workspace/git/CodeStyleChecker/src", context.MY_SOURCE_DIR());
        Assert.assertNotNull(context.GIT_FILES_TO_MERGE());
        Assert.assertEquals(gitFilesToMerge, context.GIT_FILES_TO_MERGE().trim().replaceAll("\\r?\\n", "").replaceAll(" +", "#"));
    }
}
