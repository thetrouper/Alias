package me.trouper.alias.server.systems.gui;

import me.trouper.alias.server.events.QuickListener;
import me.trouper.alias.server.systems.items.CustomItem;
import me.trouper.alias.server.systems.items.ItemManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class RegistryListeners implements QuickListener {

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        try {
            CustomGui.handleRegistriesClick(e);
        }
        catch (Exception ignore) {}
    }

    @EventHandler
    private void onClose(InventoryCloseEvent e) {
        try {
            CustomGui.handleRegistriesClose(e);
        }
        catch (Exception ignore) {}
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent e) {
        try {
            CustomItem context = ItemManager.getItemContext(e.getItem(), CustomItem.class);
            if (context != null)
                context.onInteract(e);
        }
        catch (Exception ignore) {}
    }
}
