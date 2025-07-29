package com.coqing.coqingmusic.nbs.songplayer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.coqing.coqingmusic.CoqingMusic;
import com.coqing.coqingmusic.nbs.ExtendedMinecraftInstrument;
import com.coqing.coqingutils.ComponentUtils;
import com.coqing.coqingutils.ConfigUtils;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBarViewer;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.raphimc.noteblocklib.data.MinecraftInstrument;
import net.raphimc.noteblocklib.format.nbs.model.NbsCustomInstrument;
import net.raphimc.noteblocklib.model.Note;
import net.raphimc.noteblocklib.model.Song;
import net.raphimc.noteblocklib.player.SongPlayer;

public class CoqingSongPlayer extends SongPlayer {
    private static final Set<Player> players = ConcurrentHashMap.newKeySet();
    private static final DecimalFormat format = new DecimalFormat("#.##");

    private BukkitTask playerTask;
    private BossBar bossbar;

    private final ConfigUtils config;
    private final ComponentUtils cmp;

    /**
     * Do NOT use this constructor. Instead, use the factory method, since it
     * provides ConfigUtils and ComponentUtils.
     * 
     * @param song
     * @param config
     * @param cmp
     */
    public CoqingSongPlayer(Song song, ConfigUtils config, ComponentUtils cmp) {
        super(song);
        this.config = config;
        this.cmp = cmp;
    }

    @Override
    public void start(final int delay) {
        super.start(delay);

        playerTask = new BukkitRunnable() {
            private long count = 0; // Task count
            private int scrollIndex = 0;
            private DisplayStatus status = DisplayStatus.SONG;
            private List<String> scrollText;

            private final Function<Integer, String> humanLength = (length) -> String.format("%02d:%02d:%02d",
                    length / 3600,
                    (length / 60) % 60, length % 60);
            private final Function<DisplayStatus, Boolean> isAvailable = (status) -> {
                return switch (status) {
                    case SONG -> true; // Always available
                    case TITLE -> getSong().getTitle() != null && !getSong().getTitle().isBlank();
                    case AUTHOR -> getSong().getAuthor() != null && !getSong().getAuthor().isBlank();
                    case OG_AUTHOR -> getSong().getOriginalAuthor() != null && !getSong().getOriginalAuthor().isBlank();
                };
            };

            @Override
            public void run() {
                count++;
                if (count % 4 == 0) {
                    players.clear();
                    players.addAll(Bukkit.getOnlinePlayers());

                    // Handle bossbar
                    if (bossbar != null) {
                        // Remove offline players
                        var it = bossbar.viewers().iterator();
                        Set<Player> viewers = new HashSet<>();
                        while (it.hasNext()) {
                            BossBarViewer viewer = it.next();
                            if (viewer instanceof Player p) {
                                viewers.add(p);
                                if (!p.isOnline())
                                    p.hideBossBar(bossbar);
                            }
                        }

                        // Add online players who don't exist yet as viewers
                        players.forEach(player -> {
                            if (!viewers.contains(player))
                                player.showBossBar(bossbar);
                        });
                    }
                }

                Song song = getSong();

                if (bossbar == null) {
                    bossbar = BossBar.bossBar(
                            Component.text(""),
                            0,
                            BossBar.Color.GREEN,
                            BossBar.Overlay.NOTCHED_20);
                }

                // Handle scrolling
                StringBuilder sb = new StringBuilder();
                int len = 0;
                if (isPaused()) {
                    sb.append("<yellow>⏸</yellow> ");
                } else {
                    sb.append("<green>⏵</green> ");
                }
                if (this.status == DisplayStatus.SONG) {
                    if (this.scrollText == null)
                        this.scrollText = scroll(song.getFileName(), 15);
                    sb.append("<#99d98c><b>Song:</b></#99d98c> <#99e2b4>").append(this.scrollText.get(scrollIndex))
                            .append("</#99e2b4>");
                    len = song.getFileName().length();
                } else if (this.status == DisplayStatus.TITLE) {
                    if (this.scrollText == null)
                        this.scrollText = scroll(song.getTitle(), 15);
                    sb.append("<#99d98c><b>Title:</b></#99d98c> <#99e2b4>").append(this.scrollText.get(scrollIndex))
                            .append("</#99e2b4>");
                    len = song.getTitle().length();
                } else if (this.status == DisplayStatus.AUTHOR) {
                    if (this.scrollText == null)
                        this.scrollText = scroll(song.getAuthor(), 15);
                    sb.append("<#99d98c><b>Author:</b></#99d98c> <#99e2b4>").append(this.scrollText.get(scrollIndex))
                            .append("</#99e2b4>");
                    len = song.getAuthor().length();
                } else if (this.status == DisplayStatus.OG_AUTHOR) {
                    if (this.scrollText == null)
                        this.scrollText = scroll(song.getOriginalAuthor(), 15);
                    sb.append("<#99d98c><b>O.G. Author:</b></#99d98c> <#99e2b4>")
                            .append(this.scrollText.get(scrollIndex))
                            .append("</#99e2b4>");
                    len = song.getOriginalAuthor().length();
                }

                len += 15; // Add 15 to make the text go fully to the left.

                sb.append(" <gray>|<gray> <#52B69A>")
                        .append(humanLength
                                .apply((int) (getTick() / getCurrentTicksPerSecond())))
                        .append("</#52B69A><#34A0A4>/</#34A0A4><#52B69A>").append(song.getHumanReadableLength())
                        .append("</#52B69A>");

                scrollIndex++;

                if (scrollIndex >= len) {
                    scrollIndex = 0;
                    DisplayStatus current = this.status;
                    DisplayStatus next = this.status.next();

                    while (next != current) {
                        if (isAvailable.apply(next)) {
                            break;
                        }
                        next = next.next(); // next next next
                    }

                    this.status = next;
                    this.scrollText = null;
                }

                bossbar.name(cmp.formatMessage(sb.toString()))
                        .progress(getTick() / (song.getLengthInSeconds() / (1 / getCurrentTicksPerSecond())));

                if (isPaused()) {
                    bossbar.color(BossBar.Color.YELLOW);
                } else {
                    bossbar.color(BossBar.Color.GREEN);
                }

            }
        }.runTaskTimer(CoqingMusic.get(), (long) delay / 50, 5L);
    }

    enum DisplayStatus {
        SONG,
        TITLE,
        AUTHOR,
        OG_AUTHOR;

        public DisplayStatus next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

    @Override
    public void stop() {
        super.stop();
        onFinished();
    }

    @Override
    protected void onFinished() {
        if (playerTask != null && !playerTask.isCancelled())
            playerTask.cancel();
        playerTask = null;
        Bukkit.getScheduler().runTaskLater(CoqingMusic.get(), () -> {
            if (bossbar != null && bossbar.viewers() != null) {
                players.forEach(player -> player.hideBossBar(bossbar));
            }
            bossbar = null;
        }, 20L);
    }

    @Override
    protected void playNotes(List<Note> notes) {
        if (notes.isEmpty())
            return;
        ComponentUtils cmp = CoqingMusic.get().getUtils().getUtil(ComponentUtils.class);
        StringBuilder sb = new StringBuilder();
        sb.append("\n".repeat(20));
        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i).copy();
            String name = "";

            if (note.getInstrument() instanceof MinecraftInstrument mcinst) {
                name = mcinst.mcSoundName();
            } else if (note.getInstrument() instanceof NbsCustomInstrument nbsinst) {
                name = nbsinst.getName();
            } else if (note.getInstrument() instanceof ExtendedMinecraftInstrument emcinst) {
                name = emcinst.getMinecraftInstrument().mcSoundName()
                        + (emcinst.getOctaveShift() != 0 ? "_" + emcinst.getOctaveShift() : "");
            }

            Sound sound = null;
            try {
                sound = Sound.sound(Key.key(name), Sound.Source.RECORD, note.getVolume(), note.getPitch());
            } catch (InvalidKeyException ex) {
                cmp.broadcastPermission("coqingmusic.admin",
                        "<prefix> <yellow>Invalid sound <u><name></u> has attempted to be played! Tick: <tick>",
                        Placeholder.unparsed("name", name),
                        Placeholder.unparsed("tick", String.valueOf(getTick())));
                sound = Sound.sound(Key.key("block.note_block.pling"), Sound.Source.RECORD, 0, 0);
            }
            float panning = note.getPanning() * config.getRootNode().node("stereo-spacing").getFloat(2.5f);
            final Sound finalSound = sound;
            players.forEach(player -> {
                Location loc;
                if (panning == 0.0f) {
                    loc = player.getLocation();
                } else {
                    loc = stereoPan(player.getLocation(), panning);
                }

                player.playSound(finalSound, loc.getX(), loc.getY(), loc.getZ());
            });
            sb.append("<gray>").append(i).append(".</gray> <gold>S: ").append(name).append("</gold> | <gold>V: ")
                    .append(format.format(note.getVolume())).append("</gold> | <gold>N: ")
                    .append(format.format(note.getPitch()))
                    .append("</gold> | <gold>P: ").append(format.format(panning)).append("</gold>\n");
        }
        sb.append("<yellow>Tick: ").append(this.getTick()).append("/")
                .append(this.getSong().getLengthInSeconds() / (1 / this.getCurrentTicksPerSecond()))
                .append("</yellow>");
        if (config.getRootNode().node("note-debugging").getBoolean(false))
            cmp.broadcast(sb.toString());
    }

    private List<String> scroll(String text, int width) {
        List<String> frames = new ArrayList<>();

        if (text == null || text.isBlank()) {
            frames.add(String.format("%-" + width + "s", ""));
            return frames;
        }

        if (width <= 0)
            return null;

        String leadingSpaces = String.format("%" + width + "s", ""); // Creates a string of displayWidth spaces
        String trailingSpaces = String.format("%" + width + "s", ""); // Creates a string of displayWidth spaces

        String paddedText = leadingSpaces + text + trailingSpaces;

        for (int i = 0; i <= paddedText.length() - width; i++) {
            String currentFrame = paddedText.substring(i, i + width);
            frames.add(currentFrame);
        }

        return frames;
    }

    private Location stereoPan(Location location, float distance) {
        float angle = location.getYaw();
        if (angle < 0)
            angle = 180 + (180 - Math.abs(angle));

        return location.clone().add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);
    }

}
