package com.arpit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSLClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSLClient.class);
    public static void main(String[] args) {
        try {
            X509KeyManager x509KeyManager = getKeyManager();
            X509TrustManager x509TrustManager = getTrustManager();

            //ssl context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(new KeyManager[]{x509KeyManager}, new TrustManager[]{x509TrustManager}, null);

            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            
            SSLSocket clientSocket = (SSLSocket) socketFactory.createSocket("redbear.local", 5001);
            clientSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
            
            clientSocket.setEnabledCipherSuites(new String[]{"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"});

            clientSocket.startHandshake();
            //LOGGER.info("cipher supported"+ List.of(clientSocket.getSupportedCipherSuites()).stream().collect(Collectors.joining(",\n")));
            // LOGGER.info("cipher enabled"+ List.of(clientSocket.getEnabledCipherSuites()).stream().collect(Collectors.joining(",\n")));
                    
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


        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        LOGGER.info("* Client Completed *");
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
