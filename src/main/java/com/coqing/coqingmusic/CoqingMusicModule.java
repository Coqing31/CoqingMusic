package com.coqing.coqingmusic;

import com.coqing.coqingmusic.nbs.songplayer.SongPlayerFactory;
import com.coqing.coqingmusic.nbs.songplayer.SongPlayerFactoryImpl;
import com.coqing.coqingutils.InjectorModule;
import com.coqing.coqingutils.Utils;

public class CoqingMusicModule extends InjectorModule {
    private final CoqingMusic plugin;

    public CoqingMusicModule(Utils utils, CoqingMusic plugin) {
        super(utils);
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(CoqingMusic.class).toInstance(this.plugin);
        bind(SongPlayerFactory.class).to(SongPlayerFactoryImpl.class);

    }

}
