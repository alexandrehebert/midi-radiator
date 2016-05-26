package fr.alx.midi.radiator.mixtrack;

public enum MixTrackControl {

    FILE(0x4B),
    FOLDER(0x4C),
    FX1(0x59),
    FX2(0x5A),
    FX3(0x5B),
    FX4_TAP(0x5C),
    LOOP_IN(0x53),
    LOOP_OUT(0x54),
    RELOOP(0x55),
    LOOP_2X(0x63),
    SYNC(0x40),
    PLAY_PAUSE(0x42),
    PITCH_MINUS(0x43),
    PITCH_PLUS(0x44),
    PITCH(0x0D),
    SCRATCH(0x48),
    STUTTER(0x4A),
    LOAD(0x4B),
    CUE(0x33),
    JOG_TOUCH(0x4D),
    PFL(0x47),
    SHIFT(0x61),
    UNKOWN(-1),;

    final int value;

    MixTrackControl(int value) {
        this.value = value;
    }

    static MixTrackControl fromValue(double value) {
        for (MixTrackControl k : values()) {
            if (k.value == value) {
                return k;
            }
        }
        return UNKOWN;
    }

}
