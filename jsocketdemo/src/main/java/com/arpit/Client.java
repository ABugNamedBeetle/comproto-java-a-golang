package com.arpit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("localhost", 5001);
            var out = new PrintWriter(clientSocket.getOutputStream(), true);
            var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Thread.ofVirtual().start(() -> {
                try {
                    LOGGER.info("Client reader thread started; ");
                    String incoming;
                    while ((incoming = in.readLine()) != null) {
                        LOGGER.info("---> "+incoming);
                    }

                } catch (Exception e) {
                    LOGGER.error("Error in client reader", e);
                }
            });
            if (clientSocket.isConnected()) {
                LOGGER.info("client connected");
                IntStream.range(1, 100000).forEach(i -> {
                    out.write("hello world" + i + "\n");
                    out.flush();
                    try {
                        
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        LOGGER.error("in sending");
                    }

                });
            }
            Thread.sleep(1000000000);

        } catch (Exception e) {
            // TODO: handle exception
        }

    }
}
