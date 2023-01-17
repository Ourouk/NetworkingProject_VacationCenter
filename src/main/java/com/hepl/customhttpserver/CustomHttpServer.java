/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.hepl.customhttpserver;

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
            int port = 8090;
            http_listening_thread listener_thread = new http_listening_thread(port);
            System.out.println("The server url is http://127.0.0.1:"+port);
            listener_thread.run();

        }
    }
}
