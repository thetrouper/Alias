package me.trouper.alias.server.systems.display.tracing;

import me.trouper.alias.AliasContext;
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

public class BlockDisplayRaytracer {

    private final AliasContext context;

    public BlockDisplayRaytracer(AliasContext context) {
        this.context = context;
    }

    public void cleanup() {
        JavaPlugin plugin = context.getPlugin();
        List<World> worlds = plugin.getServer().getWorlds();
        List<Entity> entities = new ArrayList<>();
        for (World world : worlds) {
            entities.addAll(world.getEntities().stream().filter(entity -> entity.getScoreboardTags().contains(context.getCommon().getTempTag())).toList());
            entities.forEach(entity -> {
                if (entity != null) entity.remove();
            });
        }
    }

    public void trace(Material display, Location start, Location end, long stayTime, List<Player> viewers) {
        trace(display, start, end.toVector().subtract(start.toVector()), 0.05, end.distance(start), stayTime, viewers);
    }

    public void trace(Material display, Location start, Location end, double thickness, long stayTime, List<Player> viewers) {
        trace(display, start, end.toVector().subtract(start.toVector()), thickness, end.distance(start), stayTime, viewers);
    }

    public void trace(Material display, Location start, Vector direction, double thickness, double distance, long stayTime, List<Player> viewers) {
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
            entity.addScoreboardTag(context.getCommon().getTempTag());
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!viewers.contains(player)) {
                    player.hideEntity(context.getPlugin(), entity);
                }
            }

            Bukkit.getScheduler().runTaskLater(context.getPlugin(), entity::remove, stayTime);
        });
    }

    public void trace(Material display, Location start, Vector direction, double thickness, double distance, long stayTime, Consumer<BlockDisplay> onEntitySpawn, List<Player> viewers) {
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
            entity.addScoreboardTag(context.getCommon().getTempTag());
            

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!viewers.contains(player)) {
                    player.hideEntity(context.getPlugin(), entity);
                }
            }

            Bukkit.getScheduler().runTaskLater(context.getPlugin(), entity::remove, stayTime);
            Bukkit.getScheduler().runTaskLater(context.getPlugin(), () -> onEntitySpawn.accept(entity), 5);
        });
    }

    public void traceGlowing(World world, double x1, double y1, double z1, double x2, double y2, double z2, Color color, long stayTime) {
        Location loc1 = new Location(world, x1, y1, z1);
        Location loc2 = new Location(world, x2, y2, z2);
        BlockDisplay ent = trace(Material.WHITE_CONCRETE, loc1, loc2, 0.01, stayTime);
        ent.setGlowColorOverride(color);
        ent.setGlowing(true);
    }

    public BlockDisplay trace(Material display, Location start, Location end, long stayTime) {
        return trace(display, start, end.toVector().subtract(start.toVector()), 0.05, end.distance(start), stayTime);
    }

    public BlockDisplay trace(Material display, Location start, Location end, double thickness, long stayTime) {
        return trace(display, start, end.toVector().subtract(start.toVector()), thickness, end.distance(start), stayTime);
    }

    public BlockDisplay trace(Material display, Location start, Vector direction, double thickness, double distance, long stayTime) {
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
        entity.addScoreboardTag(context.getCommon().getTempTag());
        
        Bukkit.getScheduler().runTaskLater(context.getPlugin(), entity::remove, stayTime);
        return entity;
    }

    public void transform(BlockDisplay display, Location start, Location end, double thickness) {
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

    public void transform(BlockDisplay display, Location start, Vector direction, double distance, double thickness) {
        Location loc = start.clone();
        loc.setDirection(direction);
        display.teleport(loc);

        Vector3f translation = new Vector3f(-(float)(thickness / 2F), 0, 0);
        Vector3f scale = new Vector3f((float)thickness, (float)thickness, (float)distance);
        AxisAngle4f rotation = new AxisAngle4f(0, 0, 0, 1);
        Transformation transformation = new Transformation(translation, rotation, scale, rotation);

        display.setTransformation(transformation);
    }


    public void translate(BlockDisplay display, Vector3f offset) {
        Transformation current = display.getTransformation();
        Vector3f translation = new Vector3f(current.getTranslation()).add(offset);
        display.setTransformation(new Transformation(
                translation,
                current.getLeftRotation(),
                current.getScale(),
                current.getRightRotation()
        ));
    }

    public void scale(BlockDisplay display, Vector3f scale) {
        Transformation current = display.getTransformation();
        display.setTransformation(new Transformation(
                current.getTranslation(),
                current.getLeftRotation(),
                scale,
                current.getRightRotation()
        ));
    }

    public void rotate(BlockDisplay display, AxisAngle4f rotation) {
        Transformation current = display.getTransformation();
        display.setTransformation(new Transformation(
                current.getTranslation(),
                rotation,
                current.getScale(),
                rotation
        ));
    }

    public void alignToDirection(BlockDisplay display, Vector direction) {
        Location loc = display.getLocation().clone();
        loc.setDirection(direction);
        display.teleport(loc);
    }

}
