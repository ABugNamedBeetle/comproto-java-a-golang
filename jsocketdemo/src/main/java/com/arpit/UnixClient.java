package com.arpit;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnixClient {
    public static final Logger LOGGER = LoggerFactory.getLogger(UnixClient.class);

    public static void main(String[] args)
            throws IOException, InterruptedException {

        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.clear(); // pointer is set to zero, but data is no erased
        buffer.put("ap".getBytes());
        LOGGER.info("ap".getBytes().toString());
        buffer.flip();
        for (var b : buffer.array()) {
            LOGGER.info("" + String.valueOf(b));
        }
        LOGGER.info("BUffLen " + buffer.array().length);

        Path socketFile = Path
                .of(System.getProperty("user.home"))
                .resolve("sample");
        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketFile);

        SocketChannel channel = SocketChannel
                .open(StandardProtocolFamily.UNIX);
        channel.connect(address);

        if (channel.isConnected()) {
            // start reading from server
            Thread.ofVirtual().start(() -> {
                ByteBuffer inBuffer = ByteBuffer.allocate(1024);
                StringBuilder messageBuilder = new StringBuilder();

                try {
                    while (true) {

                        LOGGER.info("prep reading");
                        int bytesRead;
                        bytesRead = channel.read(inBuffer);
                        // blocked unitl it reads aleast 1 byte, vo read karke hi
                        // manega
                        LOGGER.info("read " + bytesRead);
                        if (bytesRead < 0)
                            continue;

                        inBuffer.flip();
                        while (inBuffer.hasRemaining()) {
                            char c = (char) inBuffer.get();
                            if (c == '\n') { // Assuming you're using a newline as a delimiter
                                String message = messageBuilder.toString();
                                LOGGER.info("<-- received " + message);
                                messageBuilder.setLength(0); // Reset the builder for the next message
                            } else {
                                messageBuilder.append(c);
                            }
                        }
                        inBuffer.clear();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            });
            ByteBuffer outBuffer = ByteBuffer.allocate(1024);
            while (true) {
                sendPingMessages(channel, outBuffer);
            }
        }

    }

    private static void sendPingMessages(SocketChannel channel, ByteBuffer outBuffer) throws IOException {
        String msg = "Hello Server, Time : " + LocalTime.now().toString();
        outBuffer.clear();
        outBuffer.put((msg + "\n").getBytes());
        outBuffer.flip();
        int a = channel.write(outBuffer);
        LOGGER.info("-->" + msg + " , to server, len= " + a);

    }

    // private static void writeMessageToSocket(
    // SocketChannel socketChannel, String message)
    // throws IOException {
    // ByteBuffer buffer = ByteBuffer.allocate(1024);
    // buffer.clear(); // pointer is set to zero, but data is no erased
    // buffer.put(message.getBytes());
    // buffer.flip(); // If we did not flip() it, the returned ByteBuffer would be
    // empty because the
    // // position would be equal to the limit.
    // // it is similar to reset pointer to start, from here the next operation will
    // be
    // // done
    // while (buffer.hasRemaining()) {
    // int a = socketChannel.write(buffer);
    // LOGGER.info("BytesWriten " + a);
    // }
    // }
}
