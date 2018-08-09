package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

/**
 * Plugin configuration state.
 *
 * @author zhengwei.ren
 * @since 04/08/2018
 */
@State(name = "CodeStyleCheckConfigurationService", storages = {@Storage("CodeStyleCheckConfigurationService.xml")})
public class CodeStyleCheckConfigurationState {

    private boolean experimentalEnabled = true;

    /**
     *
     * @return
     */
    public boolean isExperimentalEnabled() {
        return experimentalEnabled;
    }

    /**
     *
     * @param experimentalEnabled
     */
    public void setExperimentalEnabled(boolean experimentalEnabled) {
        this.experimentalEnabled = experimentalEnabled;
    }
}
