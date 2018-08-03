package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "CodeStyleCheckConfiguration", storages = {@Storage("CodeStyleCheckConfiguration.xml")})
public class CodeStyleCheckConfigurationService implements PersistentStateComponent<CodeStyleCheckConfigurationState> {

    private CodeStyleCheckConfigurationState state = new CodeStyleCheckConfigurationState();

    @Nullable
    @Override
    public CodeStyleCheckConfigurationState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull CodeStyleCheckConfigurationState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }
}
