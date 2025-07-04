package me.trouper.alias.server.commands;

import me.trouper.alias.server.ContextAware;
import me.trouper.alias.server.commands.completions.CompletionBuilder;
import me.trouper.alias.server.commands.completions.CompletionNode;
import net.kyori.adventure.text.Component;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface QuickCommand extends TabExecutor, ContextAware {

    void handleCommand(CommandSender sender, Command command, String label, Args args);
    void handleCompletion(CommandSender sender, Command command, String label, Args args, CompletionBuilder b);

    default void register() {
        CommandRegistry registry = this.getClass().getAnnotation(CommandRegistry.class);
        PluginCommand command = getPlugin().getCommand(registry.value());

        if (command != null) {
            getPlugin().getLogger().info("Registering Command from " + this.getClass().getSimpleName());
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        CommandRegistry registry = this.getClass().getAnnotation(CommandRegistry.class);
        if (registry == null) {
            return true;
        }
        switch (sender) {
            case Player player when !registry.playersAllowed() -> {
                error(sender, Component.text("Players are not allowed to run this command!"));
                return true;
            }
            case ConsoleCommandSender consoleCommandSender when !registry.consoleAllowed() -> {
                error(sender, Component.text("This command cannot be ran from the console!"));
                return true;
            }
            case BlockCommandSender blockCommandSender when !registry.blocksAllowed() -> {
                error(sender, Component.text("This command cannot be ran from a command block!"));
                return true;
            }
            default -> {
            }
        }

        try {
            String perm = registry.permission().value();
            if (perm != null && !perm.isEmpty() && !sender.hasPermission(perm)) {
                error(sender, Component.text(registry.permission().message()));
                return true;
            }
            handleCommand(sender, command, label, new Args(args));
        }
        catch (Exception ex) {
            if (registry.printStackTrace()) {
                ex.printStackTrace();
            }
            error(sender, Component.text("Correct Usage: {0}"),Component.text(registry.usage()));
        }
        return true;
    }

    @Override
    default @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        try {
            CompletionBuilder b = new CompletionBuilder(label);
            handleCompletion(sender, command, label, new Args(args),b);
            CompletionNode node = b.getRootNode();

            if (args.length == 0) {
                return node.getOptions();
            }
            for (int i = 0; i < args.length - 1; i++) {
                node = node.next(args[i]);
            }

            String end = args[args.length - 1];
            List<String> a = new ArrayList<>(node.getOptions());

            if (node.isOptionsRegex()) {
                List<String> regexResult = new ArrayList<>();
                for (CompletionNode option : node.getNextOptions()) {
                    boolean regexMatches = CompletionNode.containsRegex(option, end) || end.isEmpty();
                    for (String s : option.getValues())
                        regexResult.add((regexMatches ? "§d" : "§c") + s + "§r");
                }
                return regexResult;
            }
            else {
                a.removeIf(s -> !s.toLowerCase().contains(end.toLowerCase()));
                return a;
            }
        }
        catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    default CommandRegistry getRegistry() {
        return this.getClass().getAnnotation(CommandRegistry.class);
    }

    default void disable() {
        CommandRegistry registry = this.getClass().getAnnotation(CommandRegistry.class);
        PluginCommand command = getPlugin().getCommand(registry.value());

        if (command != null) {
            command.setExecutor((sender, command1, label, args) -> true);
            command.setTabCompleter((sender, command2, label, args) -> List.of());
        }
    }
}
