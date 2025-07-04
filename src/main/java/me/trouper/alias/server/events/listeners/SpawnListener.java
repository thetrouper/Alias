package me.trouper.alias.server.events.listeners;

import me.trouper.alias.AliasContext;
import me.trouper.alias.server.events.custom.PlayerSpawnEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SpawnListener implements Listener {

    private final AliasContext context;
    private final ConcurrentLinkedQueue<Placed> recentBlocks = new ConcurrentLinkedQueue<>();
    private final Map<UUID, UUID> pearlOwners = new ConcurrentHashMap<>();

    public SpawnListener(AliasContext context) {
        this.context = context;
        Bukkit.getScheduler().runTaskTimer(context.getPlugin(), this::cleanup, 20, 20);
    }

    private void cleanup() {
        long cutoff = System.currentTimeMillis() - 2000;
        Iterator<Placed> it = recentBlocks.iterator();
        while (it.hasNext() && it.next().time < cutoff) {
            it.remove();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        recentBlocks.add(new Placed(e.getPlayer().getUniqueId(), e.getBlockPlaced().getLocation(), System.currentTimeMillis()));
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof EnderPearl)) return;
        if (!(e.getEntity().getShooter() instanceof Player p)) return;
        pearlOwners.put(e.getEntity().getUniqueId(), p.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(
                context.getPlugin(),
                () -> pearlOwners.remove(e.getEntity().getUniqueId()),
                100L
        );
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        SpawnReason reason = e.getSpawnReason();
        Player spawner = null;

        if (reason == SpawnReason.BUILD_IRONGOLEM
                || reason == SpawnReason.BUILD_SNOWMAN
                || reason == SpawnReason.BUILD_WITHER) {

            Location spawnLoc = e.getEntity().getLocation();
            long now = System.currentTimeMillis();
            for (Placed p : recentBlocks) {
                if (p.time > now - 2000 && p.loc.getWorld().equals(spawnLoc.getWorld())
                        && p.loc.distanceSquared(spawnLoc) < 4) {
                    spawner = Bukkit.getPlayer(p.playerId);
                    break;
                }
            }
        } else if (reason == SpawnReason.ENDER_PEARL) {
            for (Map.Entry<UUID, UUID> en : pearlOwners.entrySet()) {
                Entity pearl = Bukkit.getEntity(en.getKey());
                if (pearl != null && pearl.getLocation().distanceSquared(e.getEntity().getLocation()) < 4) {
                    spawner = Bukkit.getPlayer(en.getValue());
                    break;
                }
            }
        }

        if (spawner == null) return;

        PlayerSpawnEntityEvent pse = new PlayerSpawnEntityEvent(spawner, e.getEntity());
        Bukkit.getPluginManager().callEvent(pse);
        if (pse.isCancelled()) {
            e.setCancelled(true);
        }
    }

    private static class Placed {
        final UUID playerId;
        final Location loc;
        final long time;
        Placed(UUID p, Location l, long t) { playerId = p; loc = l; time = t; }
    }
}
