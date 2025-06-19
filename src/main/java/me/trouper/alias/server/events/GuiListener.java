package me.trouper.alias.server.events;

import me.trouper.alias.server.systems.gui.QuickGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GuiListener implements QuickListener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        QuickGui.handleClick(e);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        QuickGui.handleClose(e);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        QuickGui.handleDrag(e);
    }
}
