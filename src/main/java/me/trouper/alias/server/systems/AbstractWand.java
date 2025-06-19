package me.trouper.alias.server.systems;

import me.trouper.alias.server.Main;
import me.trouper.alias.server.events.QuickListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractWand implements QuickListener, Main {

    private final String usePermission;
    private final ItemStack wandItem;

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
    public void onSwapHands(PlayerSwapHandItemsEvent e) {
        Player p = e.getPlayer();
        if (!isHoldingWand(p) || !p.hasPermission(getUsePermission())) return;
        e.setCancelled(true);

        if (p.isSneaking()) onSwapHandSneak(p);
        else onSwapHand(p);
    }

    @EventHandler
    public void onAnimate(PlayerAnimationEvent e) {
        Player p = e.getPlayer();
        if (!isHoldingWand(p) || !p.hasPermission(getUsePermission())) return;
        if (!e.getAnimationType().equals(PlayerAnimationType.ARM_SWING)) return;
        if (e.getPlayer().getTargetEntity(5) == null) return;
        e.setCancelled(true);

        if (p.isSneaking()) onLeftClickSneak(p);
        else onLeftClick(p);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!isHoldingWand(p) || !p.hasPermission(getUsePermission())) return;

        Action action = e.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        e.setCancelled(true);

        if (p.isSneaking()) onRightClickSneak(p);
        else onRightClick(p);
    }

    @EventHandler
    public void onScroll(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        if (!isHoldingWand(p) || !p.hasPermission(getUsePermission())) return;

        int prev = e.getPreviousSlot();
        int curr = e.getNewSlot();

        if (!p.isSneaking() || !isWand(p.getInventory().getItem(prev))) return;

        if (curr < prev) onScrollUp(e.getPlayer());
        else if (curr > prev) onScrollDown(e.getPlayer());
    }

    protected void onSwapHand(Player player) {}
    protected void onSwapHandSneak(Player player) {}
    protected void onRightClick(Player player) {}
    protected void onRightClickSneak(Player player) {}
    protected void onLeftClick(Player player) {}
    protected void onLeftClickSneak(Player player) {}
    protected void onScrollUp(Player player) {}
    protected void onScrollDown(Player player) {}
}
