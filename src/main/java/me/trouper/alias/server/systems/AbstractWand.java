package me.trouper.alias.server.systems;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractWand {

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

    public void onSwapHand(Player player) {}
    public void onSwapHandSneak(Player player) {}
    public void onRightClick(Player player) {}
    public void onRightClickSneak(Player player) {}
    public void onLeftClick(Player player) {}
    public void onLeftClickSneak(Player player) {}

    /**
     * The player must be sneaking to scroll the wand.
     */
    public void onScrollUp(Player player) {}

    /**
     * The player must be sneaking to scroll the wand.
     */
    public void onScrollDown(Player player) {}
}