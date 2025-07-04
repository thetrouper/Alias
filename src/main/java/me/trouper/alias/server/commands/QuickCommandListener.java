package me.trouper.alias.server.commands;

import me.trouper.alias.server.events.QuickListener;

public interface QuickCommandListener extends QuickCommand, QuickListener {

    @Override
    default void register() {
        QuickCommand.super.register();
        QuickListener.super.register();
    }

    @Override
    default void disable() {
        QuickListener.super.unregister();
    }
}
