package me.trouper.alias.server.events.custom;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerSpawnEntityEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Entity spawnedEntity;
    private boolean cancelled = false;

    public PlayerSpawnEntityEvent(Player player, Entity spawnedEntity) {
        this.player = player;
        this.spawnedEntity = spawnedEntity;
    }

    public Player getPlayer() {
        return player;
    }

    public Entity getSpawnedEntity() {
        return spawnedEntity;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}