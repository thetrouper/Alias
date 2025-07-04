package me.trouper.alias.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ParticleUtils {

    public static ParticleBuilder builder() {
        return new ParticleBuilder();
    }

    public static class ParticleBuilder {
        private Particle type = Particle.FLAME;
        private int count = 1;
        private double offsetX = 0;
        private double offsetY = 0;
        private double offsetZ = 0;
        private float speed = 0.0f;
        private Object data = null;
        private Set<Player> viewers = new HashSet<>();

        public ParticleBuilder type(Particle type) {
            this.type = type;
            return this;
        }

        public ParticleBuilder count(int count) {
            this.count = count;
            return this;
        }

        public ParticleBuilder offset(double x, double y, double z) {
            this.offsetX = x;
            this.offsetY = y;
            this.offsetZ = z;
            return this;
        }

        public ParticleBuilder speed(float speed) {
            this.speed = speed;
            return this;
        }

        public ParticleBuilder data(Particle.DustOptions dust) {
            this.data = dust;
            return this;
        }

        public ParticleBuilder data(Material block) {
            this.data = block.createBlockData();
            return this;
        }

        public ParticleBuilder data(BlockData blockData) {
            this.data = blockData;
            return this;
        }

        public ParticleBuilder data(Object customData) {
            this.data = customData;
            return this;
        }

        public ParticleBuilder viewers(Set<Player> viewers) {
            if (viewers != null) {
                this.viewers = viewers;
            }
            return this;
        }

        public void spawn(Location location) {
            if (location == null || location.getWorld() == null) return;

            if (viewers == null || viewers.isEmpty()) {
                location.getWorld().spawnParticle(type, location, count, offsetX, offsetY, offsetZ, speed, data);
            } else {
                for (Player player : viewers) {
                    if (player != null && player.isOnline()) {
                        player.spawnParticle(type, location, count, offsetX, offsetY, offsetZ, speed, data);
                    }
                }
            }
        }
    }
}
