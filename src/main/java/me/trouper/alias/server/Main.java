package me.trouper.alias.server;

import io.papermc.paper.registry.RegistryAccess;
import me.trouper.alias.data.IO;
import me.trouper.alias.data.io.Config;
import me.trouper.alias.data.io.Storage;
import me.trouper.alias.utils.Text;
import org.bukkit.command.CommandSender;

import java.util.Random;
import java.util.function.BooleanSupplier;

public interface Main {
    Main main = new Main() {};
  
    Random random = new Random();

    default RegistryAccess getRegistryAccess() {
      return RegistryAccess.registryAccess();
    }

    default me.trouper.alias.Alias getPlugin() {
        return me.trouper.alias.Alias.getInstance();
    }
    
    default Manager man() {
        return getPlugin().getManager();
    }
    
    default IO io() {
        return man().io;
    };
    
    default Config config() {
        return io().config;
    }
    
    default Storage storage() {
        return io().storage;
    }
    
    default void info(CommandSender player, String message, Object... args) {
        Text.sendMessage(Text.Pallet.INFO, player, message, args);
    }
    
    default void error(CommandSender player, String message, Object... args) {
        Text.sendMessage(Text.Pallet.ERROR, player, message, args);
    }
    
    default void warning(CommandSender player, String message, Object... args) {
            Text.sendMessage(Text.Pallet.WARNING, player, message, args);
    }

    default void success(CommandSender player, String message, Object... args) {
            Text.sendMessage(Text.Pallet.SUCCESS, player, message, args);
    }

    default void message(CommandSender player, String message, Object... args) {
            Text.sendMessage(Text.Pallet.NEUTRAL, player, message, args);
    }

    default void checkPre(boolean check, String msg, Object... args) {
        if (!check) {
            throw new IllegalArgumentException(msg.formatted(args));
        }
    }

    default void checkPre(BooleanSupplier check, String msg, Object... args) {
        checkPre(check.getAsBoolean(), msg, args);
    }
}
