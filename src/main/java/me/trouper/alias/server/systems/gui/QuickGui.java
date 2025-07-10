package me.trouper.alias.server.systems.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class QuickGui implements InventoryHolder {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final Map<Integer, GuiAction> slotActions;
    private final Map<Integer, ItemStack> slotItems;
    private final GuiAction globalAction;
    private final GuiCreateAction createAction;
    private final GuiCloseAction closeAction;
    private final GuiDragAction dragAction;
    private final Component title;
    private final int size;
    private final boolean preventDrag;
    private final Sound clickSound;
    private final float soundVolume;
    private final float soundPitch;

    private Inventory inventory;
    private final Set<Player> viewers;

    private final Map<String, GuiCallback> callbacks;
    private final Map<Player, String> waitingForInput;
    private final Map<Player, Long> inputTimeouts;
    private final long defaultTimeout;

    private QuickGui(Component title, int size, GuiAction globalAction,
                     Map<Integer, GuiAction> slotActions, Map<Integer, ItemStack> slotItems,
                     GuiCreateAction createAction, GuiCloseAction closeAction, GuiDragAction dragAction,
                     boolean preventDrag, Sound clickSound, float soundVolume, float soundPitch,
                     Map<String, GuiCallback> callbacks, long defaultTimeout) {
        this.title = title;
        this.size = size;
        this.globalAction = globalAction;
        this.slotActions = new HashMap<>(slotActions);
        this.slotItems = new HashMap<>(slotItems);
        this.createAction = createAction;
        this.closeAction = closeAction;
        this.dragAction = dragAction;
        this.preventDrag = preventDrag;
        this.clickSound = clickSound;
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
        this.viewers = ConcurrentHashMap.newKeySet();
        this.callbacks = new HashMap<>(callbacks);
        this.waitingForInput = new ConcurrentHashMap<>();
        this.inputTimeouts = new ConcurrentHashMap<>();
        this.defaultTimeout = defaultTimeout;
    }

    public static void handleClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof QuickGui gui) {
            gui.onInventoryClick(event);
        }
    }

    public static void handleClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof QuickGui gui) {
            gui.onInventoryClose(event);
        }
    }

    public static void handleDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof QuickGui gui) {
            gui.onInventoryDrag(event);
        }
    }

    @Override
    public Inventory getInventory() {
        if (inventory == null) {
            int actualSize = calculateSize();
            inventory = Bukkit.createInventory(this, actualSize, title);
            createAction.onCreate(this, inventory);
            populateInventory();
        }
        return inventory;
    }

    public void open(Player player) {
        if (player != null && player.isOnline()) {
            player.openInventory(getInventory());
            viewers.add(player);
        }
    }

    public void closeAll() {
        new ArrayList<>(viewers).forEach(Player::closeInventory);
    }

    public void updateItem(int slot, ItemStack item) {
        if (slot >= 0 && slot < getInventory().getSize()) {
            slotItems.put(slot, item);
            getInventory().setItem(slot, item);
        }
    }

    public void updateItem(int slot, ItemStack item, GuiAction action) {
        updateItem(slot, item);
        if (action != null) {
            slotActions.put(slot, action);
        }
    }

    public void removeItem(int slot) {
        if (slot >= 0 && slot < getInventory().getSize()) {
            slotItems.remove(slot);
            slotActions.remove(slot);
            getInventory().setItem(slot, null);
        }
    }

    public void requestInput(Player player, String callbackId) {
        requestInput(player, callbackId, defaultTimeout);
    }

    public void requestInput(Player player, String callbackId, long timeoutMs) {
        if (!callbacks.containsKey(callbackId)) {
            throw new IllegalArgumentException("Callback with ID '" + callbackId + "' not found");
        }

        waitingForInput.put(player, callbackId);
        inputTimeouts.put(player, System.currentTimeMillis() + timeoutMs);

        player.closeInventory();
    }

    public boolean handleInput(Player player, String input, InputSource source) {
        String callbackId = waitingForInput.get(player);
        if (callbackId == null) {
            return false;
        }

        Long timeout = inputTimeouts.get(player);
        if (timeout != null && System.currentTimeMillis() > timeout) {
            waitingForInput.remove(player);
            inputTimeouts.remove(player);
            callbacks.get(callbackId).onTimeout(this, player);
            return false;
        }

        waitingForInput.remove(player);
        inputTimeouts.remove(player);

        GuiCallback callback = callbacks.get(callbackId);
        if (callback != null) {
            callback.onInput(this, player, input, source);
            return true;
        }

        return false;
    }

    public void cancelInput(Player player) {
        String callbackId = waitingForInput.remove(player);
        inputTimeouts.remove(player);

        if (callbackId != null) {
            GuiCallback callback = callbacks.get(callbackId);
            if (callback != null) {
                callback.onCancel(this, player);
            }
        }
    }

    public boolean isWaitingForInput(Player player) {
        return waitingForInput.containsKey(player);
    }

    public String getWaitingCallbackId(Player player) {
        return waitingForInput.get(player);
    }

    public Set<Player> getPlayersWaitingForInput() {
        return new HashSet<>(waitingForInput.keySet());
    }

    public void cleanupExpiredTimeouts() {
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<Player, Long>> iterator = inputTimeouts.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Player, Long> entry = iterator.next();
            if (currentTime > entry.getValue()) {
                Player player = entry.getKey();
                String callbackId = waitingForInput.remove(player);
                iterator.remove();

                if (callbackId != null) {
                    GuiCallback callback = callbacks.get(callbackId);
                    if (callback != null) {
                        callback.onTimeout(this, player);
                    }
                }
            }
        }
    }

    private int calculateSize() {
        if (size > 0 && size % 9 == 0) {
            return Math.min(size, 54);
        }

        int maxSlot = slotItems.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(8);

        int rows = (maxSlot / 9) + 1;
        return Math.min(rows * 9, 54);
    }

    private void populateInventory() {
        slotItems.forEach((slot, item) -> {
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, item);
            }
        });
    }

    private void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null ||
                event.getClickedInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (clickSound != null) {
            player.playSound(player.getLocation(), clickSound, soundVolume, soundPitch);
        }

        globalAction.onClick(this, event);

        int slot = event.getSlot();
        GuiAction action = slotActions.get(slot);
        if (action != null) {
            action.onClick(this, event);
        }
    }

    private void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            viewers.remove(player);
        }

        closeAction.onClose(this, event);
    }

    private void onInventoryDrag(InventoryDragEvent event) {
        if (preventDrag) {
            event.setCancelled(true);
        }
        dragAction.onDrag(this, event);
    }

    public Component getTitle() { return title; }
    public int getSize() { return size; }
    public Set<Player> getViewers() { return new HashSet<>(viewers); }
    public Map<Integer, ItemStack> getSlotItems() { return new HashMap<>(slotItems); }

    public static GuiBuilder create() {
        return new GuiBuilder();
    }

    public static class GuiBuilder {
        private Component title;
        private int size = -1;
        private GuiAction globalAction = (gui, event) -> {};
        private GuiCreateAction createAction = (gui, inv) -> {};
        private GuiCloseAction closeAction = (gui, event) -> {};
        private GuiDragAction dragAction = (gui, event) -> {};
        private final Map<Integer, GuiAction> slotActions = new HashMap<>();
        private final Map<Integer, ItemStack> slotItems = new HashMap<>();
        private boolean preventDrag = true;
        private Sound clickSound = Sound.UI_BUTTON_CLICK;
        private float soundVolume = 0.5f;
        private float soundPitch = 1.0f;
        private final Map<String, GuiCallback> callbacks = new HashMap<>();
        private long defaultTimeout = 30000; // 30 seconds default

        public GuiBuilder title(String title) {
            this.title = Component.text(title);
            return this;
        }

        public GuiBuilder title(Component title) {
            this.title = title;
            return this;
        }

        public GuiBuilder titleMini(String miniMessageTitle) {
            this.title = miniMessage.deserialize(miniMessageTitle);
            return this;
        }

        public GuiBuilder size(int size) {
            this.size = size;
            return this;
        }

        public GuiBuilder rows(int rows) {
            this.size = Math.max(1, Math.min(6, rows)) * 9;
            return this;
        }

        public GuiBuilder onGlobalClick(GuiAction action) {
            this.globalAction = action != null ? action : (gui, event) -> {};
            return this;
        }

        public GuiBuilder onCreate(GuiCreateAction action) {
            this.createAction = action != null ? action : (gui, inv) -> {};
            return this;
        }

        public GuiBuilder onClose(GuiCloseAction action) {
            this.closeAction = action != null ? action : (gui, event) -> {};
            return this;
        }

        public GuiBuilder onDrag(GuiDragAction action) {
            this.dragAction = action != null ? action : (gui, event) -> {};
            return this;
        }

        public GuiBuilder allowDrag() {
            this.preventDrag = false;
            return this;
        }

        public GuiBuilder preventDrag() {
            this.preventDrag = true;
            return this;
        }

        public GuiBuilder clickSound(Sound sound, float volume, float pitch) {
            this.clickSound = sound;
            this.soundVolume = volume;
            this.soundPitch = pitch;
            return this;
        }

        public GuiBuilder noClickSound() {
            this.clickSound = null;
            return this;
        }

        public GuiBuilder defaultTimeout(long timeoutMs) {
            this.defaultTimeout = timeoutMs;
            return this;
        }

        public GuiBuilder callback(String id, GuiCallback callback) {
            if (id != null && callback != null) {
                this.callbacks.put(id, callback);
            }
            return this;
        }

        public GuiBuilder item(int slot, ItemStack item) {
            return item(slot, item, null);
        }

        public GuiBuilder item(int slot, ItemStack item, GuiAction action) {
            if (slot >= 0 && slot < 54 && item != null) {
                slotItems.put(slot, item);
                if (action != null) {
                    slotActions.put(slot, action);
                }
            }
            return this;
        }

        public GuiBuilder item(int slot, Material material, String name) {
            return item(slot, material, name, null);
        }

        public GuiBuilder item(int slot, Material material, String name, GuiAction action) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(meta);
            }
            return item(slot, item, action);
        }

        public GuiBuilder itemMini(int slot, Material material, String miniMessageName) {
            return itemMini(slot, material, miniMessageName, null);
        }

        public GuiBuilder itemMini(int slot, Material material, String miniMessageName, GuiAction action) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(miniMessage.deserialize(miniMessageName).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(meta);
            }
            return item(slot, item, action);
        }

        public GuiBuilder fillSlots(ItemStack item, GuiAction action, int... slots) {
            for (int slot : slots) {
                if (slot >= 0 && slot < 54 && item != null) {
                    slotItems.put(slot, item);
                    slotActions.put(slot, action);
                }
            }
            return this;
        }

        public GuiBuilder fillBorder(Material material) {
            return fillBorder(new ItemStack(material));
        }

        public GuiBuilder fillBorder(ItemStack item) {
            int actualSize = size > 0 ? size : 54;
            int rows = actualSize / 9;

            for (int i = 0; i < 9; i++) {
                slotItems.put(i, item);
                if (rows > 1) {
                    slotItems.put((rows - 1) * 9 + i, item);
                }
            }

            for (int row = 1; row < rows - 1; row++) {
                slotItems.put(row * 9, item);
                slotItems.put(row * 9 + 8, item);
            }

            return this;
        }

        public GuiBuilder fillEmpty(Material material) {
            return fillEmpty(new ItemStack(material));
        }

        public GuiBuilder fillEmpty(ItemStack item) {
            int actualSize = size > 0 ? size : 54;
            for (int i = 0; i < actualSize; i++) {
                if (!slotItems.containsKey(i)) {
                    slotItems.put(i, item);
                }
            }
            return this;
        }

        public QuickGui build() {
            Component finalTitle = title != null ? title : Component.text("Untitled GUI");
            return new QuickGui(finalTitle, size, globalAction, slotActions, slotItems,
                    createAction, closeAction, dragAction, preventDrag,
                    clickSound, soundVolume, soundPitch, callbacks, defaultTimeout);
        }
    }

    @FunctionalInterface
    public interface GuiAction {
        void onClick(QuickGui gui, InventoryClickEvent event);
    }

    @FunctionalInterface
    public interface GuiCreateAction {
        void onCreate(QuickGui gui, Inventory inventory);
    }

    @FunctionalInterface
    public interface GuiCloseAction {
        void onClose(QuickGui gui, InventoryCloseEvent event);
    }

    @FunctionalInterface
    public interface GuiDragAction {
        void onDrag(QuickGui gui, InventoryDragEvent event);
    }

    public interface GuiCallback {
        void onInput(QuickGui gui, Player player, String input, InputSource source);

        default void onTimeout(QuickGui gui, Player player) {
            player.sendMessage(Component.text("Input timed out.", NamedTextColor.RED));
        }

        default void onCancel(QuickGui gui, Player player) {
            player.sendMessage(Component.text("Input cancelled.", NamedTextColor.YELLOW));
        }
    }

    public enum InputSource {
        CHAT,
        COMMAND,
        SIGN,
        BOOK,
        ANVIL,
        CUSTOM
    }
}