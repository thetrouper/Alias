package me.trouper.alias.data.enums;

import org.bukkit.Material;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public enum ValidMaterial {
    AMETHYST(TrimMaterial.AMETHYST),
    COPPER(TrimMaterial.COPPER),
    DIAMOND(TrimMaterial.DIAMOND),
    EMERALD(TrimMaterial.EMERALD),
    GOLD(TrimMaterial.GOLD),
    IRON(TrimMaterial.IRON),
    LAPIS(TrimMaterial.LAPIS),
    NETHERITE(TrimMaterial.NETHERITE),
    QUARTZ(TrimMaterial.QUARTZ),
    REDSTONE(TrimMaterial.REDSTONE);
    
    private final TrimMaterial canonical;

    ValidMaterial(TrimMaterial canonical) {
        this.canonical = canonical;
    }

    public TrimMaterial getCanonical() {
        return canonical;
    }

    public static ValidMaterial validate(TrimMaterial material) {
        for (ValidMaterial value : ValidMaterial.values()) {
            if (!value.getCanonical().equals(material)) continue;
            return value;
        }
        return null;
    }

    public static TrimMaterial validate(String name) {
        name = name.toUpperCase();
        return ValidMaterial.valueOf(name).getCanonical();
    }
}