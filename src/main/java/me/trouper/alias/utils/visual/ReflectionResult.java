package me.trouper.alias.utils.visual;

import org.bukkit.util.Vector;

public class ReflectionResult {
    private final Point hitPoint;
    private final boolean shouldReflect;
    private final Vector reflectedDirection;

    public ReflectionResult(Point hitPoint, boolean shouldReflect, Vector reflectedDirection) {
        this.hitPoint = hitPoint;
        this.shouldReflect = shouldReflect;
        this.reflectedDirection = reflectedDirection;
    }

    public Point getHitPoint() {
        return hitPoint;
    }

    public boolean shouldReflect() {
        return shouldReflect;
    }

    public Vector getReflectedDirection() {
        return reflectedDirection;
    }
}
