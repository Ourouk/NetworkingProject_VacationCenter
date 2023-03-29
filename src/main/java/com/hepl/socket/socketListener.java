/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.socket;
import com.hepl.customhttpserver.httpClientHandlerThread;
import com.hepl.socket.crypto.ssl.secureSocketListener;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Andrea
 */
public class socketListener implements Runnable{
    private ServerSocket serv_socket;
    private int port;
    private int pool_size;
    
    public socketListener(int port, int poolsize)
    {
        //Init the debug logger
        this.port = port;
        this.pool_size = poolsize;

        Logger logger = Logger.getLogger(secureSocketListener.class.getName());
        try {
            logger.addHandler(new FileHandler("logs/" + "socket_listener" + ".log"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void run() {
       ExecutorService executor_reader = Executors.newFixedThreadPool(pool_size);
        try {
            serv_socket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(socketListener.class.getName()).log(Level.SEVERE, null, ex);
        }
       while(true)
       {
           //Launch New thread for each connection
           try {
               httpClientHandlerThread reading_thread = new httpClientHandlerThread(serv_socket.accept());
               executor_reader.execute(reading_thread);
           } catch (IOException ex) {
               Logger.getLogger(socketListener.class.getName()).log(Level.SEVERE, null, ex);
           }
       }
    }
}
