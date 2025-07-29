package com.coqing.coqingmusic.commands.coqingmusic;

import org.bukkit.command.CommandSender;

import com.coqing.coqingmusic.CoqingMusic;
import com.coqing.coqingmusic.nbs.loaders.SongLoader;
import com.coqing.coqingutils.ComponentUtils;
import com.coqing.coqingutils.ConfigUtils;
import com.coqing.coqingutils.commands.CommandUtils;
import com.coqing.coqingutils.commands.PluginCommand;
import com.google.inject.Inject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class LoadSongCommand implements PluginCommand {

    private final CommandUtils command;
    private final SongLoader loader;
    private final ComponentUtils component;
    private final ConfigUtils config;
    private final CoqingMusic plugin;

    @Inject
    private LoadSongCommand(CoqingMusic plugin, CommandUtils command, SongLoader loader, ComponentUtils component,
            ConfigUtils config) {
        this.command = command;
        this.plugin = plugin;
        this.loader = loader;
        this.component = component;
        this.config = config;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("reload")
                .requires(s -> s.getSender().hasPermission("coqingmusic.command.load"))
                .executes(this::execute);

    }

    @Override
    public String description() {
        return "Forces the reload of all songs and configuration. This will erase ALL fully loaded songs.";
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = command.getExecutorOrSender(ctx);
        this.component.sendMessage(sender, "<prefix> <yellow>Reloading configuration...</yellow>");
        this.config.load(this.plugin.getDataPath().resolve("config.yml"));
        this.component.sendMessage(sender, "<prefix> <yellow>Reloading all songs...</yellow>");
        this.loader.load();
        return 1;
    }

}
