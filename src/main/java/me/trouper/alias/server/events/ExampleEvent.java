package me.trouper.alias.server.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class ExampleEvent implements QuickListener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        info(e.getPlayer(),"This server is running Alias v{0}.",main.getPlugin().getDescription().getVersion());
    }
}
