package me.trouper.alias.utils;

import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public final class InventoryUtils {

    public static Inventory getInventory(Entity entity) {
        if (entity instanceof org.bukkit.inventory.InventoryHolder inventoryHolder) {
            return inventoryHolder.getInventory();
        }
        return null;
    }

    public static Inventory getInventory(ItemStack containerItem) {
        if (containerItem.getItemMeta() instanceof BlockStateMeta blockStateMeta) {
            BlockState blockState = blockStateMeta.getBlockState();
            if (blockState instanceof Container container) {
                return container.getInventory();
            }
        }
        return null;
    }
}

