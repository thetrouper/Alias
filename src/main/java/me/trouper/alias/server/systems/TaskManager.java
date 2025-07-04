package me.trouper.alias.server.systems;

import me.trouper.alias.AliasContext;
import org.bukkit.Bukkit;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

public class TaskManager implements Closeable {
    private final AliasContext context;
    private final Map<Integer, Boolean> tasks = new HashMap<>();
    private volatile boolean closed = false;

    public TaskManager(AliasContext context) {
        this.context = context;
    }

    public int scheduleTask(Runnable task, long delay) {
        if (closed) return -1;

        int taskId = Bukkit.getScheduler().runTaskLater(context.getPlugin(), () -> {
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
