package me.trouper.alias;

import me.trouper.alias.server.Manager;
import me.trouper.alias.utils.visual.BlockDisplayRaytracer;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class Alias extends JavaPlugin {

    private static Alias instance;
    private Manager manager;

    @Override
    public void onLoad() {
        getLogger().info("Instantiating Plugin");
        instance = this;
    }

    @Override
    public void onEnable() {
        getLogger().info("Instantiating Manager");
        manager = new Manager();

        getLogger().info("Initializing Manager");
        manager.init();

        getLogger().info("Successfully enabled TrimAlias.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Saved all IO files.");
        manager.io.saveAll();
        getLogger().info("Saved all IO files.");
    }

    public static me.trouper.alias.Alias getInstance() {
        return instance;
    }
    public NamespacedKey getNameSpace() {
        return new NamespacedKey(getInstance(),"_alias");
    }
    public Manager getManager() {
        return manager;
    }
}
