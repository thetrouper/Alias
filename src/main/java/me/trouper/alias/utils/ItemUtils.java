package me.trouper.alias.utils;

import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class ItemUtils {

    public static boolean isArmor(ItemStack i) {
        if (i == null || i.isEmpty()) return false;
        
        return isHelmet(i) || isChestplate(i) || isLeggings(i) || isBoots(i);
    }
    
    public static boolean isHelmet(ItemStack i) {
        if (i == null || i.isEmpty()) return false;

        Material m = i.getType();
        String n = m.name();
        return n.contains("HELMET");
    }

    public static boolean isChestplate(ItemStack i) {
        if (i == null || i.isEmpty()) return false;

        Material m = i.getType();
        String n = m.name();
        return n.contains("CHESTPLATE");
    }
    
    public static boolean isLeggings(ItemStack i) {
        if (i == null || i.isEmpty()) return false;

        Material m = i.getType();
        String n = m.name();
        return n.contains("LEGGINGS");
    }
    
    public static boolean isBoots(ItemStack i) {
        if (i == null || i.isEmpty()) return false;

        Material m = i.getType();
        String n = m.name();
        return n.contains("BOOTS");
    }
    
    @SuppressWarnings("deprecation")
    public static boolean isSimilar(ItemStack item1, ItemStack item2) {
        if (item1 == null || item2 == null) {
            Verbose.send("One of the items is null: item1: %s, item2: %s", item1, item2);
            return false;
        }

        Material type1 = item1.getType();
        Material type2 = item2.getType();
        boolean typeEqual = type1 == type2;
        Verbose.send("Checking Material: item1 type: %s, item2 type: %s, equal: %s", type1, type2, typeEqual);
        if (!typeEqual) return false;

        boolean hasMeta1 = item1.hasItemMeta();
        boolean hasMeta2 = item2.hasItemMeta();
        boolean metaExistEqual = (hasMeta1 == hasMeta2);
        Verbose.send("Checking ItemMeta existence: item1 has meta: %s, item2 has meta: %s, equal: %s", hasMeta1, hasMeta2, metaExistEqual);
        if (!metaExistEqual) return false;

        if (!hasMeta1 && !hasMeta2) {
            return true;
        }

        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();
        
        String name1 = meta1.hasDisplayName() ? meta1.getDisplayName() : null;
        String name2 = meta2.hasDisplayName() ? meta2.getDisplayName() : null;
        if (name1 == null ^ name2 == null) {
            Verbose.send("Custom Name mismatch: item1 name: %s, item2 name: %s", name1, name2);
            return false;
        }
        boolean nameEqual = (name1 == null || name1.equals(name2));
        Verbose.send("Checking Custom Name: item1: %s, item2: %s, equal: %s", name1, name2, nameEqual);
        if (!nameEqual) return false;

        List<String> lore1 = meta1.hasLore() ? meta1.getLore() : null;
        List<String> lore2 = meta2.hasLore() ? meta2.getLore() : null;
        if (lore1 == null ^ lore2 == null) {
            Verbose.send("Lore mismatch: item1 lore: %s, item2 lore: %s", lore1, lore2);
            return false;
        }
        boolean loreEqual = (lore1 == null || lore1.equals(lore2));
        Verbose.send("Checking Lore: item1: %s, item2: %s, equal: %s", lore1, lore2, loreEqual);
        if (!loreEqual) return false;

        int cmd1 = meta1.hasCustomModelData() ? meta1.getCustomModelData() : -1;
        int cmd2 = meta2.hasCustomModelData() ? meta2.getCustomModelData() : -1;
        boolean cmdEqual = (cmd1 == cmd2);
        Verbose.send("Checking Custom Model Data: item1: %d, item2: %d, equal: %s", cmd1, cmd2, cmdEqual);
        if (!cmdEqual) return false;

        Map<Enchantment, Integer> enchants1 = meta1.getEnchants();
        Map<Enchantment, Integer> enchants2 = meta2.getEnchants();
        if (enchants1 == null ^ enchants2 == null) {
            Verbose.send("Enchantments mismatch: item1 enchants: %s, item2 enchants: %s", enchants1, enchants2);
            return false;
        }
        boolean enchantsEqual = (enchants1 == null || enchants1.equals(enchants2));
        Verbose.send("Checking Enchantments: item1: %s, item2: %s, equal: %s", enchants1, enchants2, enchantsEqual);
        if (!enchantsEqual) return false;

        Multimap<Attribute, AttributeModifier> modifiers1 = meta1.getAttributeModifiers();
        Multimap<Attribute, AttributeModifier> modifiers2 = meta2.getAttributeModifiers();
        if (modifiers1 == null ^ modifiers2 == null) {
            Verbose.send("Attribute Modifiers mismatch: item1 modifiers: %s, item2 modifiers: %s", modifiers1, modifiers2);
            return false;
        }
        boolean modifiersEqual = (modifiers1 == null || modifiers1.equals(modifiers2));
        Verbose.send("Checking Attribute Modifiers: item1: %s, item2: %s, equal: %s", modifiers1, modifiers2, modifiersEqual);
        if (!modifiersEqual) return false;
        
        return true;
    }
    
}
