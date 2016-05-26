package fr.alx.midi.radiator;

import javax.sound.midi.MidiMessage;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.function.Predicate;

public class KeyboardWriter {

    Robot robot;

    private KeyboardWriter(Robot robot) {
        this.robot = robot;
    }

    public static KeyboardWriter create() throws AWTException {
        return new KeyboardWriter(new Robot());
    }

    public KeyboardWriter press(int key) {
        robot.keyPress(key);
        robot.keyRelease(key);
        return this;
    }

    public KeyboardWriter down(int key) {
        robot.keyPress(key);
        return this;
    }

    public KeyboardWriter up(int key) {
        robot.keyRelease(key);
        return this;
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

    enum KeyAction {

        KEY_DOWN,
        KEY_UP,
        KEY_PRESS,
        MOUSE_DOWN,
        MOUSE_UP,
        MOUSE_PRESS

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

}
