package me.trouper.alias.data;

import java.util.ArrayList;
import java.util.List;

public class DebugConfig {
    public boolean debugMode = false;
    public List<String> debuggerExclusions = new ArrayList<>();

    DebugConfig() {
        this.debugMode = false;
        debuggerExclusions = new ArrayList<>();
    }
}
