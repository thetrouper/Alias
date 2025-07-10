package me.trouper.alias.server.systems.gui;

import me.trouper.alias.utils.SoundPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public abstract class QuickPaginatedGUI<T> {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    protected static final int DEFAULT_ITEMS_PER_PAGE = 45;
    protected static final int[] DEFAULT_PAGE_SLOTS = {
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    };
    protected static final int DEFAULT_PREV_SLOT = 45;
    protected static final int DEFAULT_NEXT_SLOT = 53;
    protected static final int DEFAULT_FILTER_SLOT = 49;

    protected static final Map<UUID, Integer> currentPages = new HashMap<>();
    protected static final Map<UUID, Set<String>> activeFilters = new HashMap<>();
    protected static final Map<UUID, FilterOperator> chosenOperator = new HashMap<>();

    protected abstract String getTitle(Player player);

    protected abstract List<T> getAllItems(Player player);

    protected abstract ItemStack createDisplayItem(T item);

    protected abstract void handleItemClick(Player player, T item, InventoryClickEvent event);

    protected abstract void addFilterItems(QuickGui.GuiBuilder filterGui, Player player, Set<String> filters);

    protected abstract void openBackGUI(Player player);

    protected int getItemsPerPage() {
        return DEFAULT_ITEMS_PER_PAGE;
    }

    protected int[] getPageSlots() {
        return DEFAULT_PAGE_SLOTS;
    }

    protected int getPreviousSlot() {
        return DEFAULT_PREV_SLOT;
    }

    protected int getNextSlot() {
        return DEFAULT_NEXT_SLOT;
    }

    protected int getFilterSlot() {
        return DEFAULT_FILTER_SLOT;
    }

    protected int getGuiSize() {
        return 54;
    }

    protected Sound getClickSound() {
        return Sound.UI_BUTTON_CLICK;
    }

    protected Sound getPageSound() {
        return Sound.ITEM_BOOK_PAGE_TURN;
    }

    protected Sound getFilterSound() {
        return Sound.BLOCK_NOTE_BLOCK_BELL;
    }

    public QuickGui createGUI(Player player) {
        int page = currentPages.compute(player.getUniqueId(), (k, v) -> realizePage(player, v == null ? 0 : v));

        QuickGui.GuiBuilder builder = QuickGui.create()
                .titleMini(getTitle(player))
                .size(getGuiSize())
                .onGlobalClick((gui, event) -> event.setCancelled(true))
                .clickSound(getClickSound(), 0.5f, 1.0f);

        builder.item(getPreviousSlot(), createNavigationItem("Previous", page - 1),
                (gui, event) -> changePage(player, -1));

        builder.item(getNextSlot(), createNavigationItem("Next", page + 1),
                (gui, event) -> changePage(player, 1));

        builder.item(getFilterSlot(), createFilterItem(player),
                (gui, event) -> {
                    if (event.isShiftClick()) {
                        cycleFilterOperator(player);
                        player.openInventory(createGUI(player).getInventory());
                    } else {
                        openFilterMenu(player);
                    }
                });

        fillEmptySlots(builder);

        setupPageItems(builder, player, page);

        return builder.build();
    }

    private void setupPageItems(QuickGui.GuiBuilder builder, Player player, int page) {
        List<T> filteredItems = filterItems(player);
        int[] pageSlots = getPageSlots();
        int itemsPerPage = pageSlots.length;
        int startIndex = page * itemsPerPage;

        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;

            if (itemIndex >= filteredItems.size()) {
                break;
            }

            T item = filteredItems.get(itemIndex);
            int slot = pageSlots[i];

            builder.item(slot, createDisplayItem(item),
                    (gui, event) -> handleItemClick(player, item, event));
        }
    }

    private void fillEmptySlots(QuickGui.GuiBuilder builder) {
        int[] navigationSlots = {46, 47, 48, 50, 51, 52};
        for (int slot : navigationSlots) {
            builder.item(slot, createPlaceholderItem());
        }
    }

    private void changePage(Player player, int direction) {
        int current = currentPages.getOrDefault(player.getUniqueId(), 0);

        if (current == 0 && direction < 0) {
            player.playSound(player.getLocation(), getPageSound(), 1.0f, 0.8f);
            openBackGUI(player);
            return;
        }

        List<T> filteredItems = filterItems(player);
        int itemsPerPage = getItemsPerPage();
        int maxPages = (filteredItems.isEmpty() ? 0 : (filteredItems.size() - 1) / itemsPerPage) + 1;

        if (current >= maxPages && direction > 0) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        int newPage = current + direction;
        currentPages.put(player.getUniqueId(), newPage);
        player.playSound(player.getLocation(), getPageSound(), 1.0f, 1.0f);
        createGUI(player).open(player);
    }


    private int realizePage(Player player, int requestedPage) {
        int validPage = Math.max(0, requestedPage);
        List<T> filteredItems = filterItems(player);
        int maxPages = Math.max(0, (int) Math.ceil((double) filteredItems.size() / getPageSlots().length) - 1);
        return Math.min(validPage, maxPages);
    }

    private void openFilterMenu(Player player) {
        Set<String> filters = activeFilters.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        FilterOperator operator = chosenOperator.computeIfAbsent(player.getUniqueId(), k -> FilterOperator.AND);

        QuickGui.GuiBuilder filterGui = QuickGui.create()
                .titleMini("<gold><bold>Filters")
                .rows(3)
                .onGlobalClick((gui, event) -> event.setCancelled(true))
                .clickSound(getClickSound(), 0.5f, 1.0f);

        filterGui.item(13, createOperatorItem(operator), (gui, event) -> {
            cycleFilterOperator(player);
            openFilterMenu(player);
        });

        filterGui.item(26, createBackItem(), (gui, event) -> {
            player.playSound(player.getLocation(), getPageSound(), 1.0f, 0.8f);
            createGUI(player).open(player);
        });

        addFilterItems(filterGui, player, filters);

        player.playSound(player.getLocation(), getFilterSound(), 1.0f, 0.8f);
        filterGui.build().open(player);
    }

    private void cycleFilterOperator(Player player) {
        FilterOperator current = chosenOperator.computeIfAbsent(player.getUniqueId(), k -> FilterOperator.AND);
        FilterOperator[] values = FilterOperator.values();
        int nextIndex = (current.ordinal() + 1) % values.length;
        chosenOperator.put(player.getUniqueId(), values[nextIndex]);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
    }

    protected void toggleFilter(Player player, String filterKey) {
        Set<String> filters = activeFilters.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());

        if (filters.contains(filterKey)) {
            filters.remove(filterKey);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
        } else {
            filters.add(filterKey);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        }

        openFilterMenu(player);
    }

    private List<T> filterItems(Player player) {
        List<T> allItems = getAllItems(player);
        Set<String> filters = activeFilters.get(player.getUniqueId());

        if (filters == null || filters.isEmpty()) {
            return allItems;
        }

        FilterOperator operator = chosenOperator.computeIfAbsent(player.getUniqueId(), k -> FilterOperator.AND);
        return allItems.stream()
                .filter(item -> applyFilters(player, item, filters, operator))
                .toList();
    }

    private boolean applyFilters(Player player, T item, Set<String> filters, FilterOperator operator) {
        boolean result = (operator == FilterOperator.AND);
        for (String filter : filters) {
            boolean conditionMet = testFilter(player, item, filter);
            result = operator.apply(result, conditionMet);

            if (operator == FilterOperator.AND && !result) return false;
            if (operator == FilterOperator.OR && result) return true;
        }

        return result;
    }

    protected boolean testFilter(Player player, T item, String filterKey) {
        return true;
    }

    protected int getFilteredCount(Player player) {
        return filterItems(player).size();
    }

    protected int getFilterCount(Player player) {
        Set<String> filters = activeFilters.get(player.getUniqueId());
        return filters != null ? filters.size() : 0;
    }

    private ItemStack createNavigationItem(String direction, int page) {
        if (page < 0) {
            return createBackItem();
        }

        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(direction + " Page")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(
                    Component.text("Page " + page)
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Back")
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createPlaceholderItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createFilterItem(Player player) {
        FilterOperator operator = chosenOperator.computeIfAbsent(player.getUniqueId(), k -> FilterOperator.AND);
        int filterCount = getFilterCount(player);

        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Filters")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Filters Selected: " + filterCount)
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Shift-Click to cycle filter operator")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Current Operator: " + operator.name())
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createOperatorItem(FilterOperator operator) {
        ItemStack item = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Filter Operator: " + operator.name())
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Current: " + operator.name())
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Click to cycle")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            lore.add(Component.text(""));
            lore.add(Component.text("AND: All conditions must be met")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("OR: At least one condition must be met")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("NAND: At least one condition must NOT be met")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("XOR: Exactly one condition must be met")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createFilterToggleItem(String name, Material material, boolean active) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name)
                    .color(active ? NamedTextColor.GREEN : NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(
                    Component.text("Click to " + (active ? "disable" : "enable"))
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createFilterToggleItemWithValue(String name, Material material, boolean active, String value) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name)
                    .color(active ? NamedTextColor.GREEN : NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Value: " + value)
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Left Click to " + (active ? "disable" : "enable"))
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Right Click to set value")
                    .color(NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void clearPlayerData(UUID playerUUID) {
        currentPages.remove(playerUUID);
        activeFilters.remove(playerUUID);
        chosenOperator.remove(playerUUID);
    }

    public enum FilterOperator {
        AND,
        OR,
        NAND,
        XOR;

        public boolean apply(boolean currentValue, boolean newCondition) {
            return switch (this) {
                case AND -> currentValue & newCondition;
                case OR -> currentValue | newCondition;
                case NAND -> !(currentValue & newCondition);
                case XOR -> currentValue ^ newCondition;
            };
        }
    }
}