/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.hepl.customhttpserver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 *
 * @author Andrea
 */
public class CustomHttpServer {

    public static void main(String[] args) {
        boolean finished = false;
        while(!finished)
        {
            System.out.println("Launching Custom Http Server made in Java");
            //It is to note that the listener can launch itself some thread
            http_listening_thread listener_thread = new http_listening_thread(8080);
            ExecutorService executor_listener = Executors.newFixedThreadPool(1);
            executor_listener.execute(listener_thread);
        }
    }
}
