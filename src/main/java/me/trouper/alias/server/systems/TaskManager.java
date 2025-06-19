package me.trouper.alias.server.systems;

import me.trouper.alias.server.Main;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TaskManager implements Main {
    private final ConcurrentHashMap<Integer, Boolean> tasks = new ConcurrentHashMap<>();
    private volatile boolean closed = false;

    public int scheduleTask(Runnable task, long delay) {
        if (closed) return -1;

        int taskId = Bukkit.getScheduler().runTaskLater(main.getPlugin(), () -> {
            if (!closed && tasks.containsKey(taskId)) {
                task.run();
                tasks.remove(taskId);
            }
        }, delay).getTaskId();

        if (!closed) {
            tasks.put(taskId, Boolean.TRUE);
            return taskId;
        } else {
            Bukkit.getScheduler().cancelTask(taskId);
            return -1;
        }
    }

    public void close() {
        closed = true;
        tasks.keySet().forEach(Bukkit.getScheduler()::cancelTask);
        tasks.clear();
    }
}
