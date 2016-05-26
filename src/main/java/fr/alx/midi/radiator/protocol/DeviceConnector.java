package fr.alx.midi.radiator.protocol;

import javax.sound.midi.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

public class DeviceConnector implements Closeable {

    public final Reader reader;
    public final Writer writer;

    private DeviceConnector(Reader reader, Writer writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public static DeviceConnector connect(MidiDevice input, MidiDevice output) throws MidiUnavailableException, IOException {
        Reader reader = null;
        Writer writer = null;
        try {
            reader = Reader.open(input);
            writer = Writer.open(output);
        } catch (MidiUnavailableException e) {
            if (reader != null) {
                reader.close();
            }
        }
        return new DeviceConnector(reader, writer);
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
        this.reader.close();
    }

    public static class Writer implements Closeable {

        MidiDevice device;
        Receiver receiver;

        private Writer(MidiDevice device, Receiver receiver) {
            this.device = device;
            this.receiver = receiver;
        }

        public static Writer open(MidiDevice device) throws MidiUnavailableException {
            device.open();
            return new Writer(device, device.getReceiver());
        }

        public void send(MidiMessage message) {
//            try {
            this.receiver.send(message, System.currentTimeMillis());
//            } catch (IllegalStateException e) {
//                this.close();
//            }
        }

        @Override
        public void close() throws IOException {
            if (this.device.isOpen()) {
                System.out.println("writer closed");
                this.device.close();
            }
        }

    }

    public static class Reader implements Closeable {

        final MidiDevice device;
        Transmitter transmitter;
        Receiver receiver;
        BlockingQueue<MidiMessage> messages = new LinkedBlockingQueue<>(1000);
        Stream<Optional<MidiMessage>> messageStream = null;

        private Reader(MidiDevice device, Transmitter transmitter) {
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
                    if (messageStream != null) {
                        Reader.this.messageStream.close();
                    }
                }
            };
            this.transmitter.setReceiver(receiver);
        }

        public static Reader open(MidiDevice device) throws MidiUnavailableException {
            device.open();
            return new Reader(device, device.getTransmitter());
        }

        public Stream<MidiMessage> stream() {
            // return this.messages.stream();
            messageStream = Stream.generate(() -> {
                try {
                    return Optional.of(this.messages.take());
                } catch (InterruptedException e) {
                    System.out.println("stream interrupted");
                    return Optional.empty();
                }
            });
            messageStream.onClose(() -> {
                try {
                    this.close();
                } catch (Exception e) {
                }
            });
            return messageStream.map((m) -> {
                if (!m.isPresent()) messageStream.close();
                return m.get();
            });
        }

        @Override
        public void close() throws IOException {
            if (this.device.isOpen()) {
                System.out.println("reader closed");
                this.device.close();
            }
        }

    }

}
