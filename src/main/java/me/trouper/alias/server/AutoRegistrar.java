package me.trouper.alias.server;

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

    public AutoRegistrar(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadAll(String basePackage) {
        Set<Class<?>> classes = ReflectionUtils.getClassesInPackage(plugin, basePackage);

        for (Class<?> clazz : classes) {
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()) || clazz.isEnum() || clazz.isAnnotation()) {
                continue;
            }

            boolean isCommand = QuickCommand.class.isAssignableFrom(clazz);
            boolean isWand = AbstractWand.class.isAssignableFrom(clazz);
            boolean isListener = QuickListener.class.isAssignableFrom(clazz);

            if (!isCommand && !isWand && !isListener) continue;

            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();

                if (instance instanceof QuickCommand command) {
                    command.register();
                    quickCommands.add(command);
                    plugin.getLogger().info("Registered QuickCommand: " + clazz.getSimpleName());
                }

                if (instance instanceof AbstractWand wand) {
                    wand.register();
                    wands.add(wand);
                    plugin.getLogger().info("Registered AbstractWand: " + clazz.getSimpleName());
                }

                else if (instance instanceof QuickListener listener) {
                    listener.register();
                    quickListeners.add(listener);
                    plugin.getLogger().info("Registered QuickListener: " + clazz.getSimpleName());
                }

            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Failed to instantiate: " + clazz.getName(), t);
            }
        }
    }

    public List<QuickCommand> getQuickCommands() {
        return quickCommands;
    }

    public List<QuickListener> getQuickListeners() {
        return quickListeners;
    }

    public List<AbstractWand> getWands() {
        return wands;
    }
}
