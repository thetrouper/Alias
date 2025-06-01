package me.trouper.alias.data;

import me.trouper.alias.Alias;
import me.trouper.alias.data.io.Config;
import me.trouper.alias.data.io.Storage;
import me.trouper.alias.utils.misc.JsonSerializable;

import java.io.File;

public class IO {
    public final File DATA_FOLDER;
    public final File CONFIG_FILE;
    public final File STORAGE_FILE;

    public Config config;
    public Storage storage;

    public IO() {
        DATA_FOLDER = new File("plugins/Alias");
        CONFIG_FILE = new File(DATA_FOLDER,"/config.json");
        STORAGE_FILE = new File(DATA_FOLDER,"/storage.json");
        config = JsonSerializable.load(CONFIG_FILE,Config.class,new Config());
        storage = JsonSerializable.load(STORAGE_FILE,Storage.class,new Storage());
    }

    public void loadAll() {
        me.trouper.alias.Alias.getInstance().getLogger().info("Loading all IO Files");
        config = JsonSerializable.load(CONFIG_FILE,Config.class,new Config());
        storage = JsonSerializable.load(STORAGE_FILE,Storage.class,new Storage());
    }

    public void saveAll() {
        me.trouper.alias.Alias.getInstance().getLogger().info("Saving all IO Files");
        config.save();
        storage.save();
    }
}
