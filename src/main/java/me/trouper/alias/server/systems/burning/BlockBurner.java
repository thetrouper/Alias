package me.trouper.alias.server.systems.burning;

import me.trouper.alias.server.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockSupport;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class BlockBurner implements Closeable, Main {
    private final BurnOptions options;
    private final BurnPalette palette;
    private boolean isClosed = false;
    private final Set<Block> visited = new HashSet<>();
    private final Map<Block, Material> burning = new HashMap<>();
    private final Set<Integer> tasks = new HashSet<>();

    public BlockBurner(BurnOptions options) {
        this.options = options;
        this.palette = new BurnPalette();
    }

    @Override
    public void close() {
        tasks.forEach(task -> Bukkit.getScheduler().cancelTask(task));
        visited.clear();
        burning.clear();
        isClosed = true;
    }

    public void burn(Block block, float heat) {
        if (isOccluded(block)) return;
        if (visited.contains(block)) return;

        visited.add(block);

        if (options.isDisabled()) return;
        if (block.isLiquid() || hasWater(block)) return;

        Block blockBelow = block.getRelative(0, -1, 0);
        if (block.getType().isAir() && canPlaceFireOn(blockBelow)) {
            if (ThreadLocalRandom.current().nextFloat() > options.getSetFireChance()) return;

            if (isClosed()) return;
            setBlock(block, palette.getFirePalette().getFirst());

            if (isClosed()) return;
            int taskId = Bukkit.getScheduler().runTaskLater(main.getPlugin(), ()->{
                if (!canPlaceFireOn(blockBelow)) {
                    setBlock(block, Material.AIR);
                }
            },20 * 10).getTaskId();

            if (!isClosed()) {
                tasks.add(taskId);
            } else {
                tasks.add(taskId);
                Bukkit.getScheduler().cancelTask(taskId);
            }

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
            totalDelay += stage.getDelay();
            if (isClosed()) return;

            int taskId = Bukkit.getScheduler().runTaskLater(main.getPlugin(), ()->{
                if (block.getType().isAir()) return;
                setBlock(block, stage.getBlockData());
            },totalDelay).getTaskId();

            if (!isClosed()) {
                tasks.add(taskId);
            } else {
                tasks.add(taskId);
                Bukkit.getScheduler().cancelTask(taskId);
            }
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
        if (isClosed()) return;
        block.setType(material);
    }

    private void setBlock(Block block, BlockData data) {
        if (isClosed()) return;
        block.setBlockData(data);
    }

    public boolean isClosed() {
        return isClosed;
    }
}
