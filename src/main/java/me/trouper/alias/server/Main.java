package me.trouper.alias.server;

import io.papermc.paper.registry.RegistryAccess;
import me.trouper.alias.Alias;
import me.trouper.alias.data.Common;
import me.trouper.alias.server.systems.Text;
import me.trouper.alias.utils.misc.Randomizer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public interface Main {
    Main main = new Main() {};

    default RegistryAccess getRegistryAccess() {
        return RegistryAccess.registryAccess();
    }

    default JavaPlugin getPlugin() {
        Class<? extends JavaPlugin> host = Alias.getHost();
        if (host == null) throw new RuntimeException("Alias is not enabled. Make sure to call Alias#register() in your JavaPlugin#onLoad() method!");
        return getPlugin(host);
    }

    default <T extends JavaPlugin> T getPlugin(Class<T> pluginClass) {
        return JavaPlugin.getPlugin(pluginClass);
    }

    default Common getCommon() {
        return Alias.getCommon();
    }

    default void infoAny(Audience player, String message, Object... args) {
        Text.messageAny(Text.Pallet.INFO, player, message, args);
    }

    default void errorAny(Audience player, String message, Object... args) {
        Text.messageAny(Text.Pallet.ERROR,player, message, args);
    }

    default void warningAny(Audience player, String message, Object... args) {
        Text.messageAny(Text.Pallet.WARNING, player, message, args);
    }

    default void successAny(Audience player, String message, Object... args) {
        Text.messageAny(Text.Pallet.SUCCESS, player, message, args);
    }

    default void messageAny(Audience player, String message, Object... args) {
        Text.messageAny(Text.Pallet.NEUTRAL, player, message, args);
    }

    default void info(Audience player, Component message, Component... args) {
        Text.message(Text.Pallet.INFO, player, message, args);
    }

    default void error(Audience player, Component message, Component... args) {
        Text.message(Text.Pallet.ERROR,player, message, args);
    }

    default void warning(Audience player, Component message, Component... args) {
        Text.message(Text.Pallet.WARNING, player, message, args);
    }

    default void success(Audience player, Component message, Component... args) {
        Text.message(Text.Pallet.SUCCESS, player, message, args);
    }

    default void message(Audience player, Component message, Component... args) {
        Text.message(Text.Pallet.NEUTRAL, player, message, args);
    }

    default Random random() {
        return new Random();
    }

    default Randomizer randomizer() {
        return new Randomizer();
    }

}
