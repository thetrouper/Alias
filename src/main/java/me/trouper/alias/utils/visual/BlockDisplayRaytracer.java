package me.trouper.alias.utils.visual;

import me.trouper.alias.server.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockDisplayRaytracer implements Main {
    
    public static void cleanup() {
        JavaPlugin plugin = main.getPlugin();
        List<World> worlds = plugin.getServer().getWorlds();
        List<Entity> entities = new ArrayList<>();
        for (World world : worlds) {
            entities.addAll(world.getEntities().stream().filter(entity -> entity.getScoreboardTags().contains("$/TrimAlias/ Temp")).toList());
            entities.forEach(entity -> {
                if (entity != null) entity.remove();
            });
        }
    }

    public static void outline(Material display, Location location, long stayTime, List<Player> viewers) {
        outline(display, location, 0.05, stayTime, viewers);
    }

    public static void outline(Material display, Location corner1, Location corner2, double thickness, long stayTime, List<Player> viewers) {
        World world = corner1.getWorld();

        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        Location a1 = new Location(world, minX, minY, minZ);
        Location a2 = new Location(world, maxX + 1, minY, minZ);
        Location a3 = new Location(world, maxX + 1, minY, maxZ + 1);
        Location a4 = new Location(world, minX, minY, maxZ + 1);

        Location b1 = new Location(world, minX, maxY + 1, minZ);
        Location b2 = new Location(world, maxX + 1, maxY + 1, minZ);
        Location b3 = new Location(world, maxX + 1, maxY + 1, maxZ + 1);
        Location b4 = new Location(world, minX, maxY + 1, maxZ + 1);

        trace(display, a1, a2, thickness, stayTime, viewers);
        trace(display, a2, a3, thickness, stayTime, viewers);
        trace(display, a3, a4, thickness, stayTime, viewers);
        trace(display, a4, a1, thickness, stayTime, viewers);

        trace(display, b1, b2, thickness, stayTime, viewers);
        trace(display, b2, b3, thickness, stayTime, viewers);
        trace(display, b3, b4, thickness, stayTime, viewers);
        trace(display, b4, b1, thickness, stayTime, viewers);

        trace(display, a1, b1, thickness, stayTime, viewers);
        trace(display, a2, b2, thickness, stayTime, viewers);
        trace(display, a3, b3, thickness, stayTime, viewers);
        trace(display, a4, b4, thickness, stayTime, viewers);
    }


    public static void outline(Material display, Location location, double thickness, long stayTime, List<Player> viewers) {
        Location og = location.getBlock().getLocation();

        Location a1 = og.clone().add(0, 0, 0);
        Location a2 = og.clone().add(1, 0, 0);
        Location a3 = og.clone().add(1, 0, 1);
        Location a4 = og.clone().add(0, 0, 1);

        Location b1 = og.clone().add(0, 1, 0);
        Location b2 = og.clone().add(1, 1, 0);
        Location b3 = og.clone().add(1, 1, 1);
        Location b4 = og.clone().add(0, 1, 1);

        trace(display, a1, a2, thickness, stayTime, viewers);
        trace(display, a2, a3, thickness, stayTime, viewers);
        trace(display, a3, a4, thickness, stayTime, viewers);
        trace(display, a4, a1, thickness, stayTime, viewers);

        trace(display, b1, b2, thickness, stayTime, viewers);
        trace(display, b2, b3, thickness, stayTime, viewers);
        trace(display, b3, b4, thickness, stayTime, viewers);
        trace(display, b4, b1, thickness, stayTime, viewers);

        trace(display, a1, b1, thickness, stayTime, viewers);
        trace(display, a2, b2, thickness, stayTime, viewers);
        trace(display, a3, b3, thickness, stayTime, viewers);
        trace(display, a4, b4, thickness, stayTime, viewers);
    }

    public static void trace(Material display, Location start, Location end, long stayTime, List<Player> viewers) {
        trace(display, start, end.toVector().subtract(start.toVector()), 0.05, end.distance(start), stayTime, viewers);
    }

    public static void trace(Material display, Location start, Location end, double thickness, long stayTime, List<Player> viewers) {
        trace(display, start, end.toVector().subtract(start.toVector()), thickness, end.distance(start), stayTime, viewers);
    }

    public static void trace(Material display, Location start, Vector direction, double thickness, double distance, long stayTime, List<Player> viewers) {
        World world = start.getWorld();

        BlockDisplay beam = world.spawn(start, BlockDisplay.class, entity -> {
            AxisAngle4f angle = new AxisAngle4f(0, 0, 0, 1);
            Vector3f transition = new Vector3f(-(float)(thickness / 2F));
            Vector3f scale = new Vector3f((float)thickness, (float)thickness, (float)distance);
            Transformation trans = new Transformation(transition, angle, scale, angle);
            Location vector = entity.getLocation();

            vector.setDirection(direction);
            entity.teleport(vector);
            entity.setBlock(display.createBlockData());
            entity.setBrightness(new Display.Brightness(15, 15));
            entity.setInterpolationDelay(0);
            entity.setTransformation(trans);
            entity.addScoreboardTag("$/TrimAlias/ Temp");
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!viewers.contains(player)) {
                    player.hideEntity(main.getPlugin(), entity);
                }
            }

            Bukkit.getScheduler().runTaskLater(main.getPlugin(), entity::remove, stayTime);
        });
    }

    public static void trace(Material display, Location start, Vector direction, double thickness, double distance, long stayTime, Consumer<BlockDisplay> onEntitySpawn, List<Player> viewers) {
        World world = start.getWorld();

        BlockDisplay beam = world.spawn(start, BlockDisplay.class, entity -> {
            AxisAngle4f angle = new AxisAngle4f(0, 0, 0, 1);
            Vector3f transition = new Vector3f(-(float)(thickness / 2F));
            Vector3f scale = new Vector3f((float)thickness, (float)thickness, (float)distance);
            Transformation trans = new Transformation(transition, angle, scale, angle);
            Location vector = entity.getLocation();

            vector.setDirection(direction);
            entity.teleport(vector);
            entity.setBlock(display.createBlockData());
            entity.setBrightness(new Display.Brightness(15, 15));
            entity.setInterpolationDelay(0);
            entity.setTransformation(trans);
            entity.addScoreboardTag("$/TrimAlias/ Temp");
            

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!viewers.contains(player)) {
                    player.hideEntity(main.getPlugin(), entity);
                }
            }

            Bukkit.getScheduler().runTaskLater(main.getPlugin(), entity::remove, stayTime);
            Bukkit.getScheduler().runTaskLater(main.getPlugin(), () -> onEntitySpawn.accept(entity), 5);
        });
    }

    public static List<BlockDisplay> outline(Material display, Location location, long stayTime) {
        return outline(display, location, 0.05, stayTime);
    }

    public static List<BlockDisplay> outline(Material display, Location location, double thickness, long stayTime) {
        Location og = location.getBlock().getLocation();

        Location a1 = og.clone().add(0, 0, 0);
        Location a2 = og.clone().add(1, 0, 0);
        Location a3 = og.clone().add(1, 0, 1);
        Location a4 = og.clone().add(0, 0, 1);

        Location b1 = og.clone().add(0, 1, 0);
        Location b2 = og.clone().add(1, 1, 0);
        Location b3 = og.clone().add(1, 1, 1);
        Location b4 = og.clone().add(0, 1, 1);

        List<BlockDisplay> a = new ArrayList<>();

        a.add(trace(display, a1, a2, thickness, stayTime));
        a.add(trace(display, a2, a3, thickness, stayTime));
        a.add(trace(display, a3, a4, thickness, stayTime));
        a.add(trace(display, a4, a1, thickness, stayTime));

        a.add(trace(display, b1, b2, thickness, stayTime));
        a.add(trace(display, b2, b3, thickness, stayTime));
        a.add(trace(display, b3, b4, thickness, stayTime));
        a.add(trace(display, b4, b1, thickness, stayTime));

        a.add(trace(display, a1, b1, thickness, stayTime));
        a.add(trace(display, a2, b2, thickness, stayTime));
        a.add(trace(display, a3, b3, thickness, stayTime));
        a.add(trace(display, a4, b4, thickness, stayTime));

        return a;
    }

    public static void highlightCollisions(Block block, Color color, long stayTime) {
        if (block == null || block.isEmpty() || !block.isCollidable())
            return;

        VoxelShape shape = block.getCollisionShape();
        World world = block.getWorld();
        Vector offset = block.getLocation().toVector();

        for (BoundingBox box : shape.getBoundingBoxes()) {
            highlight(box, offset, world, color, stayTime);
        }
    }

    public static void highlight(BoundingBox box, Vector offset, World world, Color color, long stayTime) {
        double x1 = box.getMinX() + offset.getX();
        double y1 = box.getMinY() + offset.getY();
        double z1 = box.getMinZ() + offset.getZ();
        double x2 = box.getMaxX() + offset.getX();
        double y2 = box.getMaxY() + offset.getY();
        double z2 = box.getMaxZ() + offset.getZ();

        traceGlowing(world, x1, y1, z1, x2, y1, z1, color, stayTime);
        traceGlowing(world, x2, y1, z1, x2, y1, z2, color, stayTime);
        traceGlowing(world, x2, y1, z2, x1, y1, z2, color, stayTime);
        traceGlowing(world, x1, y1, z2, x1, y1, z1, color, stayTime);

        traceGlowing(world, x1, y2, z1, x2, y2, z1, color, stayTime);
        traceGlowing(world, x2, y2, z1, x2, y2, z2, color, stayTime);
        traceGlowing(world, x2, y2, z2, x1, y2, z2, color, stayTime);
        traceGlowing(world, x1, y2, z2, x1, y2, z1, color, stayTime);

        traceGlowing(world, x1, y1, z1, x1, y2, z1, color, stayTime);
        traceGlowing(world, x2, y1, z1, x2, y2, z1, color, stayTime);
        traceGlowing(world, x2, y1, z2, x2, y2, z2, color, stayTime);
        traceGlowing(world, x1, y1, z2, x1, y2, z2, color, stayTime);
    }

    public static void traceGlowing(World world, double x1, double y1, double z1, double x2, double y2, double z2, Color color, long stayTime) {
        Location loc1 = new Location(world, x1, y1, z1);
        Location loc2 = new Location(world, x2, y2, z2);
        BlockDisplay ent = trace(Material.WHITE_CONCRETE, loc1, loc2, 0.01, stayTime);
        ent.setGlowColorOverride(color);
        ent.setGlowing(true);
    }

    public static BlockDisplay trace(Material display, Location start, Location end, long stayTime) {
        return trace(display, start, end.toVector().subtract(start.toVector()), 0.05, end.distance(start), stayTime);
    }

    public static BlockDisplay trace(Material display, Location start, Location end, double thickness, long stayTime) {
        return trace(display, start, end.toVector().subtract(start.toVector()), thickness, end.distance(start), stayTime);
    }

    public static BlockDisplay trace(Material display, Location start, Vector direction, double thickness, double distance, long stayTime) {
        World world = start.getWorld();

        BlockDisplay entity = world.spawn(start, BlockDisplay.class);
        AxisAngle4f angle = new AxisAngle4f(0, 0, 0, 1);
        Vector3f transition = new Vector3f(-(float)(thickness / 2F));
        Vector3f scale = new Vector3f((float)thickness, (float)thickness, (float)distance);
        Transformation trans = new Transformation(transition, angle, scale, angle);
        Location vector = entity.getLocation();

        vector.setDirection(direction);
        entity.teleport(vector);
        entity.setBlock(display.createBlockData());
        entity.setBrightness(new Display.Brightness(15, 15));
        entity.setInterpolationDelay(0);
        entity.setTransformation(trans);
        entity.addScoreboardTag("$/TrimAlias/ Temp");
        
        Bukkit.getScheduler().runTaskLater(main.getPlugin(), entity::remove, stayTime);
        return entity;
    }

    public static void transform(BlockDisplay display, Location start, Location end, double thickness) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();

        Location loc = start.clone();
        loc.setDirection(direction);
        display.teleport(loc);

        Vector3f translation = new Vector3f(-(float)(thickness / 2F), 0, 0); // Centered
        Vector3f scale = new Vector3f((float)thickness, (float)thickness, (float)distance);
        AxisAngle4f rotation = new AxisAngle4f(0, 0, 0, 1);
        Transformation transformation = new Transformation(translation, rotation, scale, rotation);

        display.setTransformation(transformation);
    }

    public static void transform(BlockDisplay display, Location start, Vector direction, double distance, double thickness) {
        Location loc = start.clone();
        loc.setDirection(direction);
        display.teleport(loc);

        Vector3f translation = new Vector3f(-(float)(thickness / 2F), 0, 0);
        Vector3f scale = new Vector3f((float)thickness, (float)thickness, (float)distance);
        AxisAngle4f rotation = new AxisAngle4f(0, 0, 0, 1);
        Transformation transformation = new Transformation(translation, rotation, scale, rotation);

        display.setTransformation(transformation);
    }


    public static void translate(BlockDisplay display, Vector3f offset) {
        Transformation current = display.getTransformation();
        Vector3f translation = new Vector3f(current.getTranslation()).add(offset);
        display.setTransformation(new Transformation(
                translation,
                current.getLeftRotation(),
                current.getScale(),
                current.getRightRotation()
        ));
    }

    public static void scale(BlockDisplay display, Vector3f scale) {
        Transformation current = display.getTransformation();
        display.setTransformation(new Transformation(
                current.getTranslation(),
                current.getLeftRotation(),
                scale,
                current.getRightRotation()
        ));
    }

    public static void rotate(BlockDisplay display, AxisAngle4f rotation) {
        Transformation current = display.getTransformation();
        display.setTransformation(new Transformation(
                current.getTranslation(),
                rotation,
                current.getScale(),
                rotation
        ));
    }

    public static void alignToDirection(BlockDisplay display, Vector direction) {
        Location loc = display.getLocation().clone();
        loc.setDirection(direction);
        display.teleport(loc);
    }

}
