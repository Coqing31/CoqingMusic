package com.coqing.coqingmusic;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.coqing.coqingmusic.nbs.loaders.SongLoader;
import com.coqing.coqingutils.ConfigUtils;
import com.coqing.coqingutils.Utils;
import com.coqing.coqingutils.commands.CommandUtils;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public class CoqingMusic extends JavaPlugin {
    private static CoqingMusic instance;
    private static Utils utils;
    private static Injector injector;

    public Utils getUtils() {
        return utils;
    }

    public Injector getGuiceInjector() {
        return injector;
    }

    public static CoqingMusic get() {
        return instance;
    }

    private void initUtils() {
        utils = Utils.createBuilder()
                .plugin(this)
                .prefix("<gradient:#99d98c:#99e2b4><b>CoqingMusic</b></gradient> <dark_gray>»</dark_gray>")
                .debug(true)
                .build();

        injector = Guice.createInjector(new CoqingMusicModule(utils, this));
        utils.setPluginInjector(injector);

        // Init config
        injector.getInstance(ConfigUtils.class).load(this.getDataPath().resolve("config.yml"));

        // Init commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            utils.getUtil(CommandUtils.class).registerCommands(commands.registrar(),
                    "com.coqing.coqingmusic.commands");
        });
    }

    @Override
    public void onEnable() {
        getSLF4JLogger().info("CoqingMusic v{} - Made by Coqing", this.getPluginMeta().getVersion());
        instance = this;
        initUtils();

        // Load songs...
        injector.getInstance(SongLoader.class).load();

        List.of(ResourcePackLoaderEvent.class)
                .forEach(c -> Bukkit.getPluginManager().registerEvents(injector.getInstance(c), this));
    }

    @Override
    public void onDisable() {
        utils = null;
        injector = null;
        instance = null;
    }
}
