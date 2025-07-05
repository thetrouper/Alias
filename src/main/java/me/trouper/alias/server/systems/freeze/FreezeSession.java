package me.trouper.alias.server.systems.freeze;

import me.trouper.alias.AliasContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class FreezeSession {
    private final AliasContext context;
    private final UUID targetUuid;
    private final Location backLocation;
    private final boolean allowMovement;
    private final boolean allowDamage;
    private final Set<String> allowedCommands;
    private final boolean allowInteract;

    private final Consumer<Player> onMove;
    private final Consumer<Player> onInteract;
    private final Consumer<Player> onDamage;
    private final Consumer<Player> onCommand;
    private final Consumer<OfflinePlayer> onQuit;
    private final Runnable onStart;
    private final Runnable onThaw;

    private final Duration duration;
    private BukkitTask thawTask;

    private FreezeSession(AliasContext context, Builder b) {
        this.context = context;
        this.targetUuid = b.targetUuid;
        this.backLocation = b.backLocation;
        this.allowMovement = b.allowMovement;
        this.allowDamage = b.allowDamage;
        this.allowedCommands = b.allowedCommands;
        this.allowInteract = b.allowInteract;

        this.onMove = b.onMove;
        this.onInteract = b.onInteract;
        this.onDamage = b.onDamage;
        this.onCommand = b.onCommand;
        this.onQuit = b.onQuit;
        this.onStart = b.onStart;
        this.onThaw = b.onThaw;

        this.duration = b.duration;
    }

    public void start() {
        context.getFreezeManager().register(this);
        if (onStart != null) onStart.run();

        if (!duration.isZero()) {
            thawTask = context.getPlugin().getServer().getScheduler().runTaskLater(context.getPlugin(), this::thaw, duration.toMillis() / 50);
        }
    }

    public void thaw() {
        context.getFreezeManager().unregister(targetUuid);
        if (thawTask != null) thawTask.cancel();
        Player p = Bukkit.getPlayer(targetUuid);
        if (p != null) {
            p.teleport(backLocation);
        }
        if (onThaw != null) onThaw.run();
    }

    public UUID uuid() { return targetUuid; }
    public boolean canMove() { return allowMovement; }
    public boolean canDmg() { return allowDamage; }
    public Set<String> getAllowedCommands() { return allowedCommands; }
    public boolean canInt() { return allowInteract; }

    public void handleMove(Player p) {
        if (onMove != null) onMove.accept(p);
    }

    public void handleInteract(Player p) {
        if (onInteract != null) onInteract.accept(p);
    }

    public void handleDamage(Player p) {
        if (onDamage != null) onDamage.accept(p);
    }

    public void handleCommand(Player p) {
        if (onCommand != null) onCommand.accept(p);
    }

    public void handleQuit(OfflinePlayer p) {
        if (onQuit != null) onQuit.accept(p);
    }

    public static class Builder {
        private final AliasContext context;

        private final UUID targetUuid;
        private final Location backLocation;

        private boolean allowMovement = false;
        private boolean allowDamage = false;
        private final Set<String> allowedCommands = new HashSet<>();
        private boolean allowInteract = false;

        private Consumer<Player> onMove = null;
        private Consumer<Player> onInteract = null;
        private Consumer<Player> onDamage = null;
        private Consumer<Player> onCommand = null;
        private Consumer<OfflinePlayer> onQuit = null;
        private Runnable onStart = null;
        private Runnable onThaw = null;

        private Duration duration = Duration.ZERO;

        public Builder(AliasContext context, UUID target, Location backLocation) {
            this.context = context;
            this.targetUuid = target;
            this.backLocation = backLocation;
        }

        public Builder allowMovement() { this.allowMovement = true;  return this; }
        public Builder allowDamage() { this.allowDamage = true;  return this; }
        public Builder allowCommands(Set<String> allowedCommands) { this.allowedCommands.addAll(allowedCommands); return this; }
        public Builder allowInteract() { this.allowInteract = true;  return this; }

        public Builder onMove(Consumer<Player> c) { this.onMove = c; return this; }
        public Builder onInteract(Consumer<Player> c) { this.onInteract = c; return this; }
        public Builder onDamage(Consumer<Player> c) { this.onDamage = c; return this; }
        public Builder onCommand(Consumer<Player> c) { this.onCommand = c; return this; }
        public Builder onQuit(Consumer<OfflinePlayer> c){ this.onQuit = c; return this; }
        public Builder onStart(Runnable r) { this.onStart = r; return this; }
        public Builder onThaw(Runnable r) { this.onThaw = r; return this; }

        public Builder duration(Duration d) {
            this.duration = d != null ? d : Duration.ZERO;
            return this;
        }

        public FreezeSession build() {
            return new FreezeSession(context, this);
        }
    }
}
