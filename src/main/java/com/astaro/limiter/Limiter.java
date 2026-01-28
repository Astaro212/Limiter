package com.astaro.limiter;

import com.astaro.limiter.Listeners.SpawnListener;
import com.astaro.limiter.Logic.LimitManager;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class Limiter extends JavaPlugin {

    // Added for future (ignore it)
    private Limiter plugin;
    private LimitManager limitmanager;

    public void onEnable() {
        saveDefaultConfig();
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(new LimitCommands(this).registerCommands().build(), "Limiter", java.util.List.of("lim"));
        });

        plugin = this;
        limitmanager = new LimitManager(this);
        Bukkit.getPluginManager().registerEvents(new SpawnListener(this), this);
    }


    public LimitManager getLimitManager() {
        return this.limitmanager;
    }

}
