package me.trouper.alias.data.enums;

import org.bukkit.Material;
import org.bukkit.inventory.meta.trim.TrimMaterial;

public enum ValidTrimMaterial {
    AMETHYST(TrimMaterial.AMETHYST, Material.AMETHYST_SHARD),
    COPPER(TrimMaterial.COPPER, Material.COPPER_INGOT),
    DIAMOND(TrimMaterial.DIAMOND, Material.DIAMOND),
    EMERALD(TrimMaterial.EMERALD, Material.EMERALD),
    GOLD(TrimMaterial.GOLD, Material.GOLD_INGOT),
    IRON(TrimMaterial.IRON, Material.IRON_INGOT),
    LAPIS(TrimMaterial.LAPIS, Material.LAPIS_LAZULI),
    NETHERITE(TrimMaterial.NETHERITE, Material.NETHERITE_INGOT),
    QUARTZ(TrimMaterial.QUARTZ, Material.QUARTZ),
    REDSTONE(TrimMaterial.REDSTONE, Material.REDSTONE),
    RESIN(TrimMaterial.RESIN, Material.RESIN_BRICK);
    
    private final TrimMaterial canonical;
    private final Material material;

    ValidTrimMaterial(TrimMaterial canonical, Material material) {
        this.canonical = canonical;
        this.material = material;
    }

    public TrimMaterial getCanonical() {
        return canonical;
    }
    
    public Material getMaterial() {
        return material;
    }

    public static ValidTrimMaterial validate(TrimMaterial material) {
        for (ValidTrimMaterial value : ValidTrimMaterial.values()) {
            if (!value.getCanonical().equals(material)) continue;
            return value;
        }
        return null;
    }

    public static TrimMaterial validate(String name) {
        name = name.toUpperCase();
        return ValidTrimMaterial.valueOf(name).getCanonical();
    }
}