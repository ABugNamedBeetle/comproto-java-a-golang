package com.arpit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSLServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSLServer.class);

    public static void main(String[] args) {
        try {
            X509KeyManager x509KeyManager = getKeyManager();
            X509TrustManager x509TrustManager = getTrustManager();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[] { x509KeyManager }, new TrustManager[] { x509TrustManager }, null);

            SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(5001);

            serverSocket.setNeedClientAuth(false);
            serverSocket.setEnabledProtocols(new String[] { "TLSv1.2" });
            serverSocket.setEnabledCipherSuites(new String[]{
                // "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                // "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            });
            
            LOGGER.info("SSL Socket Server Started");
            

            while (true) {
                try {
                    
                    // LOGGER.info("cipher supported"+ List.of(serverSocket.getSupportedCipherSuites()).stream().collect(Collectors.joining(",\n")));
                    // LOGGER.info("cipher enabled"+ List.of(serverSocket.getEnabledCipherSuites()).stream().collect(Collectors.joining(",\n")));
                    
                    LOGGER.info("Server Listening on " + 5001);
                    SSLSocket socket = (SSLSocket) serverSocket.accept();
                    socket.startHandshake();
                    LOGGER.info("Server accepted client " + 5001);
                    LOGGER.info("Cipher selected "+ socket.getSession().getCipherSuite());
                    
                    // InputStream and OutputStream Stuff
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {

                        LOGGER.info("Data recevied from client: " + inputLine);
                        out.println(inputLine);
                    }
                } catch (Exception e) {
                    LOGGER.error("Connection ended : " + e.getLocalizedMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.info("Server Completed");
    }

    private static X509TrustManager getTrustManager() throws Exception {
        // TrustManagerFactory
        String trustfile = Objects.requireNonNull(System.getProperty("ssltruststore"), "TrustStore is required") ;
        
        String password2 = "1234";
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX", "SunJSSE");
        InputStream inputStream1 = ClassLoader.getSystemClassLoader().getResourceAsStream(trustfile);
        trustStore.load(inputStream1, password2.toCharArray());
        trustManagerFactory.init(trustStore);
        X509TrustManager x509TrustManager = null;
        for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                x509TrustManager = (X509TrustManager) trustManager;
                break;
            }
        }

        if (x509TrustManager == null)
            throw new NullPointerException();
        return x509TrustManager;
    }

    private static X509KeyManager getKeyManager() throws Exception {

        String keyfile = Objects.requireNonNull(System.getProperty("sslkeystore"), "KeyStore is required") ;
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        String password = "1234";
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(keyfile);
        keyStore.load(inputStream, password.toCharArray());

        // craete the key manager
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
        keyManagerFactory.init(keyStore, password.toCharArray());
        X509KeyManager x509KeyManager = null;
        for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
            if (keyManager instanceof X509KeyManager) {
                x509KeyManager = (X509KeyManager) keyManager;
                break;
            }
        }
        if (x509KeyManager == null)
            throw new NullPointerException();

        return x509KeyManager;

    }
}
