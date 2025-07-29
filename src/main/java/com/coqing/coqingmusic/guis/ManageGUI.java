package com.coqing.coqingmusic.guis;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.ItemStack;

import com.coqing.coqingmusic.CoqingMusic;
import com.coqing.coqingmusic.nbs.loaders.SongLoader;
import com.coqing.coqingmusic.nbs.songplayer.CoqingSongPlayer;
import com.coqing.coqingutils.ComponentUtils;
import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ManageGUI {
    private final Player player;
    private final ComponentUtils cmp;
    private final SongLoader loader;

    private Listener listener;

    public ManageGUI(Player player, ComponentUtils cmp, SongLoader loader) {
        this.player = player;
        this.cmp = cmp;
        this.loader = loader;
    }

    public void openGUI() {
        ChestGui gui = new ChestGui(1,
                ComponentHolder.of(cmp.formatMessage("<prefix> <#99d98c>Manage Song Player</#99d98c>")));

        gui.setOnGlobalClick(event -> event.setCancelled(true));
        StaticPane pane = new StaticPane(0, 0, 9, 1);

        CoqingSongPlayer songPlayer = loader.getSongPlayer();
        ItemStack stop = new ItemStack(Material.RED_CONCRETE);
        stop.setData(DataComponentTypes.CUSTOM_NAME,
                cmp.formatMessage("<!i><#99d98c><b>Stop Song Player</b></#99d98c>"));

        pane.addItem(new GuiItem(stop, event -> {
            player.closeInventory(Reason.PLUGIN);
            if (songPlayer == null) {
                cmp.sendMessage(player, "<prefix> <red>The song player is unset!</red>");
                return;
            } else if (!songPlayer.isRunning()) {
                cmp.sendMessage(player, "<prefix> <red>The song player is not playing anything!</red>");
                return;
            }

            songPlayer.stop();
            cmp.sendMessage(player, "<prefix> <green>Successfully stopped song player.</green>");
        }), 3, 0);

        Map.Entry<Material, String> pauseItem;
        if (songPlayer == null || !songPlayer.isRunning()) {
            pauseItem = Map.entry(Material.BLACK_CONCRETE, "<!i><red><b>Unavailable option.</b></red>");
        } else if (!songPlayer.isPaused()) {
            pauseItem = Map.entry(Material.LIME_CONCRETE, "<!i><#99d98c><b>Pause Song Player</b></#99d98c>");
        } else {
            pauseItem = Map.entry(Material.YELLOW_CONCRETE, "<!i><#99d98c><b>Unpause Song Player</b></#99d98c>");
        }
        ItemStack pause = new ItemStack(pauseItem.getKey());
        pause.setData(DataComponentTypes.CUSTOM_NAME,
                cmp.formatMessage(pauseItem.getValue()));

        pane.addItem(new GuiItem(pause, event -> {
            player.closeInventory(Reason.PLUGIN);
            if (songPlayer == null) {
                cmp.sendMessage(player, "<prefix> <red>The song player is unset!</red>");
                return;
            } else if (!songPlayer.isRunning()) {
                cmp.sendMessage(player, "<prefix> <red>The song player is not playing anything!</red>");
                return;
            }

            songPlayer.setPaused(!songPlayer.isPaused());
            cmp.sendMessage(player, "<prefix> <green>Successfully <pause> song player.</green>",
                    Placeholder.unparsed("pause", songPlayer.isPaused() ? "paused" : "unpaused"));
        }), 4, 0);

        ItemStack changePos = new ItemStack(Material.ORANGE_CONCRETE);
        changePos.setData(DataComponentTypes.CUSTOM_NAME,
                cmp.formatMessage("<!i><#99d98c><b>Change Song Time</b></#99d98c>"));

        pane.addItem(new GuiItem(changePos, event -> {
            player.closeInventory(Reason.PLUGIN);
            if (songPlayer == null) {
                cmp.sendMessage(player, "<prefix> <red>The song player is unset!</red>");
                return;
            } else if (!songPlayer.isRunning()) {
                cmp.sendMessage(player, "<prefix> <red>The song player is not playing anything!</red>");
                return;
            }

            cmp.sendMessage(player,
                    "<prefix> <yellow>Please enter a <u>valid tick number</u> you want to go to.<br>For reference, the current song tick is <u><tick></u>.</yellow>",
                    Placeholder.unparsed("tick", String.valueOf(songPlayer.getTick())));

            if (this.listener != null) {
                HandlerList.unregisterAll(this.listener);
                this.listener = null;
            }

            this.listener = new Listener() {
                @EventHandler(priority = EventPriority.HIGHEST)
                public void onChat(AsyncChatEvent event) {
                    if (event.getPlayer() != player)
                        return;

                    event.setCancelled(true);
                    int parsedInt = 0;
                    try {
                        parsedInt = Integer
                                .parseInt(PlainTextComponentSerializer.plainText().serialize(event.message()));
                    } catch (NumberFormatException ex) {
                        cmp.sendMessage(player,
                                "<prefix> <red>The provided input is invalid.</red><br><yellow>Please enter a <u>valid tick number</u> you want to go to.<br>For reference, the current song tick is <u><tick></u>.</yellow>",
                                Placeholder.unparsed("tick", String.valueOf(songPlayer.getTick())));
                        return;
                    }

                    if (parsedInt < 0 || parsedInt > songPlayer.getCurrentTicksPerSecond()
                            * songPlayer.getSong().getLengthInMilliseconds()) {
                        cmp.sendMessage(player,
                                "<prefix> <red>The provided tick is higher or lower than the limit.</red><br><yellow>Please enter a <u>valid tick number</u> you want to go to.<br>For reference, the current song tick is <u><tick></u>.</yellow>",
                                Placeholder.unparsed("tick", String.valueOf(songPlayer.getTick())));
                        return;
                    }

                    if (songPlayer == null || !songPlayer.isRunning()) {
                        cmp.sendMessage(player,
                                "<prefix> <red>Seems like that song player is no longer playing! Oops...</red>");
                        HandlerList.unregisterAll(this);
                        return;
                    }

                    songPlayer.setTick(parsedInt);
                    cmp.sendMessage(player,
                            "<prefix> <green>Successfully set the song player tick to <u><tick></u>.</green>",
                            Placeholder.unparsed("tick", String.valueOf(parsedInt)));
                    HandlerList.unregisterAll(this);
                }
            };
            Bukkit.getPluginManager().registerEvents(this.listener, CoqingMusic.get());
        }), 5, 0);

        gui.addPane(pane);

        gui.show(this.player);
    }

}
