package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name="CodeStyleCheckConfigurationService",
        storages = {
                @Storage("CodeStyleCheckConfigurationService.xml")}
)
public class CodeStyleCheckConfigurationState {

    private boolean experimentalEnabled = true;

    public boolean isExperimentalEnabled() {
        return experimentalEnabled;
    }

    public void setExperimentalEnabled(boolean experimentalEnabled) {
        this.experimentalEnabled = experimentalEnabled;
    }
}
