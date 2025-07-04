package me.trouper.alias.server;

import me.trouper.alias.AliasContext;
import me.trouper.alias.AliasContextProvider;
import me.trouper.alias.data.Common;
import me.trouper.alias.data.DataManager;
import me.trouper.alias.server.systems.Text;
import me.trouper.alias.server.systems.Verbose;
import me.trouper.alias.server.systems.display.DisplayManager;
import me.trouper.alias.utils.misc.Randomizer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public interface ContextAware {

    /**
     * Get the plugin class this component belongs to.
     * Easiest thing to avoid repeating this method is to make a "PluginContext" interface and fill out this method.
     */
    Class<? extends JavaPlugin> getPluginClass();

    default AliasContext getContext() {
        return AliasContextProvider.getContext(getPluginClass());
    }

    default JavaPlugin getPlugin() {
        return getContext().getPlugin();
    }

    default Common getCommon() {
        return getContext().getCommon();
    }

    default Text getTextSystem() {
        return getContext().getText();
    }

    default DisplayManager getDisplayManager() {
        return getContext().getDisplayManager();
    }

    default Verbose getVerbose() {
        return getContext().getVerbose();
    }

    default AutoRegistrar getAutoRegistrar() {
        return getContext().getAutoRegistrar();
    }

    default DataManager getDataManager() {
        return getContext().getDataManager();
    }

    default void info(Audience audience, Component message, Component... args) {
        getTextSystem().message(Text.Pallet.INFO, audience, message, args);
    }

    default void error(Audience audience, Component message, Component... args) {
        getTextSystem().message(Text.Pallet.ERROR, audience, message, args);
    }

    default void warning(Audience audience, Component message, Component... args) {
        getTextSystem().message(Text.Pallet.WARNING, audience, message, args);
    }

    default void success(Audience audience, Component message, Component... args) {
        getTextSystem().message(Text.Pallet.SUCCESS, audience, message, args);
    }

    default void message(Audience audience, Component message, Component... args) {
        getTextSystem().message(Text.Pallet.NEUTRAL, audience, message, args);
    }

    default void infoAny(Audience audience, String message, Object... args) {
        getTextSystem().messageAny(Text.Pallet.INFO, audience, message, args);
    }

    default void errorAny(Audience audience, String message, Object... args) {
        getTextSystem().messageAny(Text.Pallet.ERROR, audience, message, args);
    }

    default void warningAny(Audience audience, String message, Object... args) {
        getTextSystem().messageAny(Text.Pallet.WARNING, audience, message, args);
    }

    default void successAny(Audience audience, String message, Object... args) {
        getTextSystem().messageAny(Text.Pallet.SUCCESS, audience, message, args);
    }

    default void messageAny(Audience audience, String message, Object... args) {
        getTextSystem().messageAny(Text.Pallet.NEUTRAL, audience, message, args);
    }

    default Random random() {
        return new Random();
    }

    default Randomizer randomizer() {
        return new Randomizer();
    }
}
