package me.trouper.alias.utils.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Cooldown<T> {

    private final Map<T, Long> cooldowns = new HashMap<>();

    public void setCooldown(T holder, long duration) {
        cooldowns.put(holder, System.currentTimeMillis() + duration);
    }

    public boolean isOnCooldown(T holder) {
        return getRemaining(holder) > 0;
    }

    public long getRemaining(T holder) {
        return Math.max(0, cooldowns.getOrDefault(holder, 0L) - System.currentTimeMillis());
    }

    public void clearCooldown(T holder) {
        cooldowns.remove(holder);
    }

    public String formatShort(T holder) {
        return formatShort(getRemaining(holder));
    }

    public String formatLong(T holder) {
        return formatLong(getRemaining(holder));
    }

    public String formatColon(T holder) {
        return formatColon(getRemaining(holder));
    }

    public static String formatShort(long ms) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms);
        long minutes = seconds / 60;
        seconds %= 60;

        long hours = minutes / 60;
        minutes %= 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public static String formatLong(long ms) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms);
        long minutes = seconds / 60;
        seconds %= 60;

        long hours = minutes / 60;
        minutes %= 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append(" hour").append(hours == 1 ? "" : "s").append(", ");
        if (minutes > 0) sb.append(minutes).append(" minute").append(minutes == 1 ? "" : "s").append(", ");
        sb.append(seconds).append(" second").append(seconds == 1 ? "" : "s");

        return sb.toString();
    }

    public static String formatColon(long ms) {
        long totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms);
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }
}
