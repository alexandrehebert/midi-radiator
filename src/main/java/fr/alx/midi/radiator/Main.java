package fr.alx.midi.radiator;

import fr.alx.midi.radiator.mixtrack.*;
import fr.alx.midi.radiator.protocol.DeviceConnector;
import fr.alx.midi.radiator.protocol.DeviceDiscovery;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Optional;

import static java.util.Arrays.asList;

/**
 * Created with love by alexandrehebert on 23/05/2016.
 */
public class Main {

    public static void main(String[] args) throws Throwable {

        KeyboardWriter keyboard = KeyboardWriter.create();

        try (DeviceConnector deviceConnector = DeviceConnector.connect(
                DeviceDiscovery.getMidiDevice("In"),
                DeviceDiscovery.getMidiDevice("Out")
        )) {

            MixTrackDevice device = new MixTrackDevice(deviceConnector);

            device.stream()
                    .forEach((MixTrackMessage message) -> {
                        switch (message.control) {
                            case PITCH:
                                Optional.of(message)
                                        .map(MixTrackMessage::getControl)
                                        // .filter(MixTrackControl.PITCH::equals)
                                        .map((control) -> MixTrackPadColor.fromValue(Math.floor((message.value / 127.) * 16.)))
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
                                        );
                                break;
                            case VOLUME_1:
                                // setOutputVolume((message.value / 127.f) * 7.f);
                                break;
                        }
                    });

        }

    }

    /**
     * Mac specific code
     *
     * @deprecated
     * @param value
     */
    @Deprecated
    public static void setOutputVolume(float value) {
        String command = "set volume " + value;
        try {
            ProcessBuilder pb = new ProcessBuilder("osascript","-e",command);
            pb.directory(new File("/usr/bin"));
            Process p = pb.start();
            p.waitFor();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
