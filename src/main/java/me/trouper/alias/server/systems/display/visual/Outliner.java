package me.trouper.alias.server.systems.display.visual;

import me.trouper.alias.server.systems.display.tracing.BlockDisplayRaytracer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class Outliner {

    private final BlockDisplayRaytracer rt;

    public Outliner(BlockDisplayRaytracer raytracer) {
        this.rt = raytracer;
    }

    public void highlightCollisions(Block block, Color color, long stayTime) {
        if (block == null || block.isEmpty() || !block.isCollidable())
            return;

        VoxelShape shape = block.getCollisionShape();
        World world = block.getWorld();
        Vector offset = block.getLocation().toVector();

        for (BoundingBox box : shape.getBoundingBoxes()) {
            highlight(box, offset, world, color, stayTime);
        }
    }

    public List<BlockDisplay> outline(Material display, Location location, long stayTime) {
        return outline(display, location, 0.05, stayTime);
    }

    public List<BlockDisplay> outline(Material display, Location location, double thickness, long stayTime) {
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

        a.add(rt.trace(display, a1, a2, thickness, stayTime));
        a.add(rt.trace(display, a2, a3, thickness, stayTime));
        a.add(rt.trace(display, a3, a4, thickness, stayTime));
        a.add(rt.trace(display, a4, a1, thickness, stayTime));

        a.add(rt.trace(display, b1, b2, thickness, stayTime));
        a.add(rt.trace(display, b2, b3, thickness, stayTime));
        a.add(rt.trace(display, b3, b4, thickness, stayTime));
        a.add(rt.trace(display, b4, b1, thickness, stayTime));

        a.add(rt.trace(display, a1, b1, thickness, stayTime));
        a.add(rt.trace(display, a2, b2, thickness, stayTime));
        a.add(rt.trace(display, a3, b3, thickness, stayTime));
        a.add(rt.trace(display, a4, b4, thickness, stayTime));

        return a;
    }

    public void highlight(BoundingBox box, Vector offset, World world, Color color, long stayTime) {
        double x1 = box.getMinX() + offset.getX();
        double y1 = box.getMinY() + offset.getY();
        double z1 = box.getMinZ() + offset.getZ();
        double x2 = box.getMaxX() + offset.getX();
        double y2 = box.getMaxY() + offset.getY();
        double z2 = box.getMaxZ() + offset.getZ();

        rt.traceGlowing(world, x1, y1, z1, x2, y1, z1, color, stayTime);
        rt.traceGlowing(world, x2, y1, z1, x2, y1, z2, color, stayTime);
        rt.traceGlowing(world, x2, y1, z2, x1, y1, z2, color, stayTime);
        rt.traceGlowing(world, x1, y1, z2, x1, y1, z1, color, stayTime);

        rt.traceGlowing(world, x1, y2, z1, x2, y2, z1, color, stayTime);
        rt.traceGlowing(world, x2, y2, z1, x2, y2, z2, color, stayTime);
        rt.traceGlowing(world, x2, y2, z2, x1, y2, z2, color, stayTime);
        rt.traceGlowing(world, x1, y2, z2, x1, y2, z1, color, stayTime);

        rt.traceGlowing(world, x1, y1, z1, x1, y2, z1, color, stayTime);
        rt.traceGlowing(world, x2, y1, z1, x2, y2, z1, color, stayTime);
        rt.traceGlowing(world, x2, y1, z2, x2, y2, z2, color, stayTime);
        rt.traceGlowing(world, x1, y1, z2, x1, y2, z2, color, stayTime);
    }

    public void outline(Material display, Location location, long stayTime, List<Player> viewers) {
        outline(display, location, 0.05, stayTime, viewers);
    }

    public void outline(Material display, Location corner1, Location corner2, double thickness, long stayTime, List<Player> viewers) {
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

        rt.trace(display, a1, a2, thickness, stayTime, viewers);
        rt.trace(display, a2, a3, thickness, stayTime, viewers);
        rt.trace(display, a3, a4, thickness, stayTime, viewers);
        rt.trace(display, a4, a1, thickness, stayTime, viewers);

        rt.trace(display, b1, b2, thickness, stayTime, viewers);
        rt.trace(display, b2, b3, thickness, stayTime, viewers);
        rt.trace(display, b3, b4, thickness, stayTime, viewers);
        rt.trace(display, b4, b1, thickness, stayTime, viewers);

        rt.trace(display, a1, b1, thickness, stayTime, viewers);
        rt.trace(display, a2, b2, thickness, stayTime, viewers);
        rt.trace(display, a3, b3, thickness, stayTime, viewers);
        rt.trace(display, a4, b4, thickness, stayTime, viewers);
    }


    public void outline(Material display, Location location, double thickness, long stayTime, List<Player> viewers) {
        Location og = location.getBlock().getLocation();

        Location a1 = og.clone().add(0, 0, 0);
        Location a2 = og.clone().add(1, 0, 0);
        Location a3 = og.clone().add(1, 0, 1);
        Location a4 = og.clone().add(0, 0, 1);

        Location b1 = og.clone().add(0, 1, 0);
        Location b2 = og.clone().add(1, 1, 0);
        Location b3 = og.clone().add(1, 1, 1);
        Location b4 = og.clone().add(0, 1, 1);

        rt.trace(display, a1, a2, thickness, stayTime, viewers);
        rt.trace(display, a2, a3, thickness, stayTime, viewers);
        rt.trace(display, a3, a4, thickness, stayTime, viewers);
        rt.trace(display, a4, a1, thickness, stayTime, viewers);

        rt.trace(display, b1, b2, thickness, stayTime, viewers);
        rt.trace(display, b2, b3, thickness, stayTime, viewers);
        rt.trace(display, b3, b4, thickness, stayTime, viewers);
        rt.trace(display, b4, b1, thickness, stayTime, viewers);

        rt.trace(display, a1, b1, thickness, stayTime, viewers);
        rt.trace(display, a2, b2, thickness, stayTime, viewers);
        rt.trace(display, a3, b3, thickness, stayTime, viewers);
        rt.trace(display, a4, b4, thickness, stayTime, viewers);
    }
}
