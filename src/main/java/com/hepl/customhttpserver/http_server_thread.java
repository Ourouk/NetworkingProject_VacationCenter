/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.customhttpserver;
import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Andrea
 */
public class http_server_thread implements Runnable{
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream output = null;
    
    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    
    http_server_thread(Socket s)
    {
        this.socket = s;
    }
    @Override
    public void run() {
        try {
            input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            String s,fileRequested;
            while((s = input.readLine()) != null)
            {
               StringTokenizer parse = new StringTokenizer(s);
               String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
               fileRequested = parse.nextToken().toLowerCase(); // we get file requested
               switch(method)
               {
                   case "GET":
                       this.GEThandler(fileRequested);
                       break;
                    case "HEAD":
                        this.HEADhandler(fileRequested);
                       break;
                   case "POST":
                       this.POSThandler(fileRequested);
                       break;
                   default:
                       break;
               }
               this.output = new PrintWriter(this.socket.getOutputStream());
            }
        } catch (IOException ex) {
            Logger.getLogger(http_server_thread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void GEThandler(String command)
    {
        
    }
    public void HEADhandler(String command)
    {
        
    }
    public void POSThandler(String command)
    {
        
    }
}
