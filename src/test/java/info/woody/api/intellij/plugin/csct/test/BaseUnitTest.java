package info.woody.api.intellij.plugin.csct.test;

import info.woody.api.intellij.plugin.csct.CodeStyleCheckConfigurationState;
import info.woody.api.intellij.plugin.csct.core.CodeStyleCheckRule;
import info.woody.api.intellij.plugin.csct.core.CodeStyleCheckRuleImpl;
import info.woody.api.intellij.plugin.csct.bean.*;
import org.junit.Assert;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for all unit test classes.
 *
 * @author Woody
 */
public class BaseUnitTest {

    public static final String SAMPLE_PROJECT_FOLDER;
    static {
        String path = null;
        try {
            path = Paths.get(".").toFile().getCanonicalPath().concat("/../Examples/");
        } catch (IOException e) {
            Assert.fail("info.woody.api.intellij.plugin.csct.test.BaseUnitTest.SAMPLE_PROJECT_SRC is not set!");
        }
        SAMPLE_PROJECT_FOLDER = path;
    }

    protected CodeStyleCheckRule rule = new CodeStyleCheckRuleImpl();

    /**
     * Set up prerequisites.
     */
    @Before
    public void setUpBase() {
        rule.PROGRESS = (a, b) -> false;
        rule.MY_SOURCE_DIR = SAMPLE_PROJECT_FOLDER;
        rule.FILES_TO_SKIP = new ArrayList<>();
        rule.FILENAME_PATTERN_TO_SKIP = "^.*(Controller).*$";
        CodeStyleCheckConfigurationState settings = new CodeStyleCheckConfigurationState();
        settings.setTestPrefixInTestForced(true);
        rule.SETTINGS = settings;
    }

    /**
     * Get global errors.
     *
     * @return Global errors.
     */
    protected List<String> getGlobalErrors() {
        return getErrors(true);
    }

    /**
     * Get line errors.
     *
     * @return Line errors.
     */
    protected List<String> getLineErrors() {
        return getErrors(false);
    }

    /**
     * Get errors.
     *
     * @param isGlobal Flag to indicate which kind of errors is going to get.
     * @return Errors.
     */
    private List<String> getErrors(boolean isGlobal) {
        CodeStyleCheckReportData report = rule.doCheck();
        CodeStyleCheckDetailData detailData = report.getDetailData();
        Assert.assertNotNull(detailData);
        List<CodeStyleCheckDetailFileData> fileDataList = detailData.getFileDataList();
        assertNonEmptyList(fileDataList);
        if (isGlobal) {
            List<CodeStyleCheckGlobalError> errorList = fileDataList.get(0).getGlobalErrorList();
            assertNonEmptyList(errorList);
            return errorList.stream().map(CodeStyleCheckGlobalError::getError).collect(Collectors.toList());
        } else {
            List<CodeStyleCheckLineError> errorList = fileDataList.get(0).getLineErrorList();
            assertNonEmptyList(errorList);
            return errorList.stream().map(CodeStyleCheckLineError::getError).collect(Collectors.toList());
        }
    }

    /**
     * Get error count.
     *
     * @param errors       Errors.
     * @param errorMessage Error message.
     * @return Error count.
     */
    protected long getErrorCount(List<String> errors, String errorMessage) {
        return errors.stream().filter(error -> error.contains(errorMessage)).count();
    }

    /**
     * Assertion to check if the given list is empty.
     *
     * @param list A list.
     */
    private void assertNonEmptyList(List<?> list) {
        Assert.assertNotNull(list);
        Assert.assertNotEquals(0, list.size());
    }
}
