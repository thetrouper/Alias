package me.trouper.alias.data.enums;

import org.bukkit.Material;
import org.bukkit.inventory.meta.trim.TrimPattern;

public enum ValidPattern {
    BOLT(TrimPattern.BOLT, Material.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE),
    COAST(TrimPattern.COAST, Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE),
    DUNE(TrimPattern.DUNE, Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE),
    EYE(TrimPattern.EYE, Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE),
    FLOW(TrimPattern.FLOW, Material.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE),
    HOST(TrimPattern.HOST, Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE),
    RAISER(TrimPattern.RAISER, Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE),
    RIB(TrimPattern.RIB, Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE),
    SENTRY(TrimPattern.SENTRY, Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE),
    SHAPER(TrimPattern.SHAPER, Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE),
    SILENCE(TrimPattern.SILENCE, Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE),
    SNOUT(TrimPattern.SNOUT, Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE),
    SPIRE(TrimPattern.SPIRE, Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE),
    TIDE(TrimPattern.TIDE, Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE),
    VEX(TrimPattern.VEX, Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE),
    WARD(TrimPattern.WARD, Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE),
    WAYFINDER(TrimPattern.WAYFINDER, Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE),
    WILD(TrimPattern.WILD, Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE);

    private final TrimPattern canonical;
    private final Material material;

    ValidPattern(TrimPattern canonical, Material material) {
        this.canonical = canonical;
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public TrimPattern getCanonical() {
        return canonical;
    }

    public static ValidPattern validate(TrimPattern pattern) {
        for (ValidPattern value : ValidPattern.values()) {
            if (!value.getCanonical().equals(pattern)) continue;
            return value;
        }
        return null;
    }

    public static TrimPattern validate(String name) {
        name = name.toUpperCase();
        return ValidPattern.valueOf(name).getCanonical();
    }
}
