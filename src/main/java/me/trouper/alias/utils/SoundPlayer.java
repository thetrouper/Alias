package me.trouper.alias.utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Collection;

public class SoundPlayer {
    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final SoundCategory category;

    public SoundPlayer(Sound sound) {
        this(sound, 1.0f, 1.0f);
    }

    public SoundPlayer(Sound sound, float volume, float pitch) {
        this(sound, volume, pitch, SoundCategory.MASTER);
    }

    public SoundPlayer(Sound sound, float volume, float pitch, SoundCategory category) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.category = category;
    }

    public void playTo(Player player) {
        player.playSound(player.getLocation(), sound, category, volume, pitch);
    }

    public void playTo(Collection<? extends Player> players) {
        for (Player player : players) {
            playTo(player);
        }
    }

    public void playAt(Location location, double radius) {
        Collection<? extends Player> nearby = location.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distanceSquared(location) <= radius * radius)
                .toList();

        for (Player player : nearby) {
            player.playSound(location, sound, category, volume, pitch);
        }
    }

    public static void play(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, 1.0f, 1.0f);
    }

    public static void play(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch);
    }

    public static void play(Location location, Sound sound, float volume, float pitch, double radius) {
        Collection<? extends Player> nearby = location.getWorld().getPlayers().stream()
                .filter(player -> player.getLocation().distanceSquared(location) <= radius * radius)
                .toList();

        for (Player player : nearby) {
            player.playSound(location, sound, SoundCategory.MASTER, volume, pitch);
        }
    }

    public static void play(Collection<? extends Player> players, Sound sound, float volume, float pitch) {
        for (Player player : players) {
            player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch);
        }
    }
}

