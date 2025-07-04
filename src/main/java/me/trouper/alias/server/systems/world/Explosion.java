package me.trouper.alias.server.systems.world;

import me.trouper.alias.AliasContext;
import me.trouper.alias.server.systems.TaskManager;
import me.trouper.alias.server.systems.world.burning.BlockBurner;
import me.trouper.alias.utils.ParticleUtils;
import me.trouper.alias.utils.SoundPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Explosion {

    private final AliasContext context;

    public Explosion(AliasContext context) {
        this.context = context;
    }

    public ExplosionResult createExplosion(Location center, ExplosionOptions options) {
        World world = center.getWorld();
        if (world == null) throw new IllegalArgumentException("Center location must have a valid world");

        double maxBurnRadius = options.getMaxBurnRadius();

        try {
            Map<Block, Double> affectedBlocks = getBlocksInRadius(center, maxBurnRadius);
            Map<UUID, Double> affectedEntities = getEntitiesInRadius(center, maxBurnRadius);

            TaskManager sharedTaskManager = context.createTaskManager();
            BlockBurner burner = new BlockBurner(sharedTaskManager,options.getBurnOptions());
            ExplosionResult result = new ExplosionResult(affectedBlocks.keySet(), sharedTaskManager);

            List<Block> blocksToDestroy = new ArrayList<>();
            List<Block> blocksToBurn = new ArrayList<>();
            Map<Block, Float> blocksHeatMap = new HashMap<>(affectedBlocks.size());

            categorizeBlocks(affectedBlocks, options, blocksToDestroy, blocksToBurn, blocksHeatMap);

            scheduleDestruction(blocksToDestroy, options, sharedTaskManager);
            scheduleBurning(blocksToBurn, blocksHeatMap, burner, center, options, sharedTaskManager);
            scheduleDamage(affectedEntities, options, sharedTaskManager);

            if (options.isCreateParticles() || options.isPlaySound()) {
                createExplosionEffects(center, options, sharedTaskManager);
            }

            return result;

        } catch (Exception e) {
            System.err.println("Failed to create explosion: " + e.getMessage());
            throw e;
        }
    }

    private Map<Block, Double> getBlocksInRadius(Location center, double radius) {
        World world = center.getWorld();
        if (world == null) return Collections.emptyMap();

        int radiusInt = (int) Math.ceil(radius);

        int estimatedBlocks = (int) ((4.0 / 3.0) * Math.PI * Math.pow(radius, 3));
        Map<Block, Double> blocks = new HashMap<>(estimatedBlocks * 4 / 3 + 1);

        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        double radiusSquared = radius * radius;

        for (int x = -radiusInt; x <= radiusInt; x++) {
            for (int y = -radiusInt; y <= radiusInt; y++) {
                for (int z = -radiusInt; z <= radiusInt; z++) {
                    double distanceSquared = x*x + y*y + z*z;
                    if (distanceSquared > radiusSquared) continue;

                    Block block = world.getBlockAt(centerX + x, centerY + y, centerZ + z);
                    if (block != null && !block.getType().isAir()) {
                        double distance = Math.sqrt(distanceSquared);
                        blocks.put(block, distance);
                    }
                }
            }
        }

        return blocks;
    }

    private Map<UUID, Double> getEntitiesInRadius(Location center, double radius) {
        List<LivingEntity> rawList = center.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .toList();

        if (rawList.isEmpty()) return Collections.emptyMap();

        Map<UUID, Double> entities = new HashMap<>(rawList.size() * 4 / 3 + 1);

        for (LivingEntity livingEntity : rawList) {
            if (livingEntity != null && livingEntity.isValid()) {
                entities.put(livingEntity.getUniqueId(), livingEntity.getLocation().distance(center));
            }
        }

        return entities;
    }

    private void categorizeBlocks(Map<Block, Double> affectedBlocks, ExplosionOptions options,
                                         List<Block> blocksToDestroy, List<Block> blocksToBurn,
                                         Map<Block, Float> blocksHeatMap) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int totalBlocks = affectedBlocks.size();
        List<Block> tempBlocksToBurn = new ArrayList<>(totalBlocks / 2);

        for (Map.Entry<Block, Double> entry : affectedBlocks.entrySet()) {
            Block block = entry.getKey();
            double distance = entry.getValue();

            if (block.getType().isAir() || !block.getType().isBlock()) continue;

            float heat = calculateHeat(distance, options);
            blocksHeatMap.put(block, heat);

            if (distance <= options.getCoreRadius()) {
                blocksToDestroy.add(block);
                tempBlocksToBurn.add(block);
                continue;
            }

            if (distance <= options.getFalloffRadius()) {
                double destructionChance = 1.0 - ((distance - options.getCoreRadius()) /
                        (options.getFalloffRadius() - options.getCoreRadius()));

                destructionChance *= (0.7 + random.nextDouble() * 0.6);

                if (random.nextDouble() < destructionChance) {
                    blocksToDestroy.add(block);
                } else {
                    tempBlocksToBurn.add(block);
                }
            } else {
                double burnChance = 1.0 - ((distance - options.getFalloffRadius()) /
                        (options.getMaxBurnRadius() - options.getFalloffRadius()));
                burnChance *= (0.8 + random.nextDouble() * 0.6);

                if (random.nextDouble() < burnChance) {
                    tempBlocksToBurn.add(block);
                }
            }
        }

        Set<Block> toDestroySet = new HashSet<>(blocksToDestroy);
        for (Block block : tempBlocksToBurn) {
            if (!isOccluded(block, toDestroySet)) {
                blocksToBurn.add(block);
            }
        }
    }

    private float calculateHeat(double distance, ExplosionOptions options) {
        double normalizedDistance = distance / options.getMaxBurnRadius();
        double heatRange = options.getMaxHeat() - options.getMinHeat();
        double heatFactor = Math.pow(1.0 - normalizedDistance, 2.0);
        return (float) (options.getMinHeat() + heatRange * heatFactor);
    }

    private void scheduleDestruction(List<Block> blocksToDestroy, ExplosionOptions options, TaskManager taskManager) {
        if (blocksToDestroy.isEmpty()) return;

        long destructionDelayTicks = (long) (options.getDestructionDelay() * 20);

        taskManager.scheduleTask(() -> {
            Collections.shuffle(blocksToDestroy);
            int blocksPerWave = Math.max(1, blocksToDestroy.size() / 5);

            for (int wave = 0; wave < 5; wave++) {
                int startIndex = wave * blocksPerWave;
                int endIndex = Math.min(startIndex + blocksPerWave, blocksToDestroy.size());

                if (startIndex >= blocksToDestroy.size()) break;

                final int finalStartIndex = startIndex;
                final int finalEndIndex = endIndex;

                taskManager.scheduleTask(() -> {
                    for (int i = finalStartIndex; i < finalEndIndex; i++) {
                        if (i < blocksToDestroy.size()) {
                            Block block = blocksToDestroy.get(i);
                            if (block != null && !block.getType().isAir()) {
                                block.setType(Material.AIR);
                            }
                        }
                    }
                }, wave * 2L);
            }
        }, destructionDelayTicks);
    }

    private void scheduleBurning(List<Block> blocksToMaybeBurn, Map<Block, Float> blocksHeatMap,
                                        BlockBurner burner, Location center, ExplosionOptions options,
                                        TaskManager taskManager) {
        if (blocksToMaybeBurn.isEmpty()) return;

        long burnDelayTicks = (long) (options.getBurnDelay() * 20);

        taskManager.scheduleTask(() -> {
            Collections.shuffle(blocksToMaybeBurn);

            Map<Integer, List<Block>> burnWaves = new HashMap<>();

            for (Block block : blocksToMaybeBurn) {
                if (block != null) {
                    double distance = block.getLocation().distance(center);
                    int waveIndex = (int) (distance / 2.0);
                    burnWaves.computeIfAbsent(waveIndex, k -> new ArrayList<>()).add(block);
                }
            }

            for (Map.Entry<Integer, List<Block>> waveEntry : burnWaves.entrySet()) {
                int waveDelay = waveEntry.getKey() * 3;
                List<Block> waveBlocks = waveEntry.getValue();

                taskManager.scheduleTask(() -> {
                    ThreadLocalRandom random = ThreadLocalRandom.current();

                    for (Block block : waveBlocks) {
                        if (burner.isClosed() || block == null) continue;

                        float heat = blocksHeatMap.getOrDefault(block, 0.1f);
                        int randomDelay = random.nextInt(0, 10);

                        taskManager.scheduleTask(() -> {
                            if (!burner.isClosed() && block != null && !block.getType().isAir()) {
                                burner.burn(block, heat, false);
                            }
                        }, randomDelay);
                    }
                }, waveDelay);
            }
        }, burnDelayTicks);
    }

    private void scheduleDamage(Map<UUID, Double> affected, ExplosionOptions options, TaskManager taskManager) {
        if (affected.isEmpty()) return;

        double baseDamage = options.getBaseDamage();
        double igniteDistance = options.getMaxBurnRadius();
        double halfDamageDistance = options.getFalloffRadius();
        double fullDamageDistance = options.getCoreRadius();

        for (Map.Entry<UUID, Double> entityDistance : affected.entrySet()) {
            UUID entityId = entityDistance.getKey();
            if (entityId == null) continue;

            double distance = entityDistance.getValue();
            long delay = Math.max(1, (long) distance / 2);

            if (distance >= halfDamageDistance && distance <= igniteDistance) {
                taskManager.scheduleTask(() -> {
                    LivingEntity entity = (LivingEntity) Bukkit.getEntity(entityId);
                    if (entity != null && entity.isValid()) {
                        entity.setFireTicks(5 * 20);
                    }
                }, delay);
            } else if (distance >= fullDamageDistance && distance <= halfDamageDistance) {
                taskManager.scheduleTask(() -> {
                    LivingEntity entity = (LivingEntity) Bukkit.getEntity(entityId);
                    if (entity != null && entity.isValid()) {
                        entity.setFireTicks(10 * 20);
                        entity.damage(baseDamage / 2);
                    }
                }, delay);
            } else if (distance <= fullDamageDistance) {
                taskManager.scheduleTask(() -> {
                    LivingEntity entity = (LivingEntity) Bukkit.getEntity(entityId);
                    if (entity != null && entity.isValid()) {
                        entity.setFireTicks(15 * 20);
                        entity.damage(baseDamage);
                    }
                }, delay);
            }
        }
    }

    private void createExplosionEffects(Location center, ExplosionOptions options, TaskManager taskManager) {
        World world = center.getWorld();
        if (world == null) return;

        double soundRange = options.getMaxBurnRadius() * 20;

        if (options.isPlaySound()) {
            new SoundPlayer(Sound.ENTITY_WARDEN_SONIC_BOOM, 40, 0.5F).playAt(center, soundRange);
            new SoundPlayer(Sound.ENTITY_WARDEN_SONIC_BOOM, 40, 1.0F).playAt(center, soundRange);
            new SoundPlayer(Sound.ITEM_TOTEM_USE, 40, 0.5F).playAt(center, soundRange);
        }

        if (options.isCreateParticles()) {
            world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 3);
            world.spawnParticle(Particle.EXPLOSION, center, 20, 2, 2, 2, 0.1);
            world.spawnParticle(Particle.LARGE_SMOKE, center, 15, 1, 1, 1, 0.05);
            world.spawnParticle(Particle.FLAME, center, 30, 3, 3, 3, 0.1);
            
            taskManager.scheduleTask(()->{
                if (center.getWorld() != null) {
                    center.getWorld().spawnParticle(Particle.SMOKE, center, 50, 4, 4, 4, 0.02);
                    ParticleUtils.builder()
                            .type(Particle.SMOKE)
                            .count((int) options.getCoreRadius()*10)
                            .offset(options.getCoreRadius()/2,options.getCoreRadius()/2,options.getCoreRadius()/2)
                            .speed(0.05F)
                            .spawn(center);
                }
            },20L);
        }
    }

    private boolean isOccluded(Block block, Set<Block> doesNotOcclude) {
        if (block == null) return true;

        return isOccluding(block.getRelative(0, 1, 0), doesNotOcclude) &&
                isOccluding(block.getRelative(0, -1, 0), doesNotOcclude) &&
                isOccluding(block.getRelative(1, 0, 0), doesNotOcclude) &&
                isOccluding(block.getRelative(-1, 0, 0), doesNotOcclude) &&
                isOccluding(block.getRelative(0, 0, 1), doesNotOcclude) &&
                isOccluding(block.getRelative(0, 0, -1), doesNotOcclude);
    }

    private boolean isOccluding(Block block, Set<Block> doesNotOcclude) {
        return block != null && block.getType().isOccluding() && !doesNotOcclude.contains(block);
    }
}