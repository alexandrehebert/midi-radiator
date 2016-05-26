package fr.alx.midi.radiator.mixtrack;

public enum MixTrackPadColor {

    OFF(0x00),
    RED(0x01),
    ORANGE(0x02),
    ORANGE_LIGHT(0x03),
    YELLOW(0x04),
    GREEN(0x05),
    GREEN_LIGHT(0x06),
    TURQUOISE(0x07),
    BLUE_LIGHT(0x08),
    BLUE(0x09),
    VIOLET(0x0A),
    PINK_LIGHT(0x0B),
    PINK(0x0C),
    VIOLET_XLIGHT(0x0D),
    GREEN_XLIGHT(0x0E),
    PINK_XLIGHT(0x0F),
    WHITE(0x10),;

    final int value;

    MixTrackPadColor(int value) {
        this.value = value;
    }

    public static MixTrackPadColor fromValue(double value) {
        for (MixTrackPadColor k : values()) {
            if (k.value == value) {
                return k;
            }
        }
        return OFF;
    }

}
