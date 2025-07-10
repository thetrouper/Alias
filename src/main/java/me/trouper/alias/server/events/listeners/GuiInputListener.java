package me.trouper.alias.server.events.listeners;

import me.trouper.alias.AliasContext;
import me.trouper.alias.server.systems.gui.QuickGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GuiInputListener implements Listener {

    private final Map<Player, QuickGui> waitingPlayers = new ConcurrentHashMap<>();
    private BukkitRunnable timeoutTask;
    private final AliasContext context;

    public GuiInputListener(AliasContext context) {
        this.context = context;
    }

    public void registerWaitingPlayer(Player player, QuickGui gui) {
        waitingPlayers.put(player, gui);
    }


    public void unregisterWaitingPlayer(Player player) {
        waitingPlayers.remove(player);
    }

    public boolean isWaitingForInput(Player player) {
        return waitingPlayers.containsKey(player);
    }

    public QuickGui getWaitingGui(Player player) {
        return waitingPlayers.get(player);
    }

    public boolean handleInput(Player player, String input, QuickGui.InputSource source) {
        QuickGui gui = waitingPlayers.get(player);
        if (gui != null) {
            boolean handled = gui.handleInput(player, input, source);
            if (handled) {
                waitingPlayers.remove(player);
            }
            return handled;
        }
        return false;
    }

    public void cancelInput(Player player) {
        QuickGui gui = waitingPlayers.get(player);
        if (gui != null) {
            gui.cancelInput(player);
            waitingPlayers.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        QuickGui gui = waitingPlayers.get(player);

        if (gui != null) {
            event.setCancelled(true);

            context.getPlugin().getServer().getScheduler().runTask(context.getPlugin(), () -> {
                boolean handled = gui.handleInput(player, event.getMessage(), QuickGui.InputSource.CHAT);
                if (handled) {
                    waitingPlayers.remove(player);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        QuickGui gui = waitingPlayers.get(player);

        if (gui != null) {
            String command = event.getMessage();

            if (command.equalsIgnoreCase("/cancel") || command.equalsIgnoreCase("/c")) {
                event.setCancelled(true);
                gui.cancelInput(player);
                waitingPlayers.remove(player);
                return;
            }

            event.setCancelled(true);
            boolean handled = gui.handleInput(player, command, QuickGui.InputSource.COMMAND);
            if (handled) {
                waitingPlayers.remove(player);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        QuickGui gui = waitingPlayers.remove(player);
        if (gui != null) {
            gui.cancelInput(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        QuickGui.handleClick(event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        QuickGui.handleClose(event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryDrag(InventoryDragEvent event) {
        QuickGui.handleDrag(event);
    }

    public void startTimeoutTask() {
        timeoutTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Player, QuickGui> entry : waitingPlayers.entrySet()) {
                    entry.getValue().cleanupExpiredTimeouts();
                }
            }
        };
        timeoutTask.runTaskTimer(context.getPlugin(), 20L, 20L);
    }

    public void shutdown() {
        if (timeoutTask != null) {
            timeoutTask.cancel();
        }
        waitingPlayers.clear();
    }

    public static void sendInputInstructions(Player player, String prompt) {
        sendInputInstructions(player, prompt, true);
    }

    public static void sendInputInstructions(Player player, String prompt, boolean showCancelOption) {
        player.sendMessage(Component.text("").append(Component.text("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.GRAY)));
        player.sendMessage(MiniMessage.miniMessage().deserialize(prompt));
        if (showCancelOption) {
            player.sendMessage(Component.text("Type '/cancel' to cancel input.", NamedTextColor.GRAY));
        }
        player.sendMessage(Component.text("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", NamedTextColor.GRAY));
    }
}