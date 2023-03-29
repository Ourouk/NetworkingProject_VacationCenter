package com.hepl.customhttpserver.crypto.ssl;
import com.hepl.customhttpserver.http_listening_thread;
import com.hepl.customhttpserver.http_server_thread;

import java.io.*;
import java.security.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.*;
//Note this class is there to create a listening socket in ssl, I generated
// keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -validity 365 -keystore server_keystore.jks
// keytool -exportcert -alias server -keystore server_keystore.jks -file server_certificate.cer


public class https_listener_thread implements Runnable{
    int port;
    public https_listener_thread(int port)
    {
        this.port = port;
    }
    @Override
    public void run() {

        Logger logger = Logger.getLogger(https_listener_thread.class.getName());
        logger.setLevel(Level.INFO);
        try {
            logger.addHandler(new FileHandler("logs/" + "https_listener_thread" + ".log"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            char[] password = "sYCqG5xZzpMmjrQDAWspVH2jcoQu6EDJEwgZuYEw6BEKpMyoshfQPlc1vu3J".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("ssl/server_keystore.jks");
            keyStore.load(fis, password);

            // Create the SSL context using the loaded keystore
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());

            // Create the SSL server socket and bind it to the specified port
            SSLServerSocket serverSocket;
            serverSocket = (SSLServerSocket) sslContext.getServerSocketFactory().createServerSocket(port);
            //Put this line if you want only authenticated client to be able to be connected
            //serverSocket.setNeedClientAuth(true);

    // Listening
            ExecutorService executor_reader = Executors.newFixedThreadPool(4);
            while (true) {
                http_server_thread reading_thread = new http_server_thread(serverSocket.accept());
                executor_reader.execute(reading_thread);
            }


        } catch (Exception e) {
            Logger.getLogger(http_listening_thread.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
