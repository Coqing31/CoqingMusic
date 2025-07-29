package com.coqing.coqingmusic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;

import com.coqing.coqingutils.ComponentUtils;
import com.coqing.coqingutils.ConfigUtils;
import com.google.inject.Inject;

public class ResourcePackLoaderEvent implements Listener {
    private final UUID uuid = UUID.randomUUID();
    private final List<Player> deniedPack = new ArrayList<>();

    private final ComponentUtils cmp;
    private final ConfigUtils config;

    @Inject
    private ResourcePackLoaderEvent(ComponentUtils cmp, ConfigUtils config) {
        this.cmp = cmp;
        this.config = config;
    }

    public List<Player> getDeniedPack() {
        return this.deniedPack;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var node = this.config.getRootNode().node("10-octave-pack");
        if (!node.node("enabled").getBoolean(false) || !node.node("provide-pack").getBoolean(true))
            return;
        event.getPlayer().addResourcePack(
                uuid,
                "https://github.com/RaphiMC/NoteBlockLib/raw/refs/heads/main/Extended%20Octave%20Range%20Notes%20Pack.zip",
                hexToByte("c14fc843c44775157895fb5fba26618ef82ce214"),
                "[CoqingMusic] This server has enabled the 10 octave texture pack. Accept to download.",
                false);
    }

    private byte[] hexToByte(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    @EventHandler
    public void resourceStatus(PlayerResourcePackStatusEvent event) {
        if (event.getID() != uuid
                || !List.of(Status.DECLINED, Status.DISCARDED, Status.FAILED_DOWNLOAD, Status.FAILED_RELOAD)
                        .contains(event.getStatus()))
            return;

        deniedPack.add(event.getPlayer());
        cmp.sendMessage(event.getPlayer(),
                "<prefix> <yellow>It seems like the download has declined or failed. Songs will not play for you.</yellow>");
    }
}
