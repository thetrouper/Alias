package me.trouper.alias.server.systems.world;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Snapshot {
    private final BlockState state;
    private final Map<Integer, ItemStack> inventory;

    public Snapshot(Block block) {
        this.state = block.getState();

        if (state instanceof InventoryHolder) {
            Inventory inv = ((InventoryHolder) state).getInventory();
            int size = inv.getSize();

            int nonEmptySlots = 0;
            for (int i = 0; i < size; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && !item.isEmpty()) {
                    nonEmptySlots++;
                }
            }

            if (nonEmptySlots > 0) {
                this.inventory = new HashMap<>(nonEmptySlots * 4 / 3 + 1);

                for (int i = 0; i < size; i++) {
                    ItemStack item = inv.getItem(i);
                    if (item != null && !item.isEmpty()) {
                        inventory.put(i, item.clone());
                    }
                }
            } else {
                this.inventory = null;
            }
        } else {
            this.inventory = null;
        }
    }

    public void restore(Block block) {
        if (block == null || state == null) return;

        try {
            block.setBlockData(state.getBlockData());

            if (inventory != null && block.getState() instanceof InventoryHolder) {
                InventoryHolder holder = (InventoryHolder) block.getState();
                Inventory inv = holder.getInventory();

                inv.clear();

                for (Map.Entry<Integer, ItemStack> entry : inventory.entrySet()) {
                    int slot = entry.getKey();
                    ItemStack item = entry.getValue();

                    if (slot >= 0 && slot < inv.getSize() && item != null) {
                        inv.setItem(slot, item.clone());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to restore block at " + block.getLocation() + ": " + e.getMessage());
        }
    }

    public BlockState getState() {
        return state;
    }

    public Map<Integer, ItemStack> getInventory() {
        return inventory;
    }
}