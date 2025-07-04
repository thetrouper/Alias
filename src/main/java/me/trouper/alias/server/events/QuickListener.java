package me.trouper.alias.server.events;

import me.trouper.alias.server.ContextAware;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public interface QuickListener extends Listener, ContextAware {

    default void register() {
        getPlugin().getLogger().info("Registering Listeners from " + this.getClass().getSimpleName());
        Bukkit.getPluginManager().registerEvents(this,getPlugin());
    }

    default void unregister() {
        HandlerList.unregisterAll(this);
    }
}
