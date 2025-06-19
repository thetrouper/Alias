package me.trouper.alias.server.events;

import me.trouper.alias.server.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public interface QuickListener extends Listener, Main {
    default QuickListener register() {
        Bukkit.getPluginManager().registerEvents(this,this.getPlugin());
        return this;
    }
}
