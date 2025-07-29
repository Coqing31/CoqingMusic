package com.coqing.coqingmusic.nbs.loaders;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.spongepowered.configurate.serialize.SerializationException;

import com.coqing.coqingmusic.CoqingMusic;
import com.coqing.coqingmusic.SongOptimizationsConfig;
import com.coqing.coqingmusic.nbs.ExtendedMinecraftInstrument;
import com.coqing.coqingmusic.nbs.songplayer.CoqingSongPlayer;
import com.coqing.coqingmusic.nbs.songplayer.SongPlayerFactory;
import com.coqing.coqingutils.ComponentUtils;
import com.coqing.coqingutils.ConfigUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.raphimc.noteblocklib.NoteBlockLib;
import net.raphimc.noteblocklib.data.MinecraftDefinitions;
import net.raphimc.noteblocklib.data.MinecraftInstrument;
import net.raphimc.noteblocklib.model.Song;

@Singleton
public class SongLoader {
    private final ComponentUtils cmp;
    private final CoqingMusic plugin;
    private final ConfigUtils config;
    private final SongPlayerFactory playerFactory;

    private final Map<SongHeader, Song> songs = new HashMap<>();
    private CoqingSongPlayer songPlayer;

    @Inject
    private SongLoader(ComponentUtils cmp, CoqingMusic plugin, ConfigUtils config, SongPlayerFactory playerFactory) {
        this.cmp = cmp;
        this.plugin = plugin;
        this.config = config;
        this.playerFactory = playerFactory;
    }

    /**
     * @return All loaded songs. Note that the song value is null if it hasn't been
     *         requested before, in the case that split loading is enabled.
     */
    public Map<SongHeader, Song> getLoadedSongs() {
        return songs;
    }

    public CoqingSongPlayer getSongPlayer() {
        return this.songPlayer;
    }

    /**
     * Loads a song fully.
     * 
     * @param path The path of the song.
     */
    public Song loadFully(SongHeader header) {
        Song fullSong;

        try {
            fullSong = NoteBlockLib.readSong(header.getSource());
        } catch (Exception ex) {
            cmp.broadcastPermission("coqingmusic.admin",
                    "<prefix> <red>An error occured while fully loading a song from <file>, visit the console for more info.</red>",
                    Placeholder.unparsed("file", header.getSource().toString()));
            plugin.getSLF4JLogger().error("An error occured while fully loading a song from {}:", header.getSource(),
                    ex);
            return null;
        }

        songs.put(header, fullSong);
        return fullSong;
    }

    public void playSong(SongHeader header) {
        Song song = songs.get(header);
        if (song == null)
            song = Objects.requireNonNull(loadFully(header)); // Will throw exception if null so not worried about that

        var opt = this.config.getRootNode().node("song-optimizations");
        if (!this.config.getRootNode().node("10-octave-pack", "enabled").getBoolean(false)) {
            // Check configuration for optimizations
            try {
                switch (opt.node("note-transposing").get(SongOptimizationsConfig.class,
                        SongOptimizationsConfig.INSTRUMENT_SHIFT)) {
                    case INSTRUMENT_SHIFT:
                        song.getNotes().forEach(MinecraftDefinitions::instrumentShiftNote);
                        song.getNotes().forEach(MinecraftDefinitions::transposeNoteKey);
                        break;
                    case TRANSPOSE:
                        song.getNotes().forEach(MinecraftDefinitions::transposeNoteKey);
                        break;
                    case CLAMP:
                        song.getNotes().forEach(MinecraftDefinitions::clampNoteKey);
                        break;
                }
            } catch (SerializationException e) {
                this.plugin.getSLF4JLogger().error("Couldn't deserialize the note transposing:", e);
            }

        } else {
            song.getNotes().forEach(note -> {
                if (note.getInstrument() instanceof MinecraftInstrument minecraftInstrument) {
                    final int octaveShift = MinecraftDefinitions.applyExtendedNotesResourcePack(note);
                    note.setInstrument(new ExtendedMinecraftInstrument(minecraftInstrument, octaveShift));
                }
            });
        }

        if (opt.node("duplicate-notes").getBoolean(true))
            song.getNotes().removeDoubleNotes();

        if (opt.node("silent-notes").getBoolean(true))
            song.getNotes().removeSilentNotes();

        if (this.songPlayer != null) {
            this.songPlayer.stop();
            this.songPlayer = null;
        }

        this.songPlayer = playerFactory.create(song);
        this.songPlayer.start(500);
    }

    /**
     * Loads all songs in the songs folder.<br>
     * NOTE: To decide whether songs should be lazy loaded, check out the
     * configuration.
     */
    public void load() {
        if (!songs.isEmpty())
            songs.clear();
        Logger logger = this.plugin.getSLF4JLogger();
        cmp.broadcastPermission("coqingmusic.admin", "<prefix> <blue>Loading all songs...</blue>");
        Path songsFolder = this.plugin.getDataPath().resolve("songs");

        if (!Files.exists(songsFolder)) {
            cmp.broadcastPermission("coqingmusic.admin",
                    "<prefix> <red>The songs folder does not exist, creating...</red>");
            try {
                Files.createDirectories(songsFolder);
            } catch (IOException ex) {
                cmp.broadcastPermission("coqingmusic.admin",
                        "<prefix> <red>Couldn't create songs directory, visit console for more info.</red>");
                logger.error("Couldn't create songs directory:", ex);
            }
            return;
        }

        boolean isSplitLoad = config.getRootNode().node("split-load").getBoolean(true);

        if (!isSplitLoad) {
            cmp.broadcastPermission("coqingmusic.admin",
                    "<prefix> <yellow>Split loading is disabled! If you have big songs, you will most likely OOM if you do not have enough memory.</yellow>");
        }

        // Walk through the file system
        try {
            Files.walkFileTree(songsFolder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
                    try {
                        Song song = NoteBlockLib.readSong(file);
                        SongHeader header = new SongHeader()
                                .setSource(file)
                                .setLength(song.getLengthInSeconds())
                                .setAuthor(song.getAuthorOr("Unset"))
                                .setOriginalAuthor(song.getOriginalAuthorOr("Unset"))
                                .setDescription(song.getDescriptionOr("Unset"))
                                .setTitle(song.getTitleOr("Unset"))
                                .setFormat(song.getFormat())
                                .setTempo(song.getTempoEvents().getTempoRange());

                        songs.put(header, isSplitLoad ? null : song);
                        cmp.broadcastPermission("coqingmusic.admin",
                                "<prefix> <green>Loaded song <u><song></u> successfully.</green>",
                                Placeholder.unparsed("song", song.getAuthorOr(song.getFileName())));
                    } catch (Exception ex) {
                        cmp.broadcastPermission("coqingmusic.admin",
                                "<prefix> <red>Couldn't load song <song>, view console for more details.</red>",
                                Placeholder.unparsed("song", file.toString()));
                        logger.error("Couldn't load song {}:", file, ex);
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException ex) {
                    logger.error("An error occured while visiting file {}:", file, ex);
                    cmp.broadcastPermission("coqingmusic.admin",
                            "<prefix> <red>An error occured while visiting file <file>, visit console for more details.");
                    return FileVisitResult.CONTINUE; // This is only a failed file.
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException ex) {
                    if (ex == null)
                        return FileVisitResult.CONTINUE;

                    logger.error("An error occured while visiting directory {}:", dir, ex);
                    return FileVisitResult.CONTINUE; // It's really not the end of the world...
                                                     // Foreshadowing....
                }
            });
        } catch (IOException e) {
            cmp.broadcastPermission("coqingmusic.admin",
                    "<prefix> <red>Couldn't walk the file tree in songs directory, visit console for more details.</red>");
            logger.error("Couldn't walk the file tree in songs directory:", e);
        }
    }

}
