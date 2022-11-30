/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.customhttpserver;
import java.io.IOException;
import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

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
    private BufferedReader input = null;
    private DataOutputStream output = null;
    
    static final File WEB_ROOT = new File("www");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    
    http_server_thread(Socket s)
    {
        if(WEB_ROOT.mkdir())
        {
            System.out.println("Created Directory Containing HTML files");
        }
        System.out.println(WEB_ROOT.getAbsolutePath());
        this.socket = s;
    }
    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String s;int i = 0;
            while(!(s = input.readLine()).isEmpty())
            {
                //TODO: Store the whole command inside a buffer
                //Initial Parsing of the request
                    System.out.println(s);
                    String[] s_splitted = s.split(" ");
                    String method = s_splitted[0].toUpperCase(); // we get the HTTP method of the client
                    String fileRequested = s_splitted[1].toLowerCase(); // we get file requested
                    String HTTPversion = s_splitted[2].toLowerCase(); //Version of the protocl
                    this.output = new DataOutputStream(this.socket.getOutputStream());
                    
                    switch(method)
                    {
                        case "GET":
                            System.out.println("GET" + " " + fileRequested);
                            this.GEThandler(s_splitted,output);
                            break;
                        case "HEAD":
                            System.out.println("Head");
                            this.HEADhandler(s_splitted,output);
                            break;
                        case "POST":
                            System.out.println("Post");
                            this.POSThandler(s_splitted,output);
                            break;
                        default:
                            System.out.println("Not Supported");
                            this.NOTSUPPORTEDdhandler(s_splitted,output);
                            break;
                    }
             i++;
            }
            output.flush();
            socket.close();
        }catch (IOException ex) {
            Logger.getLogger(http_server_thread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void GEThandler(String[] command,DataOutputStream out) throws FileNotFoundException, IOException
    {
        String[] split_PathAndQuery = command[1].split("\\?");
        http_smartHttp_server smarthttp;
        //TODO Improve that it trigger me
        if(split_PathAndQuery[0].contentEquals("/"))
        {
            smarthttp = new http_smartHttp_server(Paths.get(WEB_ROOT.getPath(),DEFAULT_FILE));
            http_response_builder response = new http_response_builder(smarthttp.getFileContent());
            response.send(out);
        }else
        {
            if(Files.isReadable(Paths.get(WEB_ROOT.getPath(),split_PathAndQuery[0])))
            {
                smarthttp = new http_smartHttp_server(Paths.get(WEB_ROOT.getPath(),split_PathAndQuery[0]));
                http_response_builder response = new http_response_builder(smarthttp.getFileContent());
                response.send(out);
            }else
            {
                smarthttp = new http_smartHttp_server(Paths.get(WEB_ROOT.getPath(),FILE_NOT_FOUND));
                http_response_builder response = new http_response_builder(smarthttp.getFileContent());
                response.send(out);
            }
        }
    }
    public void HEADhandler(String[] command,DataOutputStream out)
    {
        
    }
    public void POSThandler(String[] command,DataOutputStream out) throws IOException
    {
        //TODO parse double carriage return + = 
        for(String s: command)
        {
            System.out.println(s);
        }
        String[] split_PathAndQuery = command[1].split("\\?");
        HashMap hashmap;
        http_smartHttp_server smarthttp;
        //TODO Improve that it trigger me
        if(split_PathAndQuery[0].contentEquals("/"))
        {
            smarthttp = new http_smartHttp_server(Paths.get(WEB_ROOT.getPath(),DEFAULT_FILE));
            http_response_builder response = new http_response_builder(smarthttp.getFileContent());
            response.send(out);
        }else
        {
            if(Files.isReadable(Paths.get(WEB_ROOT.getPath(),split_PathAndQuery[0])))
            {
                smarthttp = new http_smartHttp_server(Paths.get(WEB_ROOT.getPath(),split_PathAndQuery[0]));
                http_response_builder response = new http_response_builder(smarthttp.getFileContent());
                response.send(out);
            }else
            {
                smarthttp = new http_smartHttp_server(Paths.get(WEB_ROOT.getPath(),FILE_NOT_FOUND));
                http_response_builder response = new http_response_builder(smarthttp.getFileContent());
                response.send(out);
            }
        }
    }
    public void NOTSUPPORTEDdhandler(String[] command,DataOutputStream out) throws IOException
    {
        http_response_builder http_response_builder = new http_response_builder(Files.readAllBytes(Paths.get(WEB_ROOT.getPath(), METHOD_NOT_SUPPORTED)));
        http_response_builder.send(out);
    }
}