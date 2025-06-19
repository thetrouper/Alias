package me.trouper.alias.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ItemBuilder {
    private ItemStack stack;
    private ItemMeta meta;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ItemBuilder() {
        this(new ItemStack(Material.STONE));
    }

    public ItemBuilder(ItemStack stack) {
        this.stack = stack.clone();
        this.meta = this.stack.getItemMeta();
        if (this.meta == null) {
            throw new IllegalArgumentException("ItemStack must have ItemMeta");
        }
    }

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(Material material, int amount) {
        this(new ItemStack(material, amount));
    }

    public ItemBuilder material(Material material) {
        this.stack = this.stack.withType(material);
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.stack.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    public ItemBuilder displayName(Component name) {
        this.meta.displayName(name);
        return this;
    }

    public ItemBuilder displayName(String miniMessageText) {
        Component component = miniMessage.deserialize(miniMessageText)
                .decoration(TextDecoration.ITALIC, false);
        return displayName(component);
    }

    public ItemBuilder displayNameRaw(String text) {
        Component component = Component.text(text)
                .decoration(TextDecoration.ITALIC, false);
        return displayName(component);
    }

    public ItemBuilder loreComponent(Component line) {
        List<Component> lore = this.meta.hasLore() ? this.meta.lore() : new ArrayList<>();
        if (lore == null) lore = new ArrayList<>();
        lore.add(line.decoration(TextDecoration.ITALIC, false));
        this.meta.lore(lore);
        return this;
    }

    public ItemBuilder loreComponent(List<Component> lines) {
        List<Component> processedLore = new ArrayList<>();
        for (Component line : lines) {
            processedLore.add(line.decoration(TextDecoration.ITALIC, false));
        }
        this.meta.lore(processedLore);
        return this;
    }

    public ItemBuilder loreComponent(Component... lines) {
        return loreComponent(Arrays.asList(lines));
    }

    public ItemBuilder loreMiniMessage(String line) {
        Component component = miniMessage.deserialize(line)
                .decoration(TextDecoration.ITALIC, false);
        return loreComponent(component);
    }

    public ItemBuilder loreMiniMessage(List<String> lines) {
        List<Component> components = new ArrayList<>();
        for (String line : lines) {
            components.add(miniMessage.deserialize(line)
                    .decoration(TextDecoration.ITALIC, false));
        }
        return loreComponent(components);
    }

    public ItemBuilder loreMiniMessage(String... lines) {
        return loreMiniMessage(Arrays.asList(lines));
    }

    public ItemBuilder loreRaw(String line) {
        Component component = Component.text(line)
                .decoration(TextDecoration.ITALIC, false);
        return loreComponent(component);
    }

    public ItemBuilder loreRaw(List<String> lines) {
        List<Component> components = new ArrayList<>();
        for (String line : lines) {
            components.add(Component.text(line)
                    .decoration(TextDecoration.ITALIC, false));
        }
        return loreComponent(components);
    }

    public ItemBuilder loreRaw(String... lines) {
        return loreRaw(Arrays.asList(lines));
    }

    public ItemBuilder clearLore() {
        this.meta.lore(new ArrayList<>());
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment) {
        return enchant(enchantment, 1);
    }

    public ItemBuilder removeEnchant(Enchantment enchantment) {
        this.meta.removeEnchant(enchantment);
        return this;
    }

    public ItemBuilder clearEnchants() {
        this.meta.getEnchants().keySet().forEach(this.meta::removeEnchant);
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        this.meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder removeFlags(ItemFlag... flags) {
        this.meta.removeItemFlags(flags);
        return this;
    }

    public ItemBuilder hideAllFlags() {
        return flags(ItemFlag.values());
    }

    public ItemBuilder attribute(Attribute attribute, AttributeModifier modifier) {
        this.meta.addAttributeModifier(attribute, modifier);
        return this;
    }

    public ItemBuilder removeAttribute(Attribute attribute) {
        this.meta.removeAttributeModifier(attribute);
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder unbreakable() {
        return unbreakable(true);
    }

    public ItemBuilder modifyStack(Function<ItemStack, ItemStack> modifier) {
        this.stack = modifier.apply(this.build());
        this.meta = this.stack.getItemMeta();
        return this;
    }

    public ItemBuilder modifyMeta(Function<ItemMeta, ItemMeta> modifier) {
        this.meta = modifier.apply(this.meta);
        return this;
    }

    public <T extends ItemMeta> ItemBuilder modifyMeta(Class<T> metaClass, Function<T, T> modifier) {
        if (metaClass.isInstance(this.meta)) {
            this.meta = modifier.apply(metaClass.cast(this.meta));
        }
        return this;
    }

    public ItemStack build() {
        this.stack.setItemMeta(this.meta);
        return this.stack.clone();
    }

    public ItemStack buildAndGet() {
        return build();
    }

    public ItemBuilder clone() {
        return new ItemBuilder(this.build());
    }

    public Material getMaterial() {
        return this.stack.getType();
    }

    public int getAmount() {
        return this.stack.getAmount();
    }

    public static ItemBuilder create() {
        return new ItemBuilder();
    }

    public static ItemBuilder create(ItemStack stack) {
        return new ItemBuilder(stack);
    }

    public static ItemBuilder create(Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder create(Material material, int amount) {
        return new ItemBuilder(material, amount);
    }

    public static ItemBuilder of(Material material) {
        return create(material);
    }

    public static ItemBuilder of(ItemStack stack) {
        return create(stack);
    }
}
