package me.trouper.alias.server.systems.world.burning;

import me.trouper.alias.server.systems.TaskManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockSupport;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BlockBurner implements Closeable {
    private final BurnOptions options;
    private final BurnPalette palette;
    private final TaskManager taskManager;
    private final Set<Block> visited = new HashSet<>();
    private final Map<Block, Material> burning = new HashMap<>();

    public BlockBurner(TaskManager taskManager, BurnOptions options) {
        this.taskManager = taskManager;
        this.options = options;
        this.palette = new BurnPalette();
    }

    @Override
    public void close() {
        taskManager.close();
        visited.clear();
        burning.clear();
    }

    public void burn(Block block, float heat, boolean checkOcclusion) {
        if (checkOcclusion && isOccluded(block)) return;
        if (visited.contains(block)) return;

        visited.add(block);

        if (options.isDisabled()) return;
        if (block.isLiquid() || hasWater(block)) return;

        Block blockBelow = block.getRelative(0, -1, 0);
        if (block.getType().isAir() && canPlaceFireOn(blockBelow)) {
            if (ThreadLocalRandom.current().nextFloat() > options.getSetFireChance()) return;

            setBlock(block, palette.getFirePalette().getFirst());

            taskManager.scheduleTask(() -> {
                if (!canPlaceFireOn(blockBelow)) {
                    setBlock(block, Material.AIR);
                }
            }, 20 * 10);

            return;
        }

        if (block.getType().isAir()) return;

        List<BurnStage> burnStages = palette.burn(block, heat);
        if (burnStages == null) return;

        burning.put(block, block.getType());
        scheduleBurnStages(block, burnStages);
    }

    private void scheduleBurnStages(Block block, List<BurnStage> stages) {
        long totalDelay = 0;

        for (BurnStage stage : stages) {
            totalDelay += stage.getDelayTicks();

            taskManager.scheduleTask(() -> {
                if (block.getType().isAir()) return;
                setBlock(block, stage.getBlockData());
            }, totalDelay);
        }
    }

    private boolean isOccluded(Block block) {
        return isOccluding(block.getRelative(0, 1, 0)) &&
                isOccluding(block.getRelative(0, -1, 0)) &&
                isOccluding(block.getRelative(1, 0, 0)) &&
                isOccluding(block.getRelative(-1, 0, 0)) &&
                isOccluding(block.getRelative(0, 0, 1)) &&
                isOccluding(block.getRelative(0, 0, -1));
    }

    private boolean isOccluding(Block block) {
        Material material = burning.getOrDefault(block, block.getType());
        return material.isOccluding();
    }

    private boolean canPlaceFireOn(Block block) {
        return block.getBlockData().isFaceSturdy(BlockFace.UP, BlockSupport.RIGID);
    }

    private boolean hasWater(Block block) {
        Material type = block.getType();
        if (type == Material.KELP || type == Material.KELP_PLANT ||
                type == Material.SEAGRASS || type == Material.TALL_SEAGRASS) {
            return true;
        }

        BlockData data = block.getBlockData();
        if (data instanceof Waterlogged) {
            return ((Waterlogged) data).isWaterlogged();
        }
        return false;
    }

    private void setBlock(Block block, Material material) {
        if (taskManager.isClosed()) return;
        block.setType(material);
    }

    private void setBlock(Block block, BlockData data) {
        if (taskManager.isClosed()) return;
        block.setBlockData(data);
    }

    public boolean isClosed() {
        return taskManager.isClosed();
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }
}