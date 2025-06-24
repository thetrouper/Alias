package me.trouper.alias.server.systems.burning;

import org.bukkit.block.data.BlockData;

public class BurnStage {
    private final long delayTicks;
    private final BlockData blockData;

    public BurnStage(long delayTicks, BlockData blockData) {
        this.delayTicks = delayTicks;
        this.blockData = blockData;
    }

    public long getDelayTicks() { return delayTicks; }
    public BlockData getBlockData() { return blockData; }
}
