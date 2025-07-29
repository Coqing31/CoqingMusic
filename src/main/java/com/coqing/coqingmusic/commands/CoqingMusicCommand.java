package com.coqing.coqingmusic.commands;

import com.coqing.coqingutils.commands.PluginCommand;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class CoqingMusicCommand implements PluginCommand {

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("coqingmusic");
    }

    @Override
    public String description() {
        return "The root command of CoqingMusic.";
    }

}
