package me.trouper.alias.server.events.listeners;

import me.trouper.alias.AliasContext;
import me.trouper.alias.server.systems.AbstractWand;
import me.trouper.alias.utils.misc.Cooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class WandListener implements Listener {

    private final AliasContext context;
    private final Cooldown<UUID> debounce = new Cooldown<>();

    public WandListener(AliasContext context) {
        this.context = context;
    }

    private List<AbstractWand> getWands() {
        return context.getAutoRegistrar().getWands();
    }

    private AbstractWand getWandForItem(ItemStack item) {
        if (item == null) return null;

        for (AbstractWand wand : getWands()) {
            if (item.isSimilar(wand.getWandItem())) {
                return wand;
            }
        }
        return null;
    }

    private AbstractWand getWandForPlayer(Player p) {
        ItemStack inMain = p.getInventory().getItemInMainHand();
        ItemStack inOff = p.getInventory().getItemInOffHand();

        AbstractWand wand = getWandForItem(inMain);
        if (wand != null) return wand;

        return getWandForItem(inOff);
    }

    @EventHandler
    public final void onSwapHands(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        AbstractWand wand = getWandForPlayer(p);
        if (wand == null || !p.hasPermission(wand.getUsePermission())) return;
        e.setCancelled(true);

        wand.swapHand(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        AbstractWand wand = getWandForPlayer(p);
        if (wand == null || !p.hasPermission(wand.getUsePermission())) return;

        Action action = e.getAction();

        switch (action) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                e.setCancelled(true);

                if (debounce.isOnCooldown(p.getUniqueId())) return;
                debounce.setCooldown(p.getUniqueId(), 100);

                wand.rightClick(p);
            }
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                e.setCancelled(true);
                wand.leftClick(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onEntityDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        AbstractWand wand = getWandForPlayer(p);
        if (wand == null || !p.hasPermission(wand.getUsePermission())) return;

        e.setCancelled(true);
        wand.leftClick(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onEntityInteract(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        AbstractWand wand = getWandForPlayer(p);
        if (wand == null || !p.hasPermission(wand.getUsePermission())) return;
        e.setCancelled(true);

        if (debounce.isOnCooldown(p.getUniqueId())) return;
        debounce.setCooldown(p.getUniqueId(), 100);

        wand.rightClick(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onEntityInteractAt(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();
        AbstractWand wand = getWandForPlayer(p);
        if (wand == null || !p.hasPermission(wand.getUsePermission())) return;

        if (debounce.isOnCooldown(p.getUniqueId())) return;
        debounce.setCooldown(p.getUniqueId(), 100);

        e.setCancelled(true);
        wand.rightClick(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        AbstractWand wand = getWandForPlayer(p);
        if (wand == null || !p.hasPermission(wand.getUsePermission())) return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        AbstractWand wand = getWandForItem(e.getItemInHand());
        if (wand == null || !p.hasPermission(wand.getUsePermission())) return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onItemDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        AbstractWand wand = getWandForItem(e.getItemDrop().getItemStack());
        if (wand == null || !p.hasPermission(wand.getUsePermission())) return;

        e.setCancelled(true);
    }

    @EventHandler
    public final void onScroll(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        AbstractWand wand = getWandForPlayer(p);
        if (wand == null || !p.hasPermission(wand.getUsePermission())) return;

        int prev = e.getPreviousSlot();
        int curr = e.getNewSlot();

        if (!p.isSneaking() || getWandForItem(p.getInventory().getItem(prev)) == null) return;
        e.setCancelled(true);

        if (curr == 8 && prev == 0) wand.onScrollUp(p);
        else if (curr == 0 && prev == 8) wand.onScrollDown(p);
        else if (curr < prev) wand.onScrollUp(p);
        else if (curr > prev) wand.onScrollDown(p);
    }
}