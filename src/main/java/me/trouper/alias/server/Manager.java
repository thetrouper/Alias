package me.trouper.alias.server;

import me.trouper.alias.data.IO;
import me.trouper.alias.server.commands.ExampleCommand;
import me.trouper.alias.server.events.ExampleEvent;
import me.trouper.alias.server.systems.gui.RegistryListeners;
import me.trouper.alias.utils.visual.BlockDisplayRaytracer;
import org.bukkit.Bukkit;

public class Manager {
    public IO io;
    // Define backends here

    public Manager() {
        io = new IO();
        // Instantiate backends here
    }

    
    public void init() {
        io.loadAll();

        registerEvents();
        registerCommands();

        cleanup();
    }


    private void registerCommands() {
        new ExampleCommand().register();
    }

    private void registerEvents() {
        new RegistryListeners().registerEvents(); // Must be called for any GUI to work!
        new ExampleEvent().registerEvents();
    }

    private void cleanup() {
        BlockDisplayRaytracer.cleanup();
    }

}