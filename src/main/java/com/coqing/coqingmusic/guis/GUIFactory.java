package com.coqing.coqingmusic.guis;

import org.bukkit.entity.Player;

import com.coqing.coqingmusic.CoqingMusic;
import com.coqing.coqingmusic.nbs.loaders.SongLoader;
import com.coqing.coqingutils.ComponentUtils;
import com.google.inject.Inject;

public class GUIFactory {
    private final ComponentUtils cmp;
    private final SongLoader loader;
    private final CoqingMusic plugin;

    @Inject
    private GUIFactory(ComponentUtils cmp, SongLoader loader, CoqingMusic plugin) {
        this.cmp = cmp;
        this.loader = loader;
        this.plugin = plugin;
    }

    public PlayGUI createPlayGUI(Player player) {
        return new PlayGUI(player, cmp, loader, plugin);
    }

    public ManageGUI createManageGUI(Player player) {
        return new ManageGUI(player, cmp, loader);
    }
}
