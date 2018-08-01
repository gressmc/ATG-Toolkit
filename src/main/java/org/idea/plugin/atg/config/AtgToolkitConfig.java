package org.idea.plugin.atg.config;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "AtgToolkitConfig", storages = {@Storage(StoragePathMacros.WORKSPACE_FILE)})
public class AtgToolkitConfig implements PersistentStateComponent<AtgToolkitConfig> {
    public static final String ATG_TOOLKIT_ID = "atg-toolkit";
    private static final String DEFAULT_RELATIVE_CONFIG_PATH = "src/main/config,src/config";
    private static final String DEFAULT_IGNORED_PARENTS = "atg.nucleus.*";

    @SuppressWarnings("WeakerAccess")
    public String ignoredClassesForSetters = DEFAULT_IGNORED_PARENTS;
    @SuppressWarnings("WeakerAccess")
    public String relativeConfigPath = DEFAULT_RELATIVE_CONFIG_PATH;
    public String configRootsPatterns = DEFAULT_RELATIVE_CONFIG_PATH;

    @Nullable
    @Override
    public AtgToolkitConfig getState() {
        AtgToolkitConfig state = new AtgToolkitConfig();
        XmlSerializerUtil.copyBean(this, state);
        return state;
    }

    @Override
    public void loadState(@NotNull AtgToolkitConfig state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @NotNull
    public static AtgToolkitConfig getInstance() {
        AtgToolkitConfig service = ServiceManager.getService(AtgToolkitConfig.class);
        return service != null ? service : new AtgToolkitConfig();
    }

    public String getIgnoredClassesForSetters() {
        return ignoredClassesForSetters;
    }

    public void setIgnoredClassesForSetters(String ignoredClassesForSetters) {
        this.ignoredClassesForSetters = ignoredClassesForSetters;
    }

    public String getRelativeConfigPath() {
        return relativeConfigPath;
    }

    public void setRelativeConfigPath(String relativeConfigPath) {
        this.relativeConfigPath = relativeConfigPath;
    }

    public String getConfigRootsPatterns() {
        return configRootsPatterns;
    }

    public void setConfigRootsPatterns(String configRootsPatterns) {
        this.configRootsPatterns = configRootsPatterns;
    }
}
