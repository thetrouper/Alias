package me.trouper.alias;

import me.trouper.alias.data.Common;
import me.trouper.alias.data.DataManager;
import me.trouper.alias.data.JsonSerializable;
import me.trouper.alias.server.AutoRegistrar;
import me.trouper.alias.server.events.listeners.GuiListener;
import me.trouper.alias.server.events.listeners.SpawnListener;
import me.trouper.alias.server.events.listeners.WandListener;
import me.trouper.alias.server.systems.TaskManager;
import me.trouper.alias.server.systems.Text;
import me.trouper.alias.server.systems.Verbose;
import me.trouper.alias.server.systems.display.DisplayManager;
import me.trouper.alias.server.update.AutoUpdater;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class AliasContext {
    private final JavaPlugin plugin;
    private final Common common;
    private final AutoRegistrar autoRegistrar;
    private final AutoUpdater autoUpdater;
    private final DataManager dataManager;
    private final Text text;
    private final Verbose verbose;
    private final DisplayManager displayManager;
    private boolean enabled = false;

    public AliasContext(JavaPlugin plugin, Common common) {
        this.plugin = plugin;
        this.common = common;
        this.autoRegistrar = new AutoRegistrar(this);
        this.autoUpdater = new AutoUpdater(this);
        this.dataManager = new DataManager(this);
        this.text = new Text(this);
        this.verbose = new Verbose(this);
        this.displayManager = new DisplayManager(this);
    }

    /**
     * Initialize the Alias context and register all components.
     * This must be called before using any Alias features.
     * Alias should be registered first.
     * See {@link AliasContextProvider#registerContext(JavaPlugin, AliasContext)}
     */
    public synchronized void initialize() {
        if (enabled) return;

        plugin.getLogger().info("Initializing Alias context for " + plugin.getName());

        autoUpdater.checkUpdate();

        autoRegistrar.loadAll(common.getPackageName());
        Bukkit.getPluginManager().registerEvents(new GuiListener(),getPlugin());
        Bukkit.getPluginManager().registerEvents(new SpawnListener(this),getPlugin());
        Bukkit.getPluginManager().registerEvents(new WandListener(this),getPlugin());
        List<JsonSerializable<?>> copy = new ArrayList<>(autoRegistrar.getSerializables());
        for (JsonSerializable<?> serializable : copy) {
            dataManager.load(serializable.getClass());
        }

        enabled = true;
        plugin.getLogger().info("Alias context initialized successfully");
    }

    /**
     * Shutdown the Alias context, save any {@link me.trouper.alias.data.JsonSerializable} and release resources.
     * This should be called when the plugin is shutting down.
     */
    public synchronized void shutdown() {
        if (!enabled) return;

        plugin.getLogger().info("Shutting down Alias context for " + plugin.getName());

        autoRegistrar.getSerializables().forEach(jsonSerializable -> {
            dataManager.save(jsonSerializable.getClass());
        });
        autoRegistrar.unregisterAll();

        autoUpdater.checkUpdate();

        enabled = false;
        plugin.getLogger().info("Alias context shutdown complete");
    }

    public JavaPlugin getPlugin() { return plugin; }
    public Common getCommon() { return common; }
    public AutoRegistrar getAutoRegistrar() { return autoRegistrar; }
    public Text getText() { return text; }
    public boolean isEnabled() { return enabled; }
    public TaskManager createTaskManager() { return new TaskManager(this); }
    public Verbose getVerbose() { return verbose; }
    public DisplayManager getDisplayManager() { return displayManager; }
    public DataManager getDataManager() { return dataManager; }
    public AutoUpdater getAutoUpdater() { return autoUpdater; }
}