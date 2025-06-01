package me.trouper.alias.data.io;

import me.trouper.alias.utils.Verbose;
import me.trouper.alias.utils.misc.JsonSerializable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Config implements JsonSerializable<Config> {

    public boolean debugMode = false;
    public List<String> debuggerExclusions = new ArrayList<>();

    @Override
    public File getFile() {
        return me.trouper.alias.Alias.getInstance().getManager().io.CONFIG_FILE;
    }

    @Override
    public void save() {
        Verbose.send(1,"Saving Config...");
        JsonSerializable.super.save();
    }

    public Messages messages = new Messages();
    
    public class Messages {
        public String mainColor = "&#ffaaff";
        public String prefix = "&9Alias> &7";
        public String pluginName = "Alias";
        public boolean fancyAlerts = true;
    }
}
