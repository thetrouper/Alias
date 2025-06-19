package me.trouper.alias.server.systems.gui;

import me.trouper.alias.server.Main;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class QuickGui implements InventoryHolder, Main {

    private static final Map<String, QuickGui> registry = new ConcurrentHashMap<>();
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final Map<Integer, GuiAction> slotActions;
    private final Map<Integer, ItemStack> slotItems;
    private final Map<Integer, BukkitTask> animations;
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

    private QuickGui(Component title, int size, GuiAction globalAction,
                     Map<Integer, GuiAction> slotActions, Map<Integer, ItemStack> slotItems,
                     GuiCreateAction createAction, GuiCloseAction closeAction, GuiDragAction dragAction,
                     boolean preventDrag, Sound clickSound, float soundVolume, float soundPitch) {
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
        this.animations = new HashMap<>();
        this.viewers = ConcurrentHashMap.newKeySet();
    }

    public static QuickGui register(String id, QuickGui gui) {
        if (gui != null && id != null && !id.isEmpty()) {
            registry.put(id, gui);
        }
        return gui;
    }

    public static Optional<QuickGui> getRegistered(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    public static Map<String, QuickGui> getRegistries() {
        return new HashMap<>(registry);
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

    public void startAnimation(int slot, List<ItemStack> frames, long interval) {
        stopAnimation(slot);

        if (frames.isEmpty()) return;

        BukkitTask task = new BukkitRunnable() {
            private int frameIndex = 0;

            @Override
            public void run() {
                if (getInventory().getViewers().isEmpty()) {
                    cancel();
                    return;
                }

                ItemStack frame = frames.get(frameIndex);
                getInventory().setItem(slot, frame);
                frameIndex = (frameIndex + 1) % frames.size();
            }
        }.runTaskTimer(main.getPlugin(), 0L, interval);

        animations.put(slot, task);
    }

    public void stopAnimation(int slot) {
        BukkitTask task = animations.remove(slot);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public void stopAllAnimations() {
        animations.values().forEach(task -> {
            if (!task.isCancelled()) {
                task.cancel();
            }
        });
        animations.clear();
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

        if (viewers.isEmpty()) {
            stopAllAnimations();
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
                    clickSound, soundVolume, soundPitch);
        }

        public QuickGui buildAndRegister(String id) {
            QuickGui gui = build();
            return register(id, gui);
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

    public static class GuiUtils {

        public static QuickGui createConfirmDialog(String title, Consumer<Boolean> callback) {
            return create()
                    .titleMini("<green>" + title)
                    .rows(3)
                    .fillBorder(Material.GRAY_STAINED_GLASS_PANE)
                    .itemMini(11, Material.GREEN_CONCRETE, "<green><bold>CONFIRM",
                            (gui, event) -> {
                                callback.accept(true);
                                event.getWhoClicked().closeInventory();
                            })
                    .itemMini(15, Material.RED_CONCRETE, "<red><bold>CANCEL",
                            (gui, event) -> {
                                callback.accept(false);
                                event.getWhoClicked().closeInventory();
                            })
                    .build();
        }

        public static GuiBuilder createPaginated(String title, List<ItemStack> items, int itemsPerPage) {
            GuiBuilder builder = create()
                    .titleMini(title)
                    .rows(6)
                    .fillBorder(Material.GRAY_STAINED_GLASS_PANE);

            int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);

            if (totalPages > 1) {
                builder.itemMini(45, Material.ARROW, "<yellow>Previous Page")
                        .itemMini(53, Material.ARROW, "<yellow>Next Page");
            }

            return builder;
        }

        public static ItemStack createSeparator(NamedTextColor color) {
            ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = pane.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text(" ").color(color));
                pane.setItemMeta(meta);
            }
            return pane;
        }
    }
}