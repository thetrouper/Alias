package me.trouper.alias.data;

import java.util.HashSet;
import java.util.Set;

public class Common {

    private final String packageName;
    private int mainColor;
    private int secondaryColor;
    private String pluginName;
    private String flatPrefix;
    private boolean flat;
    private boolean debugMode;
    private final String updateURL;
    private final Set<String> debuggerExclusions;

    public Common(String packageName, int mainColor, int secondaryColor, String pluginName, String flatPrefix, boolean flat, String updateURL) {
        this.packageName = packageName;
        this.mainColor = mainColor;
        this.secondaryColor = secondaryColor;
        this.pluginName = pluginName;
        this.flatPrefix = flatPrefix;
        this.flat = flat;
        this.updateURL = updateURL;
        this.debugMode = false;
        this.debuggerExclusions = new HashSet<>();
    }

     public void update(Common common) {
        this.mainColor = common.getMainColor();
        this.secondaryColor = common.getSecondaryColor();
        this.pluginName = common.getPluginName();
        this.flatPrefix = common.getFlatPrefix();
        this.flat = common.isFlat();
        this.debugMode = common.getDebugMode();
        this.debuggerExclusions.clear();
        this.debuggerExclusions.addAll(common.getDebuggerExclusions());
    }

    public String getPackageName() {
        return packageName;
    }

    public int getMainColor() {
        return mainColor;
    }

    public void setMainColor(int mainColor) {
        this.mainColor = mainColor;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getFlatPrefix() {
        return flatPrefix;
    }

    public void setFlatPrefix(String flatPrefix) {
        this.flatPrefix = flatPrefix;
    }

    public boolean useFlat() {
        return flat;
    }

    public void setFlat(boolean flat) {
        this.flat = flat;
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }

    public void setSecondaryColor(int secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public boolean getDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean mode) {
        this.debugMode = mode;
    }

    public Set<String> getDebuggerExclusions() {
        return debuggerExclusions;
    }

    public boolean addDebuggerExclusion(String methodName) {
        return this.debuggerExclusions.add(methodName);
    }

    public boolean removeDebuggerExclusion(String methodName) {
        return this.debuggerExclusions.remove(methodName);
    }

    public void setDebuggerExclusions(Set<String> debuggerExclusions) {
        this.debuggerExclusions.clear();
        this.debuggerExclusions.addAll(debuggerExclusions);
    }

    public String getTempTag() {
        return "$/" + pluginName + "/ TEMP";
    }


    public String getUpdateURL() {
        return updateURL;
    }

    public boolean isFlat() {
        return flat;
    }
}
