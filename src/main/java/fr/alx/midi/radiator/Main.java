package fr.alx.midi.radiator;

import fr.alx.midi.radiator.mixtrack.*;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Arrays.asList;

/**
 * Created with love by alexandrehebert on 23/05/2016.
 */
public class Main {

    public static void main(String[] args) throws Throwable {

        try (DeviceConnector deviceConnector = DeviceConnector.connect(
                DeviceDiscovery.getMidiDevice("In"),
                DeviceDiscovery.getMidiDevice("Out")
        )) {

            MixTrackDevice device = new MixTrackDevice(deviceConnector);

            device.stream()
                    .forEach((MixTrackMessage message) -> Optional.of(message)
                            .map(MixTrackMessage::getControl)
                            .filter(MixTrackControl.PITCH::equals)
                            .map((m) -> MixTrackPadColor.fromValue(Math.floor((message.value / 127.) * 16.)))
                            .ifPresent((color) -> asList(MixTrackChannel.BLUE, MixTrackChannel.RED)
                                    .forEach((channel) -> device
                                            .switchOn(MixTrackControl.FX1, channel, color)
                                            .switchOn(MixTrackControl.FX2, channel, color)
                                            .switchOn(MixTrackControl.FX3, channel, color)
                                            .switchOn(MixTrackControl.FX4_TAP, channel, color)
                                            .switchOn(MixTrackControl.LOOP_IN, channel, color)
                                            .switchOn(MixTrackControl.LOOP_OUT, channel, color)
                                            .switchOn(MixTrackControl.LOOP_2X, channel, color)
                                            .switchOn(MixTrackControl.RELOOP, channel, color)
                                    )
                            )
                    );

        }

    }

}
