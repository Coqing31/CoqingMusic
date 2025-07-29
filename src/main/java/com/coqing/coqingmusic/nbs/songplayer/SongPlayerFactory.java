package com.coqing.coqingmusic.nbs.songplayer;

import net.raphimc.noteblocklib.model.Song;

public interface SongPlayerFactory {
    CoqingSongPlayer create(Song song);
}
