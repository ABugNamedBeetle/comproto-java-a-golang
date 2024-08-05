package com.arpit;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnixServer {
	public static final Logger LOGGER = LoggerFactory.getLogger(UnixServer.class);

	public static void main(String[] args)
			throws IOException, InterruptedException {
		Path socketFile = Path
				.of(System.getProperty("user.home"))
				.resolve("sample");
		System.out.println(socketFile.toAbsolutePath());
		// in case the file is left over from the last run,
		// this makes the demo more robust
		Files.deleteIfExists(socketFile);
		UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketFile);

		ServerSocketChannel serverChannel = ServerSocketChannel
				.open(StandardProtocolFamily.UNIX);
		serverChannel.bind(address);

		System.out.println("[INFO] Waiting for client to connect..." + serverChannel.isBlocking());
		SocketChannel channel = serverChannel.accept();
		System.out.println("[INFO] Client connected, " + channel.isBlocking());

		// start receiving messages

		ByteBuffer inBuffer = ByteBuffer.allocate(1024);
		StringBuilder messageBuilder = new StringBuilder();

		while (true) {

			LOGGER.info("prep reading");
			int bytesRead = channel.read(inBuffer); // blocked unitl it reads aleast 1 byte, vo read karke hi manega
			LOGGER.info("read " + bytesRead);
			if (bytesRead < 0)
				continue;

			inBuffer.flip();
			long startTime = LocalTime.now().getNano();
			byte[] bytes = new byte[bytesRead];
    		inBuffer.get(bytes);
			//for decoupling channel pipes can be used
    		messageBuilder.append(new String(bytes, StandardCharsets.UTF_8));
			

			// Process complete messages
			int delimiterIndex;
			while ((delimiterIndex = messageBuilder.indexOf("\n")) != -1) {
				// Extract the message up to the delimiter
				String message = messageBuilder.substring(0, delimiterIndex);
				LOGGER.info("message received " + message);
				channel.write(ByteBuffer.wrap(("ACK " + message + " \n").getBytes()));

				// Remove the processed message from the builder
				messageBuilder.delete(0, delimiterIndex + 1);
			}

			// while (inBuffer.hasRemaining()) {
			// char c = (char) inBuffer.get();
			// if (c == '\n') { // Assuming you're using a newline as a delimiter
			// String message = messageBuilder.toString();
			// LOGGER.info("message received " + message);
			// channel.write(ByteBuffer.wrap(("ACK "+message+" \n").getBytes() ));
			// messageBuilder.setLength(0); // Reset the builder for the next message
			// } else {
			// messageBuilder.append(c);
			// }
			// }
			LOGGER.info("Total read time= " + (LocalTime.now().getNano() - startTime) + "ns");
			inBuffer.clear();
		}

	}

	private static Optional<String> readMessageFromSocket(
			SocketChannel channel)
			throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		LOGGER.info("prep reading");

		int bytesRead = channel.read(buffer); // blocked unitl it reads aleast 1 byte, vo read karke hi manega
		LOGGER.info("read " + bytesRead);
		if (bytesRead < 0)
			return Optional.empty();

		byte[] bytes = new byte[bytesRead];
		buffer.flip();
		buffer.get(bytes);
		String message = new String(bytes);
		return Optional.of(message);
	}
}
