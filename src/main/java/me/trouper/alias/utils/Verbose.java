package me.trouper.alias.utils;

import me.trouper.alias.server.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Verbose implements Main {
    
    public static void send(int backtrace, String message, Object... args) {
        if (!me.trouper.alias.Alias.getInstance().getManager().io.config.debugMode) return;
        String callerInfo = "Unknown Caller";

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 2 + backtrace) {
            StackTraceElement caller = stackTrace[2 + backtrace];

            String className = caller.getClassName();
            className = className.substring(className.lastIndexOf(".") + 1);
            if (className.contains("-")) {
                callerInfo = "Protected";
            } else {
                callerInfo = className + "." + caller.getMethodName();
            }

            if (me.trouper.alias.Alias.getInstance().getManager().io.config.debuggerExclusions.contains(callerInfo)) {
                return;
            }
        }

        String formattedMessage = message.formatted(args);
        String log = "[DEBUG ^ %s] [%s]: %s".formatted(backtrace, callerInfo, formattedMessage);
        me.trouper.alias.Alias.getInstance().getLogger().info(log);

        for (Player operator : Bukkit.getOnlinePlayers()) {
            if (operator.isOp()) operator.sendMessage("§d§l%s §7[§bDEBUG ^ %s§7] §7[§e%s§7] §8» §7%s"
                    .formatted(main.config().messages.pluginName,backtrace, callerInfo, formattedMessage));
        }
    }

    public static void send(String message, Object... args) {
        if (!me.trouper.alias.Alias.getInstance().getManager().io.config.debugMode) return;
        String callerInfo = "Unknown Caller";

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 2) {
            StackTraceElement caller = stackTrace[2];

            String className = caller.getClassName();
            className = className.substring(className.lastIndexOf(".") + 1);
            if (className.contains("-")) {
                callerInfo = "Protected";
            } else {
                callerInfo = className + "." + caller.getMethodName();
            }
            
            if (main.config().debuggerExclusions.contains(callerInfo)) {
                return;
            }
        }

        String formattedMessage = message.formatted(args);
        String log = "[DEBUG] [%s]: %s".formatted(callerInfo, formattedMessage);
        me.trouper.alias.Alias.getInstance().getLogger().info(log);

        for (Player operator : Bukkit.getOnlinePlayers()) {
            if (operator.isOp()) operator.sendMessage("§d§l%s §7[§bDEBUG§7] §7[§e%s§7] §8» §7%s"
                    .formatted(main.config().messages.pluginName,callerInfo, formattedMessage));
        }
    }
}
