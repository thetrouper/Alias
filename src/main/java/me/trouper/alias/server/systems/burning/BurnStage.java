package me.trouper.alias.server.systems.burning;

import org.bukkit.block.data.BlockData;

public class BurnStage {
    private final long delay;
    private final BlockData blockData;

    public BurnStage(long delay, BlockData blockData) {
        this.delay = delay;
        this.blockData = blockData;
    }

    public long getDelay() { return delay; }
    public BlockData getBlockData() { return blockData; }
}
