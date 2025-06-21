package me.trouper.alias.utils;

import org.bukkit.util.Vector;

public class VectorUtils {

    public static float[] toAngles(Vector vector) {
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();

        float yaw = (float) Math.toDegrees(Math.atan2(-x, z));

        double xzLength = Math.sqrt(x * x + z * z);

        float pitch = (float) Math.toDegrees(Math.atan2(-y, xzLength));

        return new float[] { yaw, pitch };
    }
}
