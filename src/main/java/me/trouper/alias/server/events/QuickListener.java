package me.trouper.alias.server.events;

import me.trouper.alias.server.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public interface QuickListener extends Listener, Main {
    default void register() {
        Bukkit.getPluginManager().registerEvents(this,main.getPlugin());
    }

    default void unregister() {
        HandlerList.unregisterAll(this);
    }
}
