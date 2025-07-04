package me.trouper.alias.server;

import me.trouper.alias.AliasContext;
import me.trouper.alias.data.DataManager;
import me.trouper.alias.data.JsonSerializable;
import me.trouper.alias.server.commands.QuickCommand;
import me.trouper.alias.server.events.QuickListener;
import me.trouper.alias.server.systems.AbstractWand;
import me.trouper.alias.utils.misc.ReflectionUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class AutoRegistrar {
    private final JavaPlugin plugin;
    private final List<QuickCommand> quickCommands = new ArrayList<>();
    private final List<QuickListener> quickListeners = new ArrayList<>();
    private final List<AbstractWand> wands = new ArrayList<>();
    private final List<JsonSerializable<?>> serializables = new ArrayList<>();

    public AutoRegistrar(AliasContext context) {
        this.plugin = context.getPlugin();
    }

    public void loadAll(String basePackage) {
        Set<Class<?>> classes = ReflectionUtils.getClassesInPackage(plugin, basePackage);

        for (Class<?> clazz : classes) {
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) ||
                    clazz.isEnum() || clazz.isAnnotation()) continue;

            boolean isCommand = QuickCommand.class.isAssignableFrom(clazz);
            boolean isWand = AbstractWand.class.isAssignableFrom(clazz);
            boolean isListener = QuickListener.class.isAssignableFrom(clazz);
            boolean isSerializable = JsonSerializable.class.isAssignableFrom(clazz);

            if (!isCommand && !isWand && !isListener && !isSerializable) continue;

            Registrar registrarFlags = clazz.getAnnotation(Registrar.class);
            if (registrarFlags != null && registrarFlags.exclude()) {
                plugin.getLogger().info("Excluding " + clazz.getSimpleName() + " from the Registrar.");
                continue;
            }

            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();

                if (instance instanceof QuickCommand command) {
                    quickCommands.add(command);
                    plugin.getLogger().info("Found QuickCommand: " + clazz.getSimpleName());
                }

                if (instance instanceof QuickListener listener && !(instance instanceof AbstractWand)) {
                    quickListeners.add(listener);
                    plugin.getLogger().info("Found QuickListener: " + clazz.getSimpleName());
                }

                if (instance instanceof AbstractWand wand) {
                    wands.add(wand);
                    plugin.getLogger().info("Found AbstractWand: " + clazz.getSimpleName());
                }

                if (instance instanceof JsonSerializable<?> js) {
                    serializables.add(js);
                    plugin.getLogger().info("Found JsonSerializable: " + clazz.getSimpleName());
                }

            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Failed to instantiate: " + clazz.getName(), t);
            }
        }

        quickListeners.forEach(QuickListener::register);
        quickCommands.forEach(QuickCommand::register);
    }

    public void unregisterAll() {
        quickListeners.forEach(QuickListener::unregister);
        quickListeners.clear();
        quickCommands.forEach(QuickCommand::disable);
        quickCommands.clear();
    }

    public List<QuickCommand> getQuickCommands() { return quickCommands; }
    public List<QuickListener> getQuickListeners() { return quickListeners; }
    public List<AbstractWand> getWands() { return wands; }
    public List<JsonSerializable<?>> getSerializables() { return serializables; }
}
