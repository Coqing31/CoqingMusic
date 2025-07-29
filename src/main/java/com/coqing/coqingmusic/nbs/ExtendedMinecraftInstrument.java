package com.coqing.coqingmusic.nbs;

import net.raphimc.noteblocklib.data.MinecraftInstrument;
import net.raphimc.noteblocklib.model.instrument.Instrument;

/**
 * Thanks to RK_01 (RaphiMC) for the implementation.
 */
public class ExtendedMinecraftInstrument implements Instrument {

    private final MinecraftInstrument minecraftInstrument;
    private final int octaveShift;

    public ExtendedMinecraftInstrument(final MinecraftInstrument minecraftInstrument, final int octaveShift) {
        this.minecraftInstrument = minecraftInstrument;
        this.octaveShift = octaveShift;
    }

    public MinecraftInstrument getMinecraftInstrument() {
        return this.minecraftInstrument;
    }

    public int getOctaveShift() {
        return this.octaveShift;
    }

    @Override
    public Instrument copy() {
        return new ExtendedMinecraftInstrument(this.minecraftInstrument, this.octaveShift);
    }

}
