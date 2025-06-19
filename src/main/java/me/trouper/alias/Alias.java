package me.trouper.alias;

import me.trouper.alias.data.Common;
import me.trouper.alias.server.AutoRegistrar;
import me.trouper.alias.server.commands.QuickCommand;
import me.trouper.alias.server.events.GuiListener;
import me.trouper.alias.server.events.QuickListener;
import me.trouper.alias.server.systems.AbstractWand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Alias extends JavaPlugin {

    private static Class<? extends JavaPlugin> host;
    private static AutoRegistrar autoRegistrar;
    private static Common common;
    private static boolean enabled;


    public static synchronized void register(JavaPlugin plugin, Common common) {
        if (plugin == null || enabled) return;
        Alias.host = plugin.getClass();
        Alias.common = common;

        new GuiListener().register();
        autoRegistrar = new AutoRegistrar(plugin);
        autoRegistrar.loadAll(common.getPackageName());

        enabled = true;
    }

    public static Class<? extends JavaPlugin> getHost() {
        return host;
    }

    public static AutoRegistrar getAutoRegistrar() {
        return autoRegistrar;
    }

    public static Common getCommon() {
        return common;
    }

    public static void updateCommon(Common common) {
        Alias.common = common;
    }
}
