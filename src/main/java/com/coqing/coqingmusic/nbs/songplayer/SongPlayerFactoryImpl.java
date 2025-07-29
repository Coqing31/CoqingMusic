package com.coqing.coqingmusic.nbs.songplayer;

import com.coqing.coqingutils.ComponentUtils;
import com.coqing.coqingutils.ConfigUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

import net.raphimc.noteblocklib.model.Song;

public class SongPlayerFactoryImpl implements SongPlayerFactory {
    private final Provider<ConfigUtils> configProvider;
    private final Provider<ComponentUtils> componentProvider;

    @Inject
    private SongPlayerFactoryImpl(Provider<ConfigUtils> configProvider, Provider<ComponentUtils> componentProvider) {
        this.configProvider = configProvider;
        this.componentProvider = componentProvider;
    }

    @Override
    public CoqingSongPlayer create(Song song) {
        return new CoqingSongPlayer(song, configProvider.get(), componentProvider.get());
    }

}
