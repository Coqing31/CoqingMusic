package com.coqing.coqingmusic.commands.coqingmusic;

import org.bukkit.entity.Player;

import com.coqing.coqingmusic.guis.GUIFactory;
import com.coqing.coqingutils.commands.CommandUtils;
import com.coqing.coqingutils.commands.PluginCommand;
import com.google.inject.Inject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class PlaySongCommand implements PluginCommand {

    private final CommandUtils command;
    private final GUIFactory factory;

    @Inject
    private PlaySongCommand(CommandUtils command, GUIFactory factory) {
        this.command = command;
        this.factory = factory;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("play")
                .requires(
                        s -> s.getSender().hasPermission("coqingmusic.command.play") && s.getSender() instanceof Player)
                .executes(this::execute);
    }

    @Override
    public String description() {
        return "Open a GUI to play a song.";
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) command.getExecutorOrSender(ctx);
        this.factory.createPlayGUI(player).openGUI();

        return 1;
    }

}
