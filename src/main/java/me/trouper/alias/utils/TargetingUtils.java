package me.trouper.alias.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TargetingUtils {

    /**
     * Applies an action to all living entities within a specified cuboid area that match a given filter.
     *
     * @param center The center of the cuboid area.
     * @param xRadius The half-length of the cuboid along the X-axis.
     * @param yRadius The half-length of the cuboid along the Y-axis.
     * @param zRadius The half-length of the cuboid along the Z-axis.
     * @param filter A predicate to filter which living entities are affected.
     * @param action An action to perform on each targeted living entity.
     * @return true if any targets were found, false if it missed.
     */
    public static boolean areaAffect(Location center, double xRadius, double yRadius, double zRadius, Predicate<LivingEntity> filter, Consumer<LivingEntity> action) {
        World world = center.getWorld();
        if (world == null) {
            return false;
        }
        
        List<LivingEntity> targets = new ArrayList<>(world.getNearbyLivingEntities(center, xRadius, yRadius, zRadius).stream().filter(filter).toList());
        targets.forEach(action);
        return !targets.isEmpty();
    }

    /**
     * Applies an action to all living entities within a specified spherical area (approximated by a cube) that match a given filter.
     *
     * @param center The center of the area.
     * @param radius The radius of the spherical area.
     * @param filter A predicate to filter which living entities are affected.
     * @param action An action to perform on each targeted living entity.
     * @return true if any targets were found, false if it missed.
     */
    public static boolean areaAffect(Location center, double radius, Predicate<LivingEntity> filter, Consumer<LivingEntity> action) {

        World world = center.getWorld();
        if (world == null) {
            return false;
        }
        
        List<LivingEntity> targets = new ArrayList<>(world.getNearbyLivingEntities(center, radius).stream()
                .filter(livingEntity -> livingEntity.getLocation().distanceSquared(center) <= radius*radius)
                .filter(filter)
                .toList());
        targets.forEach(action);

        return !targets.isEmpty();
    }

    /**
     * Attempts to find a passable ground location based on the given initial location.
     * <p>
     * This method checks if the current block is non-passable and searches upward (up to 10 blocks)
     * until it finds a passable block. Then, it searches downward (up to 25 blocks) for the first
     * non-passable block beneath it, which would be considered solid ground.
     * <p>
     * If a valid ground location cannot be determined within the search bounds,
     * the original location is returned.
     *
     * @param initialLocation The starting location to begin the ground-finding process. Must not be null.
     * @return A new {@link Location} object representing the nearest solid ground position,
     * or the original location if no valid position is found or world is null.
     */
    public static Location findGroundLocation(Location initialLocation) {
        if (initialLocation == null) {
            return null;
        }
        World world = initialLocation.getWorld();
        if (world == null) return initialLocation;

        Location loc = initialLocation.clone();

        int attempts = 0;
        while (!world.getBlockAt(loc).isPassable() && attempts < 10) {
            loc.add(0, 1, 0);
            attempts++;
            if (loc.getBlockY() >= world.getMaxHeight()) {
                return initialLocation;
            }
        }
        if (!world.getBlockAt(loc).isPassable()) return initialLocation;

        attempts = 0;
        while (world.getBlockAt(loc).isPassable() && attempts < 25) {
            Location belowLoc = loc.clone().subtract(0, 1, 0);
            if (belowLoc.getBlockY() <= world.getMinHeight()) {
                return world.getBlockAt(belowLoc).isPassable() ? initialLocation : loc;
            }
            if (!world.getBlockAt(belowLoc).isPassable()) {
                return loc;
            }
            loc.subtract(0, 1, 0);
            attempts++;
        }
        return initialLocation;
    }

    /**
     * Finds the closest living entity to a central location within a maximum distance.
     *
     * @param center The central location from which to search. Must not be null.
     * @param maxDistance The maximum distance (radius of a sphere) to search for entities.
     * @return An {@link Optional} containing the closest {@link LivingEntity}, or an empty Optional if no entity is found or world is null.
     */
    public static Optional<LivingEntity> getClosestLivingEntity(Location center, double maxDistance) {
        return getClosestLivingEntity(center, maxDistance, entity -> true); 
    }

    /**
     * Finds the closest living entity to a central location within a maximum distance, matching a given filter.
     *
     * @param center The central location from which to search. Must not be null.
     * @param maxDistance The maximum distance (radius of a sphere) to search for entities.
     * @param filter A predicate to apply additional filtering criteria to the entities. Must not be null.
     * @return An {@link Optional} containing the closest {@link LivingEntity} matching the criteria,
     * or an empty Optional if no such entity is found or world is null.
     */
    public static Optional<LivingEntity> getClosestLivingEntity(Location center, double maxDistance, Predicate<LivingEntity> filter) {
        if (center == null || center.getWorld() == null || filter == null) {
            return Optional.empty();
        }
        World world = center.getWorld();
        double maxDistanceSquared = maxDistance * maxDistance;

        return world.getNearbyLivingEntities(center, maxDistance, maxDistance, maxDistance, filter).stream()
                .filter(entity -> entity.getLocation().distanceSquared(center) <= maxDistanceSquared) 
                .min(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(center)));
    }

    /**
     * Finds the closest player to a central location within a maximum distance.
     *
     * @param center The central location from which to search. Must not be null.
     * @param maxDistance The maximum distance (radius of a sphere) to search for players.
     * @return An {@link Optional} containing the closest {@link Player}, or an empty Optional if no player is found or world is null.
     */
    public static Optional<Player> getClosestPlayer(Location center, double maxDistance) {
        return getClosestPlayer(center, maxDistance, player -> true);
    }

    /**
     * Finds the closest player to a central location within a maximum distance, matching a given filter.
     *
     * @param center The central location from which to search. Must not be null.
     * @param maxDistance The maximum distance (radius of a sphere) to search for players.
     * @param filter A predicate to apply additional filtering criteria to the players. Must not be null.
     * @return An {@link Optional} containing the closest {@link Player} matching the criteria,
     * or an empty Optional if no such player is found or world is null.
     */
    public static Optional<Player> getClosestPlayer(Location center, double maxDistance, Predicate<Player> filter) {
        if (center == null || center.getWorld() == null || filter == null) {
            return Optional.empty();
        }
        World world = center.getWorld();
        double maxDistanceSquared = maxDistance * maxDistance;

        List<Player> nearbyPlayers = world.getPlayers().stream()
                .filter(player -> player.getWorld().equals(world))
                .filter(player -> player.getLocation().distanceSquared(center) <= maxDistanceSquared)
                .filter(filter)
                .collect(Collectors.toList());

        return nearbyPlayers.stream()
                .min(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(center)));
    }

    /**
     * Finds the living entity with the lowest health within a given radius of a central location.
     *
     * @param center The central location from which to search. Must not be null.
     * @param radius The radius (sphere) to search for entities.
     * @return An {@link Optional} containing the {@link LivingEntity} with the lowest health,
     * or an empty Optional if no entity is found or world is null.
     */
    public static Optional<LivingEntity> getLowestHealthLivingEntity(Location center, double radius) {
        return getLowestHealthLivingEntity(center, radius, entity -> true);
    }

    /**
     * Finds the living entity with the lowest health within a given radius of a central location, matching a given filter.
     *
     * @param center The central location from which to search. Must not be null.
     * @param radius The radius (sphere) to search for entities.
     * @param filter A predicate to apply additional filtering criteria. Must not be null.
     * @return An {@link Optional} containing the {@link LivingEntity} with the lowest health matching the criteria,
     * or an empty Optional if no such entity is found or world is null.
     */
    public static Optional<LivingEntity> getLowestHealthLivingEntity(Location center, double radius, Predicate<LivingEntity> filter) {
        if (center == null || center.getWorld() == null || filter == null) {
            return Optional.empty();
        }
        World world = center.getWorld();
        double radiusSquared = radius * radius;

        return world.getNearbyLivingEntities(center, radius, radius, radius, filter).stream()
                .filter(entity -> entity.getLocation().distanceSquared(center) <= radiusSquared)
                .min(Comparator.comparingDouble(LivingEntity::getHealth));
    }

    /**
     * Finds the living entity whose eye location is closest (by angle) to a given direction vector from an origin.
     * This method is useful for simulating line-of-sight or aim-based targeting.
     *
     * @param originEyeLocation The starting location (e.g., an entity's eye location). Must not be null.
     * @param direction The normalized direction vector of the search. Must not be null.
     * @param maxDistance The maximum distance to search for entities.
     * @param maxAngleRadians The maximum allowed angle (in radians) between the direction vector and the vector to the target's eye location.
     * A smaller angle means the target is more directly in the line of sight.
     * @return An {@link Optional} containing the {@link LivingEntity} closest to the aim vector,
     * or an empty Optional if no suitable entity is found or world is null.
     */
    public static Optional<LivingEntity> getLivingEntityClosestToVector(Location originEyeLocation, Vector direction, double maxDistance, double maxAngleRadians) {
        return getLivingEntityClosestToVector(originEyeLocation, direction, maxDistance, maxAngleRadians, entity -> true);
    }

    /**
     * Finds the living entity whose eye location is closest (by angle) to a given direction vector from an origin, matching a filter.
     * This method is useful for simulating line-of-sight or aim-based targeting.
     *
     * @param originEyeLocation The starting location (e.g., an entity's eye location). Must not be null.
     * @param direction The normalized direction vector of the search. Must not be null. Its magnitude does not matter as it will be normalized.
     * @param maxDistance The maximum distance to search for entities.
     * @param maxAngleRadians The maximum allowed angle (in radians) between the direction vector and the vector to the target's eye location.
     * A smaller angle means the target is more directly in the line of sight.
     * @param filter A predicate to apply additional filtering criteria. Must not be null.
     * @return An {@link Optional} containing the {@link LivingEntity} closest to the aim vector and matching the filter,
     * or an empty Optional if no suitable entity is found or world is null.
     */
    public static Optional<LivingEntity> getLivingEntityClosestToVector(Location originEyeLocation, Vector direction, double maxDistance, double maxAngleRadians, Predicate<LivingEntity> filter) {
        if (originEyeLocation == null || originEyeLocation.getWorld() == null || direction == null || filter == null) {
            return Optional.empty();
        }
        World world = originEyeLocation.getWorld();
        Vector normalizedDirection = direction.clone().normalize();

        List<LivingEntity> candidates = world.getNearbyLivingEntities(originEyeLocation, maxDistance, maxDistance, maxDistance, entity -> {
            if (originEyeLocation.equals(entity.getEyeLocation())) {
                return false;
            }
            return filter.test(entity);
        }).stream().toList();

        LivingEntity bestTarget = null;
        double smallestAngle = maxAngleRadians + 1.0; 

        for (LivingEntity entity : candidates) {
            if (entity.getEyeLocation().distanceSquared(originEyeLocation) > maxDistance * maxDistance) {
                continue;
            }

            Vector vectorToTarget = entity.getEyeLocation().toVector().subtract(originEyeLocation.toVector());
            if (vectorToTarget.lengthSquared() == 0) {
                continue;
            }
            vectorToTarget.normalize();

            double angle = normalizedDirection.angle(vectorToTarget);

            if (angle <= maxAngleRadians && angle < smallestAngle) {
                smallestAngle = angle;
                bestTarget = entity;
            }
        }
        return Optional.ofNullable(bestTarget);
    }
}
