package me.trouper.alias.server.systems.world;

import me.trouper.alias.server.Main;
import me.trouper.alias.server.systems.Verbose;
import me.trouper.alias.server.systems.burning.BlockBurner;
import me.trouper.alias.server.systems.burning.BurnOptions;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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
        private final List<Integer> scheduledTaskIds = new ArrayList<>();
        private final BlockBurner burner;

        public ExplosionResult(BlockBurner burner) {
            this.burner = burner;
        }

        public void cleanup() {
            if (burner != null) {
                burner.close();
            }

            scheduledTaskIds.forEach(task -> Bukkit.getScheduler().cancelTask(task));
        }

        void recordSnapshot(Block block) {
            BlockState state = block.getState();
            originalStates.put(block, state);
            if (state instanceof InventoryHolder) {
                Inventory inv = ((InventoryHolder) state).getInventory();
                originalInventories.put(block, inv.getContents());
            }
        }

        void addScheduledTask(int taskId) {
            scheduledTaskIds.add(taskId);
        }

        public void restore() {
            for (int taskId : scheduledTaskIds) {
                Bukkit.getScheduler().cancelTask(taskId);
            }

            cleanup();

            for (Map.Entry<Block, BlockState> entry : originalStates.entrySet()) {
                Block block = entry.getKey();
                BlockState snapshot = entry.getValue();

                block.setBlockData(snapshot.getBlockData(), false);
                snapshot.update(true, false);

                ItemStack[] contents = originalInventories.get(block);
                if (contents != null && block.getState() instanceof InventoryHolder) {
                    Inventory inv = ((InventoryHolder) block.getState()).getInventory();
                    inv.setContents(contents);
                }
            }
        }

        public BlockBurner getBurner() { return burner; }

        public Map<Block, BlockState> getOriginalStates() {
            return originalStates;
        }

        public Map<Block, ItemStack[]> getOriginalInventories() {
            return originalInventories;
        }

        public List<Integer> getScheduledTaskIds() {
            return scheduledTaskIds;
        }
    }

    public static ExplosionResult createExplosion(Location center, ExplosionOptions options) {
        World world = center.getWorld();
        if (world == null) throw new IllegalArgumentException("Center location must have a valid world");

        Map<Block, Double> affectedBlocks = getBlocksInRadius(center, options.getMaxBurnRadius());

        ExplosionResult result = new ExplosionResult(new BlockBurner(options.getBurnOptions()));

        for (Block block : affectedBlocks.keySet()) {
            if (block.getType().isAir()) continue;

            result.recordSnapshot(block);
        }

        Set<Block> blocksToDestroy = new HashSet<>();
        Set<Block> blocksToBurn = new HashSet<>();
        Map<Block, Float> blocksHeatMap = new HashMap<>();

        categorizeBlocks(affectedBlocks, options, blocksToDestroy, blocksToBurn, blocksHeatMap);

        BlockBurner burner = new BlockBurner(options.getBurnOptions());

        scheduleDestruction(blocksToDestroy, options,result);
        scheduleBurning(blocksToBurn, blocksHeatMap, burner, center, options,result);

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

    private static void scheduleDestruction(Set<Block> blocksToDestroy, ExplosionOptions options, ExplosionResult result) {
        if (blocksToDestroy.isEmpty()) return;

        long destructionDelayTicks = (long) (options.getDestructionDelay() * 20);

        int outerTask = Bukkit.getScheduler().runTaskLater(main.getPlugin(),()->{
            List<Block> blockList = new ArrayList<>(blocksToDestroy);
            Collections.shuffle(blockList);

            int blocksPerWave = Math.max(1, blockList.size() / 5);

            for (int wave = 0; wave < 5; wave++) {
                int startIndex = wave * blocksPerWave;
                int endIndex = Math.min(startIndex + blocksPerWave, blockList.size());

                if (startIndex >= blockList.size()) break;

                int innerTask = Bukkit.getScheduler().runTaskLater(main.getPlugin(),()->{
                    for (int i = startIndex; i < endIndex; i++) {
                        Block block = blockList.get(i);
                        if (!block.getType().isAir()) {
                            block.setType(Material.AIR);
                        }
                    }
                },wave * 2).getTaskId();

                result.addScheduledTask(innerTask);
            }
        },destructionDelayTicks).getTaskId();

        result.addScheduledTask(outerTask);
    }

    private static void scheduleBurning(Set<Block> blocksToMaybeBurn, Map<Block, Float> blocksHeatMap,
                                        BlockBurner burner, Location center, ExplosionOptions options,
                                        ExplosionResult result) {
        if (blocksToMaybeBurn.isEmpty()) return;

        long burnDelayTicks = (long) (options.getBurnDelay() * 20);
        int outerTask = Bukkit.getScheduler().runTaskLater(main.getPlugin(),()->{
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

                int middleTask = Bukkit.getScheduler().runTaskLater(main.getPlugin(),()->{
                    for (Block block : waveBlocks) {
                        if (burner.isClosed()) continue;

                        float heat = blocksHeatMap.getOrDefault(block, 0.1f);

                        ThreadLocalRandom random = ThreadLocalRandom.current();
                        int randomDelay = random.nextInt(0, 10);

                        int innerTask = Bukkit.getScheduler().runTaskLater(main.getPlugin(),()->{
                            if (!burner.isClosed() && !block.getType().isAir()) {
                                burner.burn(block, heat);
                            }
                        },randomDelay).getTaskId();

                        result.addScheduledTask(innerTask);
                    }
                },waveDelay).getTaskId();

                result.addScheduledTask(middleTask);
            }
        },burnDelayTicks).getTaskId();

        result.addScheduledTask(outerTask);
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

            Bukkit.getScheduler().runTaskLater(main.getPlugin(),()->{
                world.spawnParticle(Particle.SMOKE, center, 50, 4, 4, 4, 0.02);
            },20);
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