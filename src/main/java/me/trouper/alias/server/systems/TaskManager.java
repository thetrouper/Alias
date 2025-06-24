package me.trouper.alias.server.systems;

import me.trouper.alias.server.Main;
import org.bukkit.Bukkit;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TaskManager implements Closeable, Main {
    private final Map<Integer, Boolean> tasks = new HashMap<>();
    private volatile boolean closed = false;

    public int scheduleTask(Runnable task, long delay) {
        if (closed) return -1;

        int taskId = Bukkit.getScheduler().runTaskLater(main.getPlugin(), () -> {
            if (!closed) {
                task.run();
            }
        }, delay).getTaskId();

        if (!closed) {
            tasks.put(taskId,Boolean.TRUE);
            return taskId;
        } else {
            Bukkit.getScheduler().cancelTask(taskId);
            return -1;
        }
    }

    @Override
    public void close() {
        closed = true;
        tasks.keySet().forEach(Bukkit.getScheduler()::cancelTask);
        tasks.clear();
    }

    public boolean isClosed() {
        return closed;
    }
}
