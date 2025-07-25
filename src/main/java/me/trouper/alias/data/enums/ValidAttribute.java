package me.trouper.alias.data.enums;

import org.bukkit.attribute.Attribute;

public enum ValidAttribute {
    MAX_HEALTH(Attribute.MAX_HEALTH),
    FOLLOW_RANGE(Attribute.FOLLOW_RANGE),
    KNOCKBACK_RESISTANCE(Attribute.KNOCKBACK_RESISTANCE),
    MOVEMENT_SPEED(Attribute.MOVEMENT_SPEED),
    FLYING_SPEED(Attribute.FLYING_SPEED),
    ATTACK_DAMAGE(Attribute.ATTACK_DAMAGE),
    ATTACK_KNOCKBACK(Attribute.ATTACK_KNOCKBACK),
    ATTACK_SPEED(Attribute.ATTACK_SPEED),
    ARMOR(Attribute.ARMOR),
    ARMOR_TOUGHNESS(Attribute.ARMOR_TOUGHNESS),
    FALL_DAMAGE_MULTIPLIER(Attribute.FALL_DAMAGE_MULTIPLIER),
    LUCK(Attribute.LUCK),
    MAX_ABSORPTION(Attribute.MAX_ABSORPTION),
    SAFE_FALL_DISTANCE(Attribute.SAFE_FALL_DISTANCE),
    SCALE(Attribute.SCALE),
    STEP_HEIGHT(Attribute.STEP_HEIGHT),
    GRAVITY(Attribute.GRAVITY),
    JUMP_STRENGTH(Attribute.JUMP_STRENGTH),
    BURNING_TIME(Attribute.BURNING_TIME),
    EXPLOSION_KNOCKBACK_RESISTANCE(Attribute.EXPLOSION_KNOCKBACK_RESISTANCE),
    MOVEMENT_EFFICIENCY(Attribute.MOVEMENT_EFFICIENCY),
    OXYGEN_BONUS(Attribute.OXYGEN_BONUS),
    WATER_MOVEMENT_EFFICIENCY(Attribute.WATER_MOVEMENT_EFFICIENCY),
    TEMPT_RANGE(Attribute.TEMPT_RANGE),
    BLOCK_INTERACTION_RANGE(Attribute.BLOCK_INTERACTION_RANGE),
    ENTITY_INTERACTION_RANGE(Attribute.ENTITY_INTERACTION_RANGE),
    BLOCK_BREAK_SPEED(Attribute.BLOCK_BREAK_SPEED),
    MINING_EFFICIENCY(Attribute.MINING_EFFICIENCY),
    SNEAKING_SPEED(Attribute.SNEAKING_SPEED),
    SUBMERGED_MINING_SPEED(Attribute.SUBMERGED_MINING_SPEED),
    SWEEPING_DAMAGE_RATIO(Attribute.SWEEPING_DAMAGE_RATIO),
    SPAWN_REINFORCEMENTS(Attribute.SPAWN_REINFORCEMENTS);

    private final Attribute canonical;

    ValidAttribute(Attribute canonical) {
        this.canonical = canonical;
    }

    public Attribute getCanonical() {
        return canonical;
    }

    public static ValidAttribute validate(Attribute attribute) {
        for (ValidAttribute value : ValidAttribute.values()) {
            if (!value.getCanonical().equals(attribute)) continue;
            return value;
        }
        return null;
    }

    public static Attribute validate(String name) {
        name = name.toUpperCase();
        return ValidAttribute.valueOf(name).getCanonical();
    }
}