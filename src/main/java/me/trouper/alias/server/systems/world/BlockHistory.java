package me.trouper.alias.server.systems.world;

import org.bukkit.block.Block;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class BlockHistory {

    private final List<State> history = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private int currentIndex = -1;

    public void addChange(State state) {
        lock.lock();
        try {
            if (currentIndex < history.size() - 1) {
                history.subList(currentIndex + 1, history.size()).clear();
            }
            history.add(state);
            currentIndex = history.size() - 1;
        } finally {
            lock.unlock();
        }
    }

    public void addChange(Collection<Block> blocks) {
        lock.lock();
        try {
            if (currentIndex < history.size() - 1) {
                history.subList(currentIndex + 1, history.size()).clear();
            }
            State newState = new State(blocks);
            history.add(newState);
            currentIndex = history.size() - 1;
        } finally {
            lock.unlock();
        }
    }

    public boolean canUndo() {
        lock.lock();
        try {
            return currentIndex > 0;
        } finally {
            lock.unlock();
        }
    }

    public boolean canRedo() {
        lock.lock();
        try {
            return currentIndex < history.size() - 1;
        } finally {
            lock.unlock();
        }
    }

    public boolean undo() {
        lock.lock();
        try {
            if (!canUndo()) return false;
            currentIndex--;
            history.get(currentIndex).restore();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean redo() {
        lock.lock();
        try {
            if (!canRedo()) return false;
            currentIndex++;
            history.get(currentIndex).restore();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public int getCurrentIndex() {
        lock.lock();
        try {
            return currentIndex;
        } finally {
            lock.unlock();
        }
    }

    public int getHistorySize() {
        lock.lock();
        try {
            return history.size();
        } finally {
            lock.unlock();
        }
    }

    public Set<Block> getCurrentBlocks() {
        lock.lock();
        try {
            if (currentIndex >= 0 && currentIndex < history.size()) {
                return history.get(currentIndex).getBlocks();
            }
            return Collections.emptySet();
        } finally {
            lock.unlock();
        }
    }
}
