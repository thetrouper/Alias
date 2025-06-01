package me.trouper.alias.utils;

import me.trouper.alias.server.Main;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PlayerUtils implements Main {

    public static final int DEFAULT_NO_DAMAGE_TICKS = 9;
    public static final int DEFAULT_MAX_NO_DAMAGE_TICKS = 10;
    public static final int NO_DAMAGE_TICKS = 1;
    public static final int MAX_NO_DAMAGE_TICKS = 2;

    private static float wrapYaw(float yaw) {
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;
        return yaw;
    }

    private static Vector getDirection(float yaw, float pitch) {
        double radYaw = Math.toRadians(yaw);
        double radPitch = Math.toRadians(pitch);
        double x = -Math.cos(radPitch) * Math.sin(radYaw);
        double y = -Math.sin(radPitch);
        double z = Math.cos(radPitch) * Math.cos(radYaw);
        return new Vector(x, y, z);
    }

    public static Player playerClosestAngle(Player player, double range) {
        Vector playerDirection = player.getEyeLocation().getDirection().normalize();
        Location eyeLoc = player.getEyeLocation();

        return player.getNearbyEntities(range, range, range).stream()
                .filter(entity -> entity instanceof Player && !entity.equals(player))
                .map(entity -> (Player) entity)
                .min((p1, p2) -> {
                    Vector dirToP1 = p1.getEyeLocation().toVector().subtract(eyeLoc.toVector()).normalize();
                    Vector dirToP2 = p2.getEyeLocation().toVector().subtract(eyeLoc.toVector()).normalize();

                    double angle1 = playerDirection.angle(dirToP1);
                    double angle2 = playerDirection.angle(dirToP2);

                    return Double.compare(angle1, angle2);
                })
                .orElse(null);
    }

    public static void dealTrueDamage(LivingEntity target, DamageSource source, double amount) {
        if (source.getDirectEntity() instanceof Player a && target instanceof Player t) return;
        
        target.damage(1, source);

        double newHealth = target.getHealth() - amount;
        if (newHealth <= 0) {
            target.setHealth(0);
        } else {
            target.setHealth(newHealth);
        }

        Entity attacker = source.getDirectEntity();
        if (attacker instanceof LivingEntity) {
            double dx = target.getX() - attacker.getX();
            double dz = target.getZ() - attacker.getZ();
            double magnitude = Math.sqrt(dx * dx + dz * dz);

            if (magnitude > 0) {
                double strength = 0.4;
                dx /= magnitude;
                dz /= magnitude;
                target.setVelocity(target.getVelocity().add(new Vector(dx * strength, 0.1, dz * strength)));
            }
        }
    }

}
