package me.trouper.alias;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AliasContextProvider {
    private static final Map<Class<? extends JavaPlugin>, AliasContext> contexts = new ConcurrentHashMap<>();

    /**
     * Register a context for a plugin
     * This must be called BEFORE {@link AliasContext#initialize()}.
     */
    public static void registerContext(JavaPlugin plugin, AliasContext context) {
        contexts.put(plugin.getClass(), context);
    }

    /**
     * Get context for a plugin class
     */
    public static AliasContext getContext(Class<? extends JavaPlugin> pluginClass) {
        AliasContext context = contexts.get(pluginClass);
        if (context == null) {
            throw new RuntimeException("No Alias context registered for " + pluginClass.getSimpleName() +
                    ". Make sure to call AliasContext.initialize() in your plugin's onEnable() method!");
        }
        return context;
    }

    /**
     * Remove context for a plugin
     */
    public static void removeContext(Class<? extends JavaPlugin> pluginClass) {
        contexts.remove(pluginClass);
    }

    /**
     * Check if context exists for a plugin
     */
    public static boolean hasContext(Class<? extends JavaPlugin> pluginClass) {
        return contexts.containsKey(pluginClass);
    }
}
