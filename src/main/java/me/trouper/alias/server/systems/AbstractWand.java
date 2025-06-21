package me.trouper.alias.server.systems;

import me.trouper.alias.server.Main;
import me.trouper.alias.server.events.QuickListener;
import me.trouper.alias.utils.misc.Cooldown;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public abstract class AbstractWand implements QuickListener, Main {

    private final String usePermission;
    private final ItemStack wandItem;
    private final Cooldown<UUID> debounce = new Cooldown<>();

    public AbstractWand(String usePermission, ItemStack wandItem) {
        this.wandItem = wandItem.clone();
        this.usePermission = usePermission;
    }

    public String getUsePermission() {
        return usePermission;
    }
    public ItemStack getWandItem() {
        return wandItem.clone();
    }

    private boolean isWand(ItemStack item) {
        return item != null && item.isSimilar(wandItem);
    }

    private boolean isHoldingWand(Player p) {
        ItemStack inMain = p.getInventory().getItemInMainHand();
        ItemStack inOff  = p.getInventory().getItemInOffHand();
        return isWand(inMain) || isWand(inOff);
    }

    @EventHandler
    public final void onSwapHands(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        if (!isHoldingWand(p) || !p.hasPermission(getUsePermission())) return;
        e.setCancelled(true);

        swapHand(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!isHoldingWand(p) || !p.hasPermission(getUsePermission())) return;

        Action action = e.getAction();

        switch (action) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> {
                e.setCancelled(true);

                if (debounce.isOnCooldown(p.getUniqueId())) return;
                debounce.setCooldown(p.getUniqueId(),100);

                rightClick(p);
            }
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                e.setCancelled(true);
                leftClick(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onEntityDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        if (!isHoldingWand(p) || !p.hasPermission(getUsePermission())) return;

        e.setCancelled(true);
        leftClick(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onEntityInteract(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (!isHoldingWand(p) || !p.hasPermission(getUsePermission())) return;
        e.setCancelled(true);

        if (debounce.isOnCooldown(p.getUniqueId())) return;
        debounce.setCooldown(p.getUniqueId(),100);

        rightClick(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onEntityInteractAt(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();
        if (!isHoldingWand(p) || !p.hasPermission(getUsePermission())) return;

        if (debounce.isOnCooldown(p.getUniqueId())) return;
        debounce.setCooldown(p.getUniqueId(),100);

        e.setCancelled(true);
        rightClick(p);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!isHoldingWand(p) || !p.hasPermission(getUsePermission())) return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!isWand(e.getItemInHand()) || !p.hasPermission(getUsePermission())) return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onItemDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (!isWand(e.getItemDrop().getItemStack()) || !p.hasPermission(getUsePermission())) return;

        e.setCancelled(true);
    }

    @EventHandler
    public final void onScroll(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        if (!isHoldingWand(p) || !p.hasPermission(getUsePermission())) return;

        int prev = e.getPreviousSlot();
        int curr = e.getNewSlot();

        if (!p.isSneaking() || !isWand(p.getInventory().getItem(prev))) return;
        e.setCancelled(true);

        if (curr == 8 && prev == 0) onScrollUp(p);
        else if (curr == 0 && prev == 8) onScrollDown(p);
        else if (curr < prev) onScrollUp(p);
        else if (curr > prev) onScrollDown(p);
    }

    public final void swapHand(Player p) {
        if (p.isSneaking()) onSwapHandSneak(p);
        else onSwapHand(p);
    }

    public final void leftClick(Player p) {
        if (p.isSneaking()) onLeftClickSneak(p);
        else onLeftClick(p);
    }

    public final void rightClick(Player p) {
        if (p.isSneaking()) onRightClickSneak(p);
        else onRightClick(p);
    }

    protected void onSwapHand(Player player) {}
    protected void onSwapHandSneak(Player player) {}
    protected void onRightClick(Player player) {}
    protected void onRightClickSneak(Player player) {}
    protected void onLeftClick(Player player) {}
    protected void onLeftClickSneak(Player player) {}

    /**
     * The player must be sneaking to scroll the wand.
     */
    protected void onScrollUp(Player player) {}

    /**
     * The player must be sneaking to scroll the wand.
     */
    protected void onScrollDown(Player player) {}
}