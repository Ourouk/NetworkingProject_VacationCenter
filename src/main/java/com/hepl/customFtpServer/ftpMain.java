package com.hepl.customFtpServer;

import com.hepl.socket.crypto.ssl.secureSocketListener;
import com.hepl.socket.socketListener;

public class ftpMain implements Runnable{
    @Override
    public void run() {
        System.out.println("Launching Custom FTP Server made in Java");
        //It is to note that the listener can launch itself some thread
        int listen_port = 21;
        int data_port = 20;
        int ssl_listen_port = 990;
        int ssl_data_port = 989;

        socketListener listener_thread = new socketListener(listen_port,4,socketListener.availableHandler.ftpClientHandlerThread);
        //This code launch both socket to receive connection
        Thread http_listening_thread = new Thread(listener_thread);
        http_listening_thread.start();
        System.out.println("The server url is ftp://127.0.0.1:"+listen_port);

        secureSocketListener SSLlistener_thread = new secureSocketListener(ssl_listen_port,4,false,secureSocketListener.availableHandler.ftpClientHandlerThread);
        Thread https_listener_thread = new Thread(SSLlistener_thread);
        https_listener_thread.start();

        System.out.println("The secured server url is ftp://127.0.0.1:"+ssl_listen_port);
    }

}
