/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.hepl.customhttpserver;

import com.hepl.customhttpserver.crypto.ssl.https_listener_thread;

/**
 *
 * @author Andrea
 */
public class CustomWebServer {

    public static void main(String[] args) {
        System.out.println("Launching Custom Http Server made in Java");
        //It is to note that the listener can launch itself some thread
        int port = 8090;
        int ssl_port = 8091;

        http_listening_thread listener_thread = new http_listening_thread(port);
        //This code launch both socket to receive connection
        Thread http_listening_thread = new Thread(listener_thread);
        http_listening_thread.start();
        System.out.println("The server url is http://127.0.0.1:"+port);

        https_listener_thread  ssllistener_thread = new https_listener_thread(ssl_port);
        Thread https_listener_thread = new Thread(ssllistener_thread);
        https_listener_thread.start();

        System.out.println("The secured server url is https://127.0.0.1:"+ssl_port);
    }
}
