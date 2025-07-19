package me.trouper.alias.server.events.listeners;

import me.trouper.alias.AliasContext;
import me.trouper.alias.server.systems.freeze.FreezeSession;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FreezeListener implements Listener {

    private final AliasContext context;

    public FreezeListener(AliasContext context) {
        this.context = context;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        OfflinePlayer p = e.getPlayer();
        FreezeSession freeze = context.getFreezeManager().getSession(p.getUniqueId());
        if (freeze != null) freeze.handleQuit(p);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        FreezeSession freeze = context.getFreezeManager().getSession(p.getUniqueId());
        if (freeze == null) return;
        if (!freeze.canMove()) {
            if (!e.getFrom().toVector().equals(e.getTo().toVector())) {
                e.setTo(e.getFrom());
            }
        }
        freeze.handleMove(p);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        FreezeSession freeze = context.getFreezeManager().getSession(p.getUniqueId());
        if (freeze == null) return;
        if (!freeze.canInt()) e.setCancelled(true);
        freeze.handleInteract(p);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        FreezeSession freeze = context.getFreezeManager().getSession(p.getUniqueId());
        if (freeze == null) return;
        if (!freeze.getAllowedCommands().contains(e.getMessage().split(" ")[0])) {
            e.setCancelled(true);
        }
        freeze.handleCommand(p);
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        FreezeSession freeze = context.getFreezeManager().getSession(p.getUniqueId());
        if (freeze == null) return;
        if (!freeze.canDmg()) e.setCancelled(true);
        freeze.handleDamage(p);
    }

    @EventHandler
    public void onDamageByBlock(EntityDamageByBlockEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        FreezeSession freeze = context.getFreezeManager().getSession(p.getUniqueId());
        if (freeze == null) return;
        if (!freeze.canDmg()) e.setCancelled(true);
        freeze.handleDamage(p);
    }
}
