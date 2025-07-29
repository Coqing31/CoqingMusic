# CoqingMusic

A simple, yet effective music player plugin.

## What is this plugin?

This plugin can play music to your players in the following formats:

- NBS (OpenNBS and old NBS)
- MIDI (All types supported)
- MCSP & MCSP2 (Minecraft Song Planner, AKA the very old NBS)
- TXT (From BleachHack)
- Notebot (from Future Client)

However, this plugin CANNOT play audio formats like MP3, WAV, FLAC, etc. Here is why:

The formats supported are not audio files, but rather give instructions to the software
playing those files which notes to play at which time. For example: NBS says that at tick number 300,
play a pling sound with pitch 1.5, volume 0.8 and panning 80% right. But, MP3 files do not contain instructions,
but rather is a baked audio file which simply has audio waveforms and NOT instructions like NBS does.

## How to install?

1. Click on the latest release.
2. Download the plugin from the release.
3. Install the plugin, and you should be done! The plugin does not come with
any songs pre-loaded, so you need to get some songs yourself by heading to
<https://noteblock.world> or searching for MIDI files online (a good site is BitMidi).

## How to use?

First, make sure your songs are loaded. If you have downloaded songs to your
server and placed them in `plugins/CoqingMusic/songs`, make sure to run `/cqm reload`
to reload the songs.

Then, run `/cqm play` to open the songs GUI, and click on the song you want to play.
The song will be played to all online players on the server. If you would like to mute the music,
simply mute the "Jukebox/Note Blocks" volume category in your volume settings.

## Transposing vs. 10 Octaves

In Minecraft, there is only a 2 octave range, meaning only a limited set of notes can be played.
There are 2 solutions: first is transposing, which repositions the notes outside of that octave
range to be within the range, so it can be playable within Minecraft. The second option is
to apply a custom resource pack that adds more notes.

Transposing is active by default and will always be used, but the custom resource pack
can be activated from CoqingMusic. CoqingMusic can either provide the pack itself (it will
call `Player#addResourcePack()` and not `setResourcePack()` to not override existing packs),
or if you are already providing a resource pack to your players, you can add the contents
of the pack yourself to your server resource pack.
