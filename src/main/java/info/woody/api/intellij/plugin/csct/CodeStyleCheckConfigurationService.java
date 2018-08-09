package info.woody.api.intellij.plugin.csct;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Plugin configuration service.
 *
 * @author zhengwei.ren
 * @since 04/08/2018
 */
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
