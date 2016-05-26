package fr.alx.midi.radiator;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

/**
 * Created with love by alexandrehebert on 26/05/2016.
 */
public class DeviceDiscovery {

    public static MidiDevice getMidiDevice(String inOut) {
        MidiDevice device;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info deviceInfo : infos) {
            try {
                device = MidiSystem.getMidiDevice(deviceInfo);
                if (device.getClass().getSimpleName().equals("Midi" + inOut + "Device")) {
                    return device;
                }
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("device not found");
    }

}
