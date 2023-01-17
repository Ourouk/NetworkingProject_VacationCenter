/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.customhttpserver;
import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Andrea
 */
public class http_listening_thread implements Runnable{
    private ServerSocket serv_socket;
    
    public http_listening_thread(int port)
    {
        try {
            serv_socket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(http_listening_thread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public void run() {
       ExecutorService executor_reader = Executors.newFixedThreadPool(4);
       while(true)
       {
           //Launch New thread for each connections
           try {
               http_server_thread reading_thread = new http_server_thread(serv_socket.accept());
               executor_reader.execute(reading_thread);
           } catch (IOException ex) {
               Logger.getLogger(http_listening_thread.class.getName()).log(Level.SEVERE, null, ex);
           }
       }
    }
}
