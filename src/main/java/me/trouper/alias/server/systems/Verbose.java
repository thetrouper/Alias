package me.trouper.alias.server.systems;

import me.trouper.alias.server.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Verbose implements Main {

    /**
     * A dynamic verbose system which uses the format from the {@link Text} system.
     * @param backtrace The number of calls up the stacktrace to go.
     * @param verbose A message with 0 indexed curly brace placeholders. {0}, {1}, {2}...
     * @param args Qualified placeholder values.
     */
    public static void send(int backtrace, String verbose, Object... args) {
        if (!main.getCommon().getDebugMode()) return;
        String callerInfo = "Unknown Caller";

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 2 + backtrace) {
            StackTraceElement caller = stackTrace[2 + backtrace];

            String className = caller.getClassName();
            className = className.substring(className.lastIndexOf(".") + 1);
            if (className.contains("-")) callerInfo = "Protected";
            else callerInfo = className + "." + caller.getMethodName();


            if (main.getCommon().getDebuggerExclusions().contains(callerInfo)) return;
        }

        Component message = Text.format(Text.Pallet.INFO,verbose,args);
        message = Text.format(Text.Pallet.INFO,Component.text("{0} [DEBUG ^ {1}] [{2}] » {3}"),Component.text(main.getCommon().getPluginName()), Component.text(backtrace), Component.text(callerInfo), message);

        main.getPlugin().getComponentLogger().info(message);

        for (Player operator : Bukkit.getOnlinePlayers()) {
            if (!operator.isOp()) continue;
            operator.sendMessage(message);
        }
    }

    /**
     * A dynamic verbose system which uses the format from the {@link Text} system.
     * @param verbose A message with 0 indexed curly brace placeholders. {0}, {1}, {2}...
     * @param args Qualified placeholder values.
     */
    public static void send(String verbose, Object... args) {
        send(1,verbose,args);
    }
}
