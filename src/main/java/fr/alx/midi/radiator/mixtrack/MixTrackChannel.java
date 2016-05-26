package fr.alx.midi.radiator.mixtrack;

/**
 * Created with love by alexandrehebert on 26/05/2016.
 */
public enum MixTrackChannel {

    BLUE(0x01, -111),
    RED(0x02, -110),
    GREEN(0x03, -109),
    ORANGE(0x04, -108),
    UNKOWN(0x00, -1);

    int value, hardwareValue;

    MixTrackChannel(int value, int hardwareValue) {
        this.value = value;
        this.hardwareValue = hardwareValue;
    }

    static MixTrackChannel fromHardwareValue(double value) {
        for (MixTrackChannel c : values()) {
            if (c.hardwareValue == value) {
                return c;
            }
        }
        return UNKOWN;
    }

}
