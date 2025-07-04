package me.trouper.alias.server.systems;

import me.trouper.alias.AliasContext;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Verbose {

    private final AliasContext context;

    public Verbose(AliasContext context) {
        this.context = context;
    }

    /**
     * A Uses the format from the {@link Text} system.
     * @param backtrace The number of calls up the stacktrace to go.
     * @param verbose A message with 0 indexed curly brace placeholders. {0}, {1}, {2}...
     * @param args Qualified placeholder values.
     */
    public void send(int backtrace, String verbose, Object... args) {
        if (!context.getCommon().getDebugMode()) return;
        String callerInfo = "Unknown Caller";

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 2 + backtrace) {
            StackTraceElement caller = stackTrace[2 + backtrace];

            String className = caller.getClassName();
            className = className.substring(className.lastIndexOf(".") + 1);
            if (className.contains("-")) callerInfo = "Protected";
            else callerInfo = className + "." + caller.getMethodName();

            if (context.getCommon().getDebuggerExclusions().contains(callerInfo)) return;
        }

        Object[] processedArgs = processArgs(args);
        Component message = context.getText().format(Text.Pallet.INFO, verbose, processedArgs);
        message = context.getText().format(Text.Pallet.INFO,
                Component.text("{0} [DEBUG ^ {1}] [{2}] » {3}"),
                Component.text(context.getCommon().getPluginName()),
                Component.text(backtrace),
                Component.text(callerInfo),
                message
        );

        context.getPlugin().getComponentLogger().info(message);

        for (Player operator : Bukkit.getOnlinePlayers()) {
            if (!operator.isOp()) continue;
            operator.sendMessage(message);
        }
    }

    /**
     * Uses the format from the {@link Text} system.
     * @param verbose A message with 0 indexed curly brace placeholders. {0}, {1}, {2}...
     * @param args Qualified placeholder values.
     */
    public void send(String verbose, Object... args) {
        send(1,verbose,args);
    }

    private Object[] processArgs(Object... args) {
        Object[] processed = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof Location loc) {
                processed[i] = String.format("(%s, %.2f, %.2f, %.2f)",
                        loc.getWorld() != null ? loc.getWorld().getName() : "null",
                        loc.getX(), loc.getY(), loc.getZ());
            } else if (arg instanceof Vector vec) {
                processed[i] = String.format("(%.2f, %.2f, %.2f)",
                        vec.getX(), vec.getY(), vec.getZ());
            } else {
                processed[i] = arg;
            }
        }

        return processed;
    }
}
