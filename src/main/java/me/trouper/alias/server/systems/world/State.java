package me.trouper.alias.server.systems.world;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.io.Closeable;
import java.util.*;

public class State implements Closeable {
    private Map<Location, Snapshot> snapshots;
    private volatile boolean closed = false;

    public State(Collection<Block> blocks) {
        this.snapshots = new HashMap<>(blocks.size() * 4 / 3 + 1);

        for (Block block : blocks) {
            if (block != null && block.getWorld() != null) {
                snapshots.put(block.getLocation(), new Snapshot(block));
            }
        }
    }

    public void restore() {
        if (closed || snapshots == null) {
            return;
        }

        try {
            for (Map.Entry<Location, Snapshot> entry : snapshots.entrySet()) {
                Location loc = entry.getKey();
                Snapshot snapshot = entry.getValue();

                if (loc != null && snapshot != null && loc.getWorld() != null) {
                    Block block = loc.getBlock();
                    if (block != null) {
                        snapshot.restore(block);
                    }
                }
            }
        } finally {
            close();
        }
    }

    public Set<Block> getBlocks() {
        if (closed || snapshots == null) {
            return new HashSet<>();
        }

        Set<Block> blocks = new HashSet<>(snapshots.size());
        for (Location loc : snapshots.keySet()) {
            if (loc != null && loc.getWorld() != null) {
                Block block = loc.getBlock();
                if (block != null) {
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    @Override
    public void close() {
        if (closed) return;

        synchronized (this) {
            if (closed) return;
            closed = true;

            if (snapshots != null) {
                snapshots.clear();
                snapshots = null;
            }
        }
    }

    public boolean isClosed() {
        return closed;
    }
}