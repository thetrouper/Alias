package me.trouper.alias.server.systems.world;

import me.trouper.alias.server.systems.TaskManager;
import org.bukkit.block.Block;

import java.io.Closeable;
import java.util.Set;

public class ExplosionResult implements Closeable {
    private State previousState;
    private TaskManager taskManager;
    private volatile boolean closed = false;

    public ExplosionResult(Set<Block> affectedBlocks, TaskManager manager) {
        this.taskManager = manager;
        this.previousState = new State(affectedBlocks);
    }

    @Override
    public void close() {
        if (closed) return;

        synchronized (this) {
            if (closed) return;
            closed = true;

            if (taskManager != null) {
                taskManager.close();
                taskManager = null;
            }

            if (previousState != null) {
                previousState.close();
                previousState = null;
            }
        }
    }

    public void restore() {
        if (closed) {
            throw new IllegalStateException("ExplosionResult has been closed");
        }

        try {
            if (previousState != null) {
                previousState.restore();
            }
        } finally {
            close();
        }
    }

    public State getPreviousState() {
        if (closed) return null;
        return previousState;
    }

    public TaskManager getTaskManager() {
        if (closed) return null;
        return taskManager;
    }

    public boolean isClosed() {
        return closed;
    }
}