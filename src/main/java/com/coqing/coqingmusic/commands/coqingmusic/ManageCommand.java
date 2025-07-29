package com.coqing.coqingmusic.commands.coqingmusic;

import org.bukkit.entity.Player;

import com.coqing.coqingmusic.guis.GUIFactory;
import com.coqing.coqingmusic.nbs.loaders.SongLoader;
import com.coqing.coqingutils.ComponentUtils;
import com.coqing.coqingutils.commands.CommandUtils;
import com.coqing.coqingutils.commands.PluginCommand;
import com.google.inject.Inject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class ManageCommand implements PluginCommand {

    private final CommandUtils command;
    private final GUIFactory factory;
    private final SongLoader loader;
    private final ComponentUtils cmp;

    @Inject
    private ManageCommand(CommandUtils command, GUIFactory factory, SongLoader loader, ComponentUtils cmp) {
        this.command = command;
        this.factory = factory;
        this.loader = loader;
        this.cmp = cmp;
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand() {
        return Commands.literal("manage")
                .requires(s -> s.getSender() instanceof Player
                        && s.getSender().hasPermission("coqingmusic.command.manage"))
                .executes(this::execute);
    }

    @Override
    public String description() {
        return "Opens a management interface for the global song player.";
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        Player sender = (Player) command.getExecutorOrSender(ctx);
        if (this.loader.getSongPlayer() == null || !this.loader.getSongPlayer().isRunning()) {
            cmp.sendMessage(sender, "<prefix> <red>The song player is no longer active.</red>");
            return 1;
        }
        factory.createManageGUI(sender).openGUI();
        return 1;
    }

}
