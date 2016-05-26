package fr.alx.midi.radiator.mixtrack;

import fr.alx.midi.radiator.protocol.DeviceConnector;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import java.util.stream.Stream;

/**
 * Created with love by alexandrehebert on 26/05/2016.
 */
public class MixTrackDevice {

    DeviceConnector device;

    public MixTrackDevice(DeviceConnector device) {
        this.device = device;
    }

    public MixTrackDevice send(MixTrackControl led, int channel, int value) {
        try {
            this.device.writer.send(new ShortMessage(
                    ShortMessage.NOTE_ON, channel,
                    led.value, value
            ));
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        return this;
    }

    public MixTrackDevice switchOn(MixTrackControl led, MixTrackChannel channel, MixTrackPadColor color) {
        return send(led, channel.value, color.value);
    }

    public MixTrackDevice switchOff(MixTrackControl led, MixTrackChannel channel) {
        return send(led, channel.value, MixTrackPadColor.OFF.value);
    }

    public Stream<MixTrackMessage> stream() {
        return this.device.reader.stream()
                .map(MixTrackMessage::fromMidiMessage)
                .peek(System.out::println);
    }

}
