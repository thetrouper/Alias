package me.trouper.alias.server.systems.display.tracing;

import me.trouper.alias.AliasContext;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;

import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class CustomRaytracer {

    private final AliasContext context;

    public CustomRaytracer(AliasContext context) {
        this.context = context;
    }

    public static final Predicate<Point> HIT_BLOCK = point -> {
        Block b = point.getBlock();
        Location l = point.getLoc();

        if (b == null || b.isEmpty() || !b.isCollidable())
            return false;

        Vector vec = l.toVector().subtract(b.getLocation().toVector());
        VoxelShape shape = b.getCollisionShape();

        for (BoundingBox box : shape.getBoundingBoxes())
            if (box.contains(vec))
                return true;
        return false;
    };

    public static final Predicate<Point> HIT_ENTITY = point -> {
        return !point.getNearbyEntities(null, 5, true, 0.1, e -> e instanceof LivingEntity le && !le.isDead()).isEmpty();
    };

    public static final Predicate<Point> HIT_BLOCK_OR_ENTITY = point -> {
        return HIT_BLOCK.test(point) || HIT_ENTITY.test(point);
    };

    public static final Predicate<Point> HIT_BLOCK_AND_ENTITY = point -> {
        return HIT_BLOCK.test(point) && HIT_ENTITY.test(point);
    };

    public static Predicate<Point> hitEntityExclude(Entity exclude) {
        return point -> !point.getNearbyEntities(exclude, 5, true, 0.1, e -> e instanceof LivingEntity le && !le.isDead()).isEmpty();
    }

    public static Predicate<Point> hitAnythingExclude(Entity exclude) {
        return point -> HIT_BLOCK.test(point) || !point.getNearbyEntities(exclude, 5, true, 0.1, e -> e instanceof LivingEntity le && !le.isDead()).isEmpty();
    }

    public static Predicate<Point> hitEverythingExclude(Entity exclude) {
        return point -> HIT_BLOCK.test(point) && !point.getNearbyEntities(exclude, 5, true, 0.1, e -> e instanceof LivingEntity le && !le.isDead()).isEmpty();
    }
    
    public static Predicate<Point> hitEntityIf(Predicate<Entity> condition) {
        return point -> !point.getNearbyEntities(null, 5, true, 0.1, e -> e instanceof LivingEntity le && !le.isDead() && condition.test(e)).isEmpty();
    }
    
    public static Predicate<Point> hitBlockIf(Predicate<Block> condition) {
        return point -> HIT_BLOCK.test(point) && condition.test(point.getBlock());
    }
    
    public static Predicate<Point> hitAnythingIf(Predicate<Entity> condition) {
        return point -> HIT_BLOCK.test(point) || !point.getNearbyEntities(null, 5, true, 0.1, e -> e instanceof LivingEntity le && !le.isDead() && condition.test(e)).isEmpty();
    }
    
    public static Predicate<Point> hitEverythingIf(Predicate<Entity> condition) {
        return point -> HIT_BLOCK.test(point) && !point.getNearbyEntities(null, 5, true, 0.1, e -> e instanceof LivingEntity le && !le.isDead() && condition.test(e)).isEmpty();
    }

    public Point trace(Location start, Location end, Predicate<Point> hitCondition) {
        return trace(start, end, 0.5, hitCondition);
    }

    public Point trace(Location start, Location end, double interval, Predicate<Point> hitCondition) {
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double distance = end.distance(start);
        return trace(start, direction, distance, interval, hitCondition);
    }

    public Point trace(Location start, Vector direction, double distance, Predicate<Point> hitCondition) {
        Vector normal = direction.clone().normalize();
        return trace(start, normal, distance, 0.5, hitCondition);
    }

    public Point trace(Location start, Vector direction, double distance, double interval, Predicate<Point> hitCondition) {
        if (interval < 0) throw new IllegalArgumentException("interval cannot be zero!");
        if (distance < 0) throw new IllegalArgumentException("distance cannot be zero!");

        for (double i = 0.0; i < distance; i += interval) {
            Point point = blocksInFrontOf(start, direction, i, false);
            if (hitCondition.test(point)) {
                return point;
            }
        }
        return blocksInFrontOf(start, direction, distance, true);
    }


    public BukkitTask traceDelayed(Location start, Vector direction, double distance, double interval, long tickDelay, int pointsPerTick, Predicate<Point> hitCondition) {

        if (interval <= 0) throw new IllegalArgumentException("interval cannot be zero or negative!");
        if (distance <= 0) throw new IllegalArgumentException("distance cannot be zero or negative!");
        if (tickDelay < 0) throw new IllegalArgumentException("tickDelay cannot be negative!");

        Vector normalizedDir = direction.clone().normalize();

        return new BukkitRunnable() {
            private double currentDistance = 0.0;
            private boolean hit = false;

            @Override
            public void run() {
                if (hit || currentDistance > distance) {
                    if (!hit) {
                        Point finalPoint = blocksInFrontOf(start, normalizedDir, distance, true);
                        hitCondition.test(finalPoint);
                    }
                    this.cancel();
                    return;
                }

                for (int i = 0; i < pointsPerTick && currentDistance <= distance; i++) {
                    Point point = blocksInFrontOf(start, normalizedDir, currentDistance, false);
                    if (hitCondition.test(point)) {
                        hit = true;
                        break;
                    }
                    currentDistance += interval;
                }
            }
        }.runTaskTimer(context.getPlugin(), 0, tickDelay);
    }

    public BukkitTask traceDelayed(Location start, Location end, double interval, long tickDelay, int pointsPerTick, Predicate<Point> hitCondition) {
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double distance = start.distance(end);
        return traceDelayed(start, direction, distance, interval, tickDelay,pointsPerTick, hitCondition);
    }

    public BukkitTask traceDelayed(Plugin plugin,
                                          Location start,
                                          Location end,
                                          long tickDelay,
                                          Predicate<Point> hitCondition) {
        return traceDelayed(start, end,0.5, tickDelay, 1, hitCondition);
    }

    public BukkitTask traceDelayed(Plugin plugin,
                                          Location start,
                                          Vector direction,
                                          double distance,
                                          long tickDelay,
                                          Predicate<Point> hitCondition) {
        return traceDelayed(start, direction, distance, 0.5, tickDelay,1, hitCondition);
    }

    public Point traceWithReflection(Location start, Vector direction, double distance, double interval,
                                            int maxReflections, Predicate<Point> hitCondition,
                                            BiPredicate<Point, Block> blockReflectCondition,
                                            BiPredicate<Point, Entity> entityReflectCondition) {
        
        
        if (interval <= 0) throw new IllegalArgumentException("interval cannot be zero or negative!");
        if (distance <= 0) throw new IllegalArgumentException("distance cannot be zero or negative!");

        Vector normalizedDir = direction.clone().normalize();
        Location currentLocation = start.clone();
        Vector currentDirection = normalizedDir.clone();
        double remainingDistance = distance;
        int reflections = 0;

        while (remainingDistance > 0 && reflections <= maxReflections) {
            for (double i = 0.0; i < remainingDistance; i += interval) {
                Point point = blocksInFrontOf(currentLocation, currentDirection, i, false);

                if (hitCondition.test(point)) {
                    return point; 
                }

                boolean shouldReflect = false;
                Vector newDirection = null;

                if (HIT_BLOCK.test(point) && point.getBlock() != null) {
                    Block hitBlock = point.getBlock();
                    if (blockReflectCondition.test(point, hitBlock)) {
                        Point previousPoint = blocksInFrontOf(currentLocation, currentDirection, Math.max(0, i - interval), false);

                        BlockFace hitFace = traceBlockFace(previousPoint.getLoc(), currentDirection, interval * 2);

                        if (hitFace != null) {
                            Vector faceNormal = getFaceNormal(hitFace);
                            newDirection = calculateReflection(currentDirection, faceNormal);
                            shouldReflect = true;
                        }
                    }
                }

                List<Entity> nearbyEntities = point.getNearbyEntities(null, 5, true, 0.1, e -> e instanceof LivingEntity le && !le.isDead());
                if (!nearbyEntities.isEmpty()) {
                    for (Entity entity : nearbyEntities) {
                        if (entityReflectCondition.test(point, entity)) {
                            Point previousPoint = blocksInFrontOf(currentLocation, currentDirection, Math.max(0, i - interval), false);

                            newDirection = glanceReflect(currentDirection);
                            shouldReflect = true;
                            break;
                        }
                    }
                }

                if (shouldReflect) {
                    double backStep = Math.max(0, i - interval);
                    currentLocation = blocksInFrontOf(currentLocation, currentDirection, backStep, false).getLoc();

                    currentDirection = newDirection;

                    remainingDistance -= backStep;

                    reflections++;

                    currentLocation = currentLocation.add(currentDirection.clone().multiply(interval * 0.1));
                    remainingDistance -= interval * 0.1;

                    break;
                }

                if (i + interval >= remainingDistance) {
                    return blocksInFrontOf(currentLocation, currentDirection, remainingDistance, true);
                }
            }

            if (reflections > maxReflections) {
                return blocksInFrontOf(currentLocation, currentDirection, remainingDistance, true);
            }
        }

        return blocksInFrontOf(start, normalizedDir, distance, true);
    }
    
    private Vector glanceReflect(Vector incident) {
        return offsetVector(incident,4).multiply(-1);
    }

    private BlockFace traceBlockFace(Location startLocation, Vector direction, double maxDistance) {
        Predicate<Block> blockPredicate = block -> true;
        Predicate<Entity> entityPredicate = entity -> false;

        RayTraceResult result = startLocation.getWorld().rayTrace(startLocation, direction, maxDistance, FluidCollisionMode.NEVER,true,0.1,entityPredicate, blockPredicate);

        if (result != null && result.getHitBlock() != null && result.getHitBlockFace() != null) {
            return result.getHitBlockFace();
        }

        return null;
    }

    private Vector calculateReflection(Vector incident, Vector normal) {
        // r = i - 2(i dot n)n
        double dot = incident.dot(normal);
        Vector reflection = incident.clone().subtract(normal.clone().multiply(2 * dot));

        return reflection.normalize();
    }
    
    private Vector getFaceNormal(BlockFace face) {
        return switch (face) {
            case DOWN -> new Vector(0, -1, 0);
            case NORTH -> new Vector(0, 0, -1);
            case SOUTH -> new Vector(0, 0, 1);
            case EAST -> new Vector(1, 0, 0);
            case WEST -> new Vector(-1, 0, 0);
            default -> new Vector(0, 1, 0);
        };
    }

    public Point blocksInFrontOf(Location loc, Vector dir, double blocks, boolean missed) {
        Vector normal = dir.clone().normalize();
        return new Point(loc.clone().add(normal.getX() * blocks, normal.getY() * blocks, normal.getZ() * blocks), blocks, missed);
    }

    private Vector offsetVector(Vector original, double angleDegrees) {
        Random random = new Random();
        original = original.clone().normalize();

        double yaw = Math.toDegrees(Math.atan2(-original.getX(), original.getZ()));
        double pitch = Math.toDegrees(Math.asin(-original.getY()));

        double yawOffset = (random.nextDouble() * 2 - 1) * angleDegrees;
        double pitchOffset = (random.nextDouble() * 2 - 1) * angleDegrees;

        yaw += yawOffset;
        pitch += pitchOffset;

        pitch = Math.max(-90, Math.min(90, pitch));

        double pitchRad = Math.toRadians(pitch);
        double yawRad = Math.toRadians(yaw);

        double x = -Math.sin(yawRad) * Math.cos(pitchRad);
        double y = -Math.sin(pitchRad);
        double z = Math.cos(yawRad) * Math.cos(pitchRad);

        return new Vector(x, y, z);
    }
}
