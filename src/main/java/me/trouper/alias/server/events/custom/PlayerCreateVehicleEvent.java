package me.trouper.alias.server.events.custom;

import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerCreateVehicleEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final Vehicle vehicle;
    private boolean cancelled;

    public PlayerCreateVehicleEvent(Player player, Vehicle vehicle) {
        this.player = player;
        this.vehicle = vehicle;
    }

    public Player getPlayer() {
        return player;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
