package me.trouper.alias.server.systems.display.tracing;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Point {
    private final Location loc;
    private final World world;
    private final Block block;
    private final boolean missed;
    private final double traveledDist;
    private List<Point> rayPath;

    public Point(Location loc, double traveledDist, boolean missed) {
        this.loc = loc;
        this.world = loc.getWorld();
        this.block = loc.getBlock();
        this.missed = missed;
        this.traveledDist = traveledDist;

        if (world == null) {
            throw new IllegalArgumentException("point world cannot be null!");
        }
    }

    public List<Entity> getNearbyEntities(Entity exclude, int range, boolean requireContact, double expansionX, double expansionY, double expansionZ, Predicate<Entity> filter) {
        return new ArrayList<>(world.getNearbyEntities(loc, range, range, range, e -> {
            if (requireContact && !e.getBoundingBox().expand(expansionX, expansionY, expansionZ).contains(loc.toVector())) {
                return false;
            }
            return filter.test(e) && e != exclude;
        }));
    }

    public List<Entity> getNearbyEntities(Entity exclude, int range, boolean requireContact, double expansion, Predicate<Entity> filter) {
        return getNearbyEntities(exclude, range, requireContact, expansion, expansion, expansion, filter);
    }

    public List<Entity> getNearbyEntities(Entity exclude, int range, boolean requireContact, Predicate<Entity> filter) {
        return getNearbyEntities(exclude, range, requireContact, 0, filter);
    }

    public List<Entity> getNearbyEntities(Entity exclude, int range, Predicate<Entity> filter) {
        return getNearbyEntities(exclude, range, false, filter);
    }

    public double getTraveledDist() {
        return traveledDist;
    }

    public boolean wasMissed() {
        return missed;
    }

    public Block getBlock() {
        return block;
    }

    public Location getLoc() {
        return loc;
    }

    public World getWorld() {
        return world;
    }

    public double distance(Location other) {
        return other.distance(loc);
    }
    
    public Point addRayPoint(Point point) {
        rayPath.add(point);
        return point;
    }
    
    public List<Point> setRayPath(List<Point> points) {
        rayPath = points;
        return rayPath;
    }
    
    public List<Point> getRayPath = new ArrayList<>();
}
