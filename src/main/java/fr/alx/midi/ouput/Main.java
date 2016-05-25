package fr.alx.midi.ouput;

import javax.sound.midi.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Stream;

enum KeyAction {

    KEY_DOWN,
    KEY_UP,
    KEY_PRESS,
    MOUSE_DOWN,
    MOUSE_UP,
    MOUSE_PRESS

}

enum PadButton {

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

    PadButton(int value) {
        this.value = value;
    }

    static PadButton fromValue(int value) {
        for (PadButton k : values()) {
            if (k.value == value) {
                return k;
            }
        }
        return UNKOWN;
    }

}

enum PadColor {

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

    PadColor(int value) {
        this.value = value;
    }

}

enum KeyMapping {

    SHIFT_DOWN(-111, 74, (v) -> v == 127, KeyEvent.VK_SHIFT, KeyAction.KEY_DOWN), // -111 74 127
    SHIFT_UP(-111, 74, (v) -> v == 1, KeyEvent.VK_SHIFT, KeyAction.KEY_UP),
    ALT_DOWN(-110, 64, (v) -> v == 127, KeyEvent.VK_ALT, KeyAction.KEY_DOWN),
    ALT_UP(-110, 64, (v) -> v == 0, KeyEvent.VK_ALT, KeyAction.KEY_UP),
    LEFT(-79, 37, (v) -> v > 63, KeyEvent.VK_LEFT, KeyAction.KEY_PRESS),
    RIGHT(-79, 37, (v) -> v <= 63, KeyEvent.VK_RIGHT, KeyAction.KEY_PRESS),
    UP(-78, 37, (v) -> v == 1, KeyEvent.VK_UP, KeyAction.KEY_PRESS),
    DOWN(-78, 37, (v) -> v == 127, KeyEvent.VK_DOWN, KeyAction.KEY_PRESS),
    PAGE_UP(-78, 37, (v) -> v <= 63, KeyEvent.VK_PAGE_UP, KeyAction.KEY_PRESS),
    PAGE_DOWN(-78, 37, (v) -> v > 63, KeyEvent.VK_PAGE_DOWN, KeyAction.KEY_PRESS);

    final int a, b;
    final int code;
    final Predicate<Byte> value;
    final KeyAction action;

    KeyMapping(int a, int b, Predicate<Byte> value, int code, KeyAction action) {
        this.a = a;
        this.b = b;
        this.value = value;
        this.code = code;
        this.action = action;
    }

    static Optional<KeyMapping> fromMidiMessage(MidiMessage midiMessage) {
        byte[] message = midiMessage.getMessage();
        for (KeyMapping k : values()) {
            if (k.a == message[0] && k.b == message[1] && k.value.test(message[2])) {
                return Optional.of(k);
            }
        }
        return Optional.empty();
    }

}

/**
 * Created with love by alexandrehebert on 23/05/2016.
 */
public class Main {

    public static void main(String[] args) throws Throwable {

        try (MidiDeviceMessageReader deviceMessageReader = MidiDeviceMessageReader.open(getMidiDevice("In"))) {
            try (MidiDeviceMessageWriter deviceMessageWriter = MidiDeviceMessageWriter.open(getMidiDevice("Out"))) {

                KeyboardWriter keyboardWriter = KeyboardWriter.open();

                deviceMessageReader.stream()
                        .peek((MidiMessage message) -> System.out.println(message.getStatus()
                                + " - " + DatatypeConverter.printHexBinary(message.getMessage())
                                + " - " + Arrays.toString(message.getMessage())
                                + " - " + PadButton.fromValue(message.getMessage()[1]).name()))
                        .peek((MidiMessage message) -> {
                            if (PadButton.fromValue(message.getMessage()[1]) == PadButton.PITCH) {
                                int color = (int) Math.floor((message.getMessage()[2] / 127.) * 16.);
                                deviceMessageWriter.send(PadButton.FX1, 0x01, color);
                                deviceMessageWriter.send(PadButton.FX2, 0x01, color);
                                deviceMessageWriter.send(PadButton.FX3, 0x01, color);
                                deviceMessageWriter.send(PadButton.FX4_TAP, 0x01, color);
                            }
                        })
                        .map(KeyMapping::fromMidiMessage)
                        .filter(Optional::isPresent)
                        .forEach((k) -> keyboardWriter.write(k.get()));

            }
        }

    }

    private static MidiDevice getMidiDevice(String inOut) {
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

class KeyboardWriter {

    Robot robot;

    private KeyboardWriter(Robot robot) {
        this.robot = robot;
    }

    public static KeyboardWriter open() throws AWTException {
        return new KeyboardWriter(new Robot());
    }

    public void write(KeyMapping key) {
        switch (key.action) {
            case KEY_PRESS:
                robot.keyPress(key.code);
                robot.keyRelease(key.code);
                break;
            case KEY_UP:
                robot.keyRelease(key.code);
                break;
            case KEY_DOWN:
                robot.keyPress(key.code);
                break;
            case MOUSE_PRESS:
                robot.mousePress(key.code);
                robot.mouseRelease(key.code);
                break;
            case MOUSE_UP:
                robot.mouseRelease(key.code);
                break;
            case MOUSE_DOWN:
                robot.mousePress(key.code);
                break;
        }
    }

}

class MidiDeviceMessageWriter implements Closeable {

    MidiDevice device;
    Receiver receiver;

    private MidiDeviceMessageWriter(MidiDevice device, Receiver receiver) {
        this.device = device;
        this.receiver = receiver;
    }

    public static MidiDeviceMessageWriter open(MidiDevice device) throws MidiUnavailableException {
        device.open();
        return new MidiDeviceMessageWriter(device, device.getReceiver());
    }

    public void send(PadButton led, int channel, int value) {
        try {
            this.receiver.send(
                    new ShortMessage(
                            ShortMessage.NOTE_ON, channel,
                            led.value, value
                    ),
                    System.currentTimeMillis()
            );
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        System.out.println("writer closed");
        this.device.close();
    }

}

class MidiDeviceMessageReader implements Closeable {

    MidiDevice device;
    Transmitter transmitter;
    Receiver receiver;
    BlockingQueue<MidiMessage> messages = new LinkedBlockingQueue<>(1000);

    private MidiDeviceMessageReader(MidiDevice device, Transmitter transmitter) {
        this.device = device;
        this.transmitter = transmitter;
        this.receiver = new Receiver() {
            public void send(MidiMessage message, long timeStamp) {
                try {
                    messages.put(message);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }

            public void close() {
                System.out.println("receiver closed");
            }
        };
        this.transmitter.setReceiver(receiver);
    }

    public static MidiDeviceMessageReader open(MidiDevice device) throws MidiUnavailableException {
        device.open();
        return new MidiDeviceMessageReader(device, device.getTransmitter());
    }

    public Stream<MidiMessage> stream() {
        // return this.messages.stream();
        Stream<Optional<MidiMessage>> messageStream = Stream.generate(() -> {
            try {
                return Optional.of(this.messages.take());
            } catch (InterruptedException e) {
                System.out.println("stream interrupted");
                return Optional.empty();
            }
        });
        return messageStream.map((m) -> {
            if (!m.isPresent()) messageStream.close();
            return m.get();
        });
    }

    @Override
    public void close() throws IOException {
        System.out.println("reader closed");
        this.device.close();
    }

}
