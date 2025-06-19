package me.trouper.alias.server.systems.visual;

import me.trouper.alias.server.Main;
import me.trouper.alias.utils.misc.Randomizer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class DisplayUtils implements Main {

    // This will 100% get Javadoc in the future when I try to use it. Everything in here is so convoluted.

    public static final Function<Particle, Consumer<Location>> PARTICLE_FACTORY = particle -> l -> l.getWorld().spawnParticle(particle, l, 1, 0, 0, 0, 0);

    public static final BiFunction<Color, Float, Consumer<Location>> DUST_PARTICLE_FACTORY = (color, thickness) -> {
        Particle.DustOptions dust = new Particle.DustOptions(color, thickness);
        return l -> l.getWorld().spawnParticle(Particle.DUST, l, 1, 0, 0, 0, 0, dust);
    };

    public static final Function<Boolean, Consumer<Location>> FLAME_PARTICLE_FACTORY = soul -> {
        Particle flame = soul ? Particle.SOUL_FIRE_FLAME : Particle.FLAME;
        return l -> l.getWorld().spawnParticle(flame, l, 1, 0, 0, 0, 0);
    };

    public static void ring(Location loc, double radius, Color color, float thickness) {
        ring(loc, radius, DUST_PARTICLE_FACTORY.apply(color, thickness));
    }

    public static void sphere(Location center, double radius, double pointDistance, Consumer<Location> action) {
        double dPhi = pointDistance / radius;

        for (double phi = 0.0; phi <= Math.PI; phi += dPhi) {
            double yOffset = radius * Math.cos(phi);
            double ringRadius = radius * Math.sin(phi);

            if (ringRadius < 1e-6) {
                Location loc = center.clone().add(0, yOffset, 0);
                action.accept(loc);
            } else {
                double dTheta = pointDistance / ringRadius;

                for (double theta = 0.0; theta < 2 * Math.PI; theta += dTheta) {
                    double xOffset = ringRadius * Math.cos(theta);
                    double zOffset = ringRadius * Math.sin(theta);

                    Location loc = center.clone().add(xOffset, yOffset, zOffset);
                    action.accept(loc);
                }
            }
        }
    }
    
    public static void sphereWave(Location center, double maxRadius, double radialStep, double maxDistanceBetweenPoints, Consumer<Location> action) {
        AtomicReference<Double> currentRadius = new AtomicReference<>(radialStep);

        Bukkit.getScheduler().runTaskTimer(main.getPlugin(), (task) -> {
            double r = currentRadius.get();
            if (r > maxRadius) {
                task.cancel();
                return;
            }

            sphere(center, r, maxDistanceBetweenPoints, action);
            currentRadius.set(r + radialStep);
        }, 0L, 1L);
    }

    public static void ring(Location loc, double radius, Consumer<Location> action) {
        for (int theta = 0; theta < 360; theta += 10) {
            double x = Math.cos(Math.toRadians(theta)) * radius;
            double z = Math.sin(Math.toRadians(theta)) * radius;
            Location newLoc = loc.clone().add(x, 0, z);
            action.accept(newLoc);
        }
    }

    public static void ring(Location loc, double radius, double maxDistanceBetweenPoints, Consumer<Location> action) {
        arc(loc, radius, 0, 360, maxDistanceBetweenPoints, action);
    }

    public static void wave(Location loc, double radius, Color color, float thickness, double gap) {
        wave(loc, radius, DUST_PARTICLE_FACTORY.apply(color, thickness), gap);
    }

    public static void wave(Location loc, double radius, Consumer<Location> action, double gap) {
        AtomicReference<Double> i = new AtomicReference<>(gap);
        Bukkit.getScheduler().runTaskTimer(main.getPlugin(), (task) -> {
            if (i.get() >= radius) {
                task.cancel();
                return;
            }
            ring(loc, i.get(), action);
            i.set(i.get() + gap);
        }, 0, 1);
    }

    public static void wave(Location loc, double radius, double radialGap, double maxDistanceBetweenPoints, Consumer<Location> action) {
        AtomicReference<Double> r = new AtomicReference<>(radialGap);
        Bukkit.getScheduler().runTaskTimer(main.getPlugin(), (task) -> {
            if (r.get() > radius) {
                task.cancel();
                return;
            }
            ring(loc, r.get(), maxDistanceBetweenPoints, action);
            r.set(r.get() + radialGap);
        }, 0, 1);
    }

    public static void disc(Location loc, double radius, Consumer<Location> action, double gap) {
        for (double i = gap; i < radius; i += gap) {
            ring(loc, i, action);
        }
    }

    public static void disc(Location loc, double radius, double radialGap, double maxDistanceBetweenPoints, Consumer<Location> action) {
        for (double r = radialGap; r <= radius; r += radialGap) {
            ring(loc, r, maxDistanceBetweenPoints, action);
        }
    }

    public static void helix(Location loc, double radius, Consumer<Location> action, double gap, int height) {
        int theta = 0;
        for (double y = 0; y <= height; y += gap) {
            double x = Math.cos(Math.toRadians(theta)) * radius;
            double z = Math.sin(Math.toRadians(theta)) * radius;

            Location newLoc = loc.clone().add(x, y, z);
            action.accept(newLoc);
            theta += 10;
        }
    }

    public static void vortex(Location loc, double radius, Consumer<Location> action, double gapH, double gapV, int height) {
        double r = radius;
        int theta = 0;
        for (double y = 0; y <= height; y += gapV) {
            double x = Math.cos(Math.toRadians(theta)) * r;
            double z = Math.sin(Math.toRadians(theta)) * r;

            Location newLoc = loc.clone().add(x, y, z);
            action.accept(newLoc);
            r += gapH;
            theta += 10;
        }
    }

    public static void beam(Location loc, Consumer<Location> action, double gap, int height) {
        for (double y = 0; y <= height; y += gap) {
            Location newLoc = loc.clone().add(0, y, 0);
            action.accept(newLoc);
        }
    }

    public static void arc(Location loc, double radius, int angleFrom, int angleTo, Consumer<Location> action) {
        for (int theta = angleFrom; theta < angleTo; theta += 10) {
            double x = Math.cos(Math.toRadians(theta)) * radius;
            double z = Math.sin(Math.toRadians(theta)) * radius;
            Location newLoc = loc.clone().add(x, 0, z);
            action.accept(newLoc);
        }
    }

    public static void arc(Location loc, double radius, int angleFrom, int angleTo, double maxDistanceBetweenPoints, Consumer<Location> action) {
        int angleSpan = angleTo - angleFrom;
        if (angleSpan <= 0) return;

        int points = Math.max(2, (int) ((2 * Math.PI * radius * (angleSpan / 360.0)) / maxDistanceBetweenPoints));
        double angleStep = (double) angleSpan / points;

        for (int i = 0; i <= points; i++) {
            double theta = angleFrom + (i * angleStep);
            double x = Math.cos(Math.toRadians(theta)) * radius;
            double z = Math.sin(Math.toRadians(theta)) * radius;
            Location point = loc.clone().add(x, 0, z);
            action.accept(point);
        }
    }


    public static void fan(Location loc, double radius, int angleFrom, int angleTo, Consumer<Location> action, double gap) {
        for (double i = gap; i < radius; i += gap) {
            arc(loc, i, angleFrom, angleTo, action);
        }
    }

    public static void fan(Location loc, double radius, int angleFrom, int angleTo, double maxDistanceBetweenPoints, Consumer<Location> action, double radialGap) {
        for (double r = radialGap; r < radius; r += radialGap) {
            arc(loc, r, angleFrom, angleTo, maxDistanceBetweenPoints, action);
        }
    }


    public static void fanWave(Location loc, double radius, int sections, Consumer<Location> action, double gap) {
        double arcLength = 360.0 / sections;
        AtomicReference<Double> i = new AtomicReference<>(0.0);
        Bukkit.getScheduler().runTaskTimer(main.getPlugin(), (task) -> {
            if (i.get() >= 360) {
                task.cancel();
                return;
            }
            double start = i.get();
            fan(loc, radius, (int)start, (int)(start + arcLength), action, gap);
            i.set(i.get() + arcLength);
        }, 0, 5);
    }

    public static void fanWaveRandom(Location loc, double radius, int sections, Consumer<Location> action, double gap) {
        double arcLength = 360.0 / sections;
        List<Double> ints = new ArrayList<>();
        for (double start = 0; start < 360; start += arcLength) {
            ints.add(start);
        }

        AtomicInteger i = new AtomicInteger(0);
        Randomizer random = new Randomizer();
        Bukkit.getScheduler().runTaskTimer(main.getPlugin(), (task) -> {
            if (i.get() >= sections) {
                task.cancel();
                return;
            }
            double start = random.getRandomElement(ints);
            ints.remove(start);
            fan(loc, radius, (int)start, (int)(start + arcLength), action, gap);
            i.getAndIncrement();
        }, 0, 5);
    }

    public static void waveFan(Location loc, double radius, int angleFrom, int angleTo, double maxDistanceBetweenPoints, Consumer<Location> action, double radialGap) {
        AtomicReference<Double> r = new AtomicReference<>(radialGap);
        Bukkit.getScheduler().runTaskTimer(main.getPlugin(), (task) -> {
            if (r.get() >= radius) {
                task.cancel();
                return;
            }
            arc(loc, r.get(), angleFrom, angleTo, maxDistanceBetweenPoints, action);
            r.set(r.get() + radialGap);
        }, 0, 1);
    }

    public static void waveFan(Location loc, double radius, Vector direction, int angle, double maxDistanceBetweenPoints, Consumer<Location> action, double radialGap) {
        double baseAngle = Math.toDegrees(Math.atan2(direction.getZ(), direction.getX()));
        int angleFrom = (int) (baseAngle - angle / 2.0);
        int angleTo = (int) (baseAngle + angle / 2.0);
        waveFan(loc, radius, angleFrom, angleTo, maxDistanceBetweenPoints, action, radialGap);
    }
}