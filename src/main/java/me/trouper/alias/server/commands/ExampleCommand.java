package me.trouper.alias.server.commands;

import me.trouper.alias.utils.command.Args;
import me.trouper.alias.utils.command.CommandRegistry;
import me.trouper.alias.utils.command.QuickCommand;
import me.trouper.alias.utils.command.completions.CompletionBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@CommandRegistry(value = "example")
public class ExampleCommand implements QuickCommand {

    @Override
    public void dispatchCommand(CommandSender sender, Command command, String label, Args args) {
        success(sender,"Hello, World!");
    }

    @Override
    public void dispatchCompletions(CommandSender sender, Command command, String label, CompletionBuilder b) {

    }
}
