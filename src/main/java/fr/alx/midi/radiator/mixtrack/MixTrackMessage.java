package fr.alx.midi.radiator.mixtrack;

import javax.sound.midi.MidiMessage;
import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;

public final class MixTrackMessage {

    public final MixTrackControl control;
    public final MixTrackChannel channel;
    public final int value;
    private final MidiMessage originalMessage;

    private MixTrackMessage(MidiMessage message) {
        this.originalMessage = message;
        this.control = MixTrackControl.fromValue(getMessage()[1]);
        this.channel = MixTrackChannel.fromHardwareValue(getMessage()[0]);
        this.value = getMessage()[2];
    }

    public static MixTrackMessage fromMidiMessage(MidiMessage message) {
        return new MixTrackMessage(message);
    }

    public byte[] getMessage() {
        return originalMessage.getMessage();
    }

    public MixTrackControl getControl() {
        return control;
    }

    @Override
    public String toString() {
        return "MixTrackMessage( " +
                "control=" + control +
                ", channel=" + channel +
                ", value=" + value +
                ", originalMessage={" + this.originalMessage.getStatus()
                + " - " + DatatypeConverter.printHexBinary(this.originalMessage.getMessage())
                + " - " + Arrays.toString(this.originalMessage.getMessage()) + "}" +
                " )";
    }
}
