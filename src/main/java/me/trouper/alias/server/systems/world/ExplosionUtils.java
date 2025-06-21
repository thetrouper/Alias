package me.trouper.alias.server.systems.world;

import me.trouper.alias.server.Main;
import me.trouper.alias.server.systems.Verbose;
import me.trouper.alias.server.systems.TaskManager;
import me.trouper.alias.server.systems.burning.BlockBurner;
import me.trouper.alias.server.systems.burning.BurnOptions;
import me.trouper.alias.utils.TargetingUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ExplosionUtils implements Main {

    public static class ExplosionOptions {
        private double coreRadius = 3.0;
        private double falloffRadius = 8.0;
        private double maxBurnRadius = 15.0;
        private double baseDamage = 20;
        private double destructionDelay = 0.0; // SECONDS
        private double burnDelay = 0.5; // SECONDS
        private double maxHeat = 1.0;
        private double minHeat = 0.1;
        private boolean createParticles = true;
        private boolean playSound = true;
        private BurnOptions burnOptions = new BurnOptions();

        public double getCoreRadius() { return coreRadius; }
        public void setCoreRadius(double coreRadius) { this.coreRadius = coreRadius; }

        public double getFalloffRadius() { return falloffRadius; }
        public void setFalloffRadius(double falloffRadius) { this.falloffRadius = falloffRadius; }

        public double getMaxBurnRadius() { return maxBurnRadius; }
        public void setMaxBurnRadius(double maxBurnRadius) { this.maxBurnRadius = maxBurnRadius; }

        public double getBaseDamage() { return baseDamage; }
        public void setBaseDamage(double baseDamage) { this.baseDamage = baseDamage; }

        public double getDestructionDelay() { return destructionDelay; }
        public void setDestructionDelay(double destructionDelay) { this.destructionDelay = destructionDelay; }

        public double getBurnDelay() { return burnDelay; }
        public void setBurnDelay(double burnDelay) { this.burnDelay = burnDelay; }

        public double getMaxHeat() { return maxHeat; }
        public void setMaxHeat(double maxHeat) { this.maxHeat = maxHeat; }

        public double getMinHeat() { return minHeat; }
        public void setMinHeat(double minHeat) { this.minHeat = minHeat; }

        public boolean isCreateParticles() { return createParticles; }
        public void setCreateParticles(boolean createParticles) { this.createParticles = createParticles; }

        public boolean isPlaySound() { return playSound; }
        public void setPlaySound(boolean playSound) { this.playSound = playSound; }

        public BurnOptions getBurnOptions() { return burnOptions; }
        public void setBurnOptions(BurnOptions burnOptions) { this.burnOptions = burnOptions; }
    }

    public static class ExplosionResult {
        private final Map<Block, BlockState> originalStates = new HashMap<>();
        private final Map<Block, ItemStack[]> originalInventories = new HashMap<>();
        private final TaskManager taskManager;
        private final BlockBurner burner;

        public ExplosionResult(TaskManager taskManager) {
            this.taskManager = taskManager;
            this.burner = new BlockBurner(new BurnOptions(), taskManager);
        }

        public ExplosionResult(BlockBurner burner) {
            this.burner = burner;
            this.taskManager = burner.getTaskManager();
        }

        public void cleanup() {
            taskManager.close();
        }

        void recordSnapshot(Block block) {
            BlockState state = block.getState();
            originalStates.put(block, state);
            if (state instanceof InventoryHolder) {
                Inventory inv = ((InventoryHolder) state).getInventory();
                originalInventories.put(block, inv.getContents());
            }
        }

        public void restore() {
            cleanup();

            for (Map.Entry<Block, BlockState> entry : originalStates.entrySet()) {
                Block block = entry.getKey();
                BlockState snapshot = entry.getValue();

                Bukkit.getScheduler().runTask(main.getPlugin(),()->{
                    block.setBlockData(snapshot.getBlockData(), false);
                    snapshot.update(true, false);

                    ItemStack[] contents = originalInventories.get(block);
                    if (contents != null && block.getState() instanceof InventoryHolder) {
                        Inventory inv = ((InventoryHolder) block.getState()).getInventory();
                        inv.setContents(contents);
                    }
                });
            }
        }

        public BlockBurner getBurner() { return burner; }
        public TaskManager getTaskManager() { return taskManager; }

        public Map<Block, BlockState> getOriginalStates() {
            return originalStates;
        }

        public Map<Block, ItemStack[]> getOriginalInventories() {
            return originalInventories;
        }
    }

    public static ExplosionResult createExplosion(Location center, ExplosionOptions options) {
        World world = center.getWorld();
        if (world == null) throw new IllegalArgumentException("Center location must have a valid world");

        double maxBurnRadius = options.getMaxBurnRadius();

        Map<Block, Double> affectedBlocks = getBlocksInRadius(center, maxBurnRadius);
        Map<UUID, Double> affectedEntities = getEntitiesInRadius(center, maxBurnRadius);

        TaskManager sharedTaskManager = new TaskManager();
        BlockBurner burner = new BlockBurner(options.getBurnOptions(), sharedTaskManager);
        ExplosionResult result = new ExplosionResult(burner);

        for (Block block : affectedBlocks.keySet()) {
            if (block.getType().isAir()) continue;
            result.recordSnapshot(block);
        }

        Set<Block> blocksToDestroy = new HashSet<>();
        Set<Block> blocksToBurn = new HashSet<>();
        Map<Block, Float> blocksHeatMap = new HashMap<>();

        categorizeBlocks(affectedBlocks, options, blocksToDestroy, blocksToBurn, blocksHeatMap);

        scheduleDestruction(blocksToDestroy, options, sharedTaskManager);
        scheduleBurning(blocksToBurn, blocksHeatMap, burner, center, options, sharedTaskManager);
        scheduleDamage(affectedEntities, options, sharedTaskManager);

        if (options.isCreateParticles() || options.isPlaySound()) createExplosionEffects(center, options);

        return result;
    }

    private static Map<Block, Double> getBlocksInRadius(Location center, double radius) {
        Map<Block, Double> blocks = new HashMap<>();
        World world = center.getWorld();

        int radiusInt = (int) Math.ceil(radius);
        Vector centerVec = center.toVector();

        for (int x = -radiusInt; x <= radiusInt; x++) {
            for (int y = -radiusInt; y <= radiusInt; y++) {
                for (int z = -radiusInt; z <= radiusInt; z++) {
                    Block block = world.getBlockAt(
                            center.getBlockX() + x,
                            center.getBlockY() + y,
                            center.getBlockZ() + z
                    );

                    double distance = block.getLocation().toVector().distance(centerVec);
                    if (distance <= radius) {
                        blocks.put(block, distance);
                    }
                }
            }
        }

        return blocks;
    }

    public static Map<UUID, Double> getEntitiesInRadius(Location center, double radius) {
        List<LivingEntity> rawList = center.getNearbyLivingEntities(radius).stream().toList();
        Map<UUID, Double> entities = new HashMap<>();

        for (LivingEntity livingEntity : rawList) {
            entities.put(livingEntity.getUniqueId(),livingEntity.getLocation().distance(center));
        }

        return entities;
    }

    private static void categorizeBlocks(Map<Block, Double> affectedBlocks, ExplosionOptions options,
                                         Set<Block> blocksToDestroy, Set<Block> blocksToBurn,
                                         Map<Block, Float> blocksHeatMap) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (Map.Entry<Block, Double> entry : affectedBlocks.entrySet()) {
            Block block = entry.getKey();
            double distance = entry.getValue();

            if (block.getType().isAir() || !block.getType().isBlock()) continue;

            float heat = calculateHeat(distance, options);
            blocksHeatMap.put(block, heat);

            if (distance <= options.getCoreRadius()) blocksToDestroy.add(block);

            if (distance <= options.getFalloffRadius()) {
                double destructionChance = 1.0 - ((distance - options.getCoreRadius()) /
                        (options.getFalloffRadius() - options.getCoreRadius()));

                destructionChance *= (0.7 + random.nextDouble() * 0.6);

                if (random.nextDouble() < destructionChance) blocksToDestroy.add(block);
                else blocksToBurn.add(block);
            } else {
                double burnChance = 1.0 - ((distance - options.getFalloffRadius()) /
                        (options.getMaxBurnRadius() - options.getFalloffRadius()));
                burnChance *= (0.8 + random.nextDouble() * 0.6);

                if (random.nextDouble() < burnChance) blocksToBurn.add(block);
            }
        }
    }

    private static float calculateHeat(double distance, ExplosionOptions options) {
        double normalizedDistance = distance / options.getMaxBurnRadius();
        double heatRange = options.getMaxHeat() - options.getMinHeat();

        double heatFactor = Math.pow(1.0 - normalizedDistance, 2.0);

        return (float) (options.getMinHeat() + heatRange * heatFactor);
    }

    private static void scheduleDestruction(Set<Block> blocksToDestroy, ExplosionOptions options, TaskManager taskManager) {
        if (blocksToDestroy.isEmpty()) return;

        long destructionDelayTicks = (long) (options.getDestructionDelay() * 20);

        taskManager.scheduleTask(() -> {
            List<Block> blockList = new ArrayList<>(blocksToDestroy);
            Collections.shuffle(blockList);

            int blocksPerWave = Math.max(1, blockList.size() / 5);

            for (int wave = 0; wave < 5; wave++) {
                int startIndex = wave * blocksPerWave;
                int endIndex = Math.min(startIndex + blocksPerWave, blockList.size());

                if (startIndex >= blockList.size()) break;

                taskManager.scheduleTask(() -> {
                    for (int i = startIndex; i < endIndex; i++) {
                        Block block = blockList.get(i);
                        if (!block.getType().isAir()) {
                            block.setType(Material.AIR);
                        }
                    }
                }, wave * 2);
            }
        }, destructionDelayTicks);
    }

    private static void scheduleBurning(Set<Block> blocksToMaybeBurn, Map<Block, Float> blocksHeatMap,
                                        BlockBurner burner, Location center, ExplosionOptions options,
                                        TaskManager taskManager) {
        if (blocksToMaybeBurn.isEmpty()) return;

        long burnDelayTicks = (long) (options.getBurnDelay() * 20);

        taskManager.scheduleTask(() -> {
            List<Block> blockList = new ArrayList<>(blocksToMaybeBurn);
            Collections.shuffle(blockList);

            Map<Integer, List<Block>> burnWaves = new HashMap<>();

            for (Block block : blockList) {
                double distance = block.getLocation().distance(center);
                int waveIndex = (int) (distance / 2.0);

                burnWaves.computeIfAbsent(waveIndex, k -> new ArrayList<>()).add(block);
            }

            for (Map.Entry<Integer, List<Block>> waveEntry : burnWaves.entrySet()) {
                int waveDelay = waveEntry.getKey() * 3;
                List<Block> waveBlocks = waveEntry.getValue();

                taskManager.scheduleTask(() -> {
                    for (Block block : waveBlocks) {
                        if (burner.isClosed()) continue;

                        float heat = blocksHeatMap.getOrDefault(block, 0.1f);

                        ThreadLocalRandom random = ThreadLocalRandom.current();
                        int randomDelay = random.nextInt(0, 10);

                        taskManager.scheduleTask(() -> {
                            if (!burner.isClosed() && !block.getType().isAir()) {
                                burner.burn(block, heat);
                            }
                        }, randomDelay);
                    }
                }, waveDelay);
            }
        }, burnDelayTicks);
    }

    private static void scheduleDamage(Map<UUID, Double> affected, ExplosionOptions options, TaskManager taskManager) {
        double baseDamage = options.getBaseDamage();
        double igniteDistance = options.getMaxBurnRadius();
        double halfDamageDistance = options.getFalloffRadius();
        double fullDamageDistance = options.getCoreRadius();

        for (Map.Entry<UUID, Double> entityDistance : affected.entrySet()) {
            LivingEntity liv = (LivingEntity) Bukkit.getEntity(entityDistance.getKey());
            if (liv == null) continue;

            double distance = entityDistance.getValue();

            if (distance >= halfDamageDistance && distance <= igniteDistance) {
                taskManager.scheduleTask(()->{
                    liv.setFireTicks(5 * 20);
                },(long) distance / 2);
                return;
            }
            if (distance >= fullDamageDistance && distance <= halfDamageDistance) {
                taskManager.scheduleTask(()->{
                    liv.setFireTicks(10 * 20);
                    liv.damage(baseDamage / 2);
                },(long) distance / 2);
                return;
            }
            if (distance <= fullDamageDistance) {
                taskManager.scheduleTask(()->{
                    liv.setFireTicks(15 * 20);
                    liv.damage(baseDamage);
                },(long) distance / 2);
                return;
            }
        }
    }

    private static void createExplosionEffects(Location center, ExplosionOptions options) {
        World world = center.getWorld();
        if (world == null) return;

        if (options.isPlaySound()) {
            world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
            world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.2f);
        }

        if (options.isCreateParticles()) {
            world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 3);
            world.spawnParticle(Particle.EXPLOSION, center, 20, 2, 2, 2, 0.1);
            world.spawnParticle(Particle.LARGE_SMOKE, center, 15, 1, 1, 1, 0.05);
            world.spawnParticle(Particle.FLAME, center, 30, 3, 3, 3, 0.1);

            Bukkit.getScheduler().runTaskLater(main.getPlugin(), () -> {
                world.spawnParticle(Particle.SMOKE, center, 50, 4, 4, 4, 0.02);
            }, 20);
        }
    }

    public static ExplosionResult createExplosion(Location center) {
        return createExplosion(center, new ExplosionOptions());
    }

    public static ExplosionResult createExplosion(Location center, double coreRadius, double falloffRadius, double maxBurnRadius) {
        ExplosionOptions options = new ExplosionOptions();
        options.setCoreRadius(coreRadius);
        options.setFalloffRadius(falloffRadius);
        options.setMaxBurnRadius(maxBurnRadius);
        options.setBurnDelay(0);
        options.setDestructionDelay(0);
        return createExplosion(center, options);
    }
}