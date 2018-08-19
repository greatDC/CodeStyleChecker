package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Plugin configuration state.
 *
 * @author zhengwei.ren
 * @since 04/08/2018
 */
@State(name = "CodeStyleCheckConfigurationService", storages = {@Storage("CodeStyleCheckConfigurationService.xml")})
public class CodeStyleCheckConfigurationState {

    private boolean experimentalEnabled = true;
    private boolean testPrefixInTestForced = false;
    private List<String> badNamingSkipList = Stream.of("SuperPNR", "GPath", "SPNR", "PNR").collect(Collectors.toList());

    /**
     * @return
     */
    public boolean isExperimentalEnabled() {
        return experimentalEnabled;
    }

    /**
     * @param experimentalEnabled
     */
    public void setExperimentalEnabled(boolean experimentalEnabled) {
        this.experimentalEnabled = experimentalEnabled;
    }

    /**
     * Indicate if the method name in test class has to start with `test` prefix.
     *
     * @return
     */
    public boolean isTestPrefixInTestForced() {
        return testPrefixInTestForced;
    }

    /**
     * Indicate if the method name in test class has to start with `test` prefix.
     *
     * @param testPrefixInTestForced
     */
    public void setTestPrefixInTestForced(boolean testPrefixInTestForced) {
        this.testPrefixInTestForced = testPrefixInTestForced;
    }

    public List<String> getBadNamingSkipList() {
        return badNamingSkipList;
    }

    public void setBadNamingSkipList(List<String> badNamingSkipList) {
        this.badNamingSkipList = badNamingSkipList;
    }

}
