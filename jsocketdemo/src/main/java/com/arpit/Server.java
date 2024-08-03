package com.arpit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
    public static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        while (true) {

            try {

                if (serverSocket == null) {
                    serverSocket = new ServerSocket(5001);
                }

                LOGGER.info("Server listening on " + 5001);
                Socket clientSocket = serverSocket.accept();
                LOGGER.info("Server accepted the client " + 5001);

                // write data to socket
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // get data from the socket
                var in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {

                    LOGGER.info("Data recevied from client: " + inputLine);
                    out.println(inputLine);
                }

            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);

            }
        }
    }
}
