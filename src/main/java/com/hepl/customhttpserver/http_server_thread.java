    /*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
     */
    package com.hepl.customhttpserver;
    import java.io.IOException;
    import java.net.*;
    import java.io.*;
    import java.nio.CharBuffer;
    import java.nio.file.Files;
    import java.nio.file.Paths;
    import java.util.Arrays;
    import java.util.HashMap;
    import java.util.ArrayList;

    import java.util.List;
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
                output = new DataOutputStream(socket.getOutputStream());
                int i = 0;
                String header = "",body ="",line = null,content = "";

                List<String> header_list =  new ArrayList<String>(),body_list=new ArrayList<String>();
                Boolean isbody = false;
                //Read the whole content
                while ((line = input.readLine()) != null && !line.isEmpty()) {
                    header_list.add(line);
                }
                if(input.ready()) {
                    while ((line = input.readLine()) != null && !line.isEmpty()) {
                        body_list.add(line);
                    }
                }
                //when the whole command is read we go to appropriate functions
                String[] s_splitted = header_list.get(0).split(" ");
                switch(s_splitted[0])
                {
                    case "GET":
                        this.GEThandler(s_splitted,header_list,body_list,output);
                        break;
                    case "HEAD":
                        this.HEADhandler(s_splitted,header_list,body_list,output);
                        break;
                    case "POST":
                        this.POSThandler(s_splitted,header_list,body_list,output);
                        break;
                    default:
                        this.NOTSUPPORTEDdhandler(s_splitted,header_list,body_list,output);
                        break;
                }
                output.flush();
                socket.close();
            }catch (IOException ex) {
                ex.printStackTrace();
                Logger.getLogger(http_server_thread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        public void GEThandler(String[] command, List<String> header_list ,  List<String> body_list, DataOutputStream out) throws FileNotFoundException, IOException
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
        public void HEADhandler(String[] command,List<String> header_list ,  List<String> body_list,DataOutputStream out) throws IOException {
            this.GEThandler(command,header_list,body_list,out);
        }
        public void POSThandler(String[] command,List<String> header_list ,  List<String> body_list,DataOutputStream out) throws IOException
        {
            //Parsing form return
            HashMap form_hashmap = new HashMap();
            for (String line: body_list) {
                String[] split_key = line.split("=");
                form_hashmap.put(split_key[0], split_key[1]);
            }
            //Manage the return pages
            http_smartHttp_server smartHttp;
            String[] split_PathAndQuery = command[1].split("\\?"); //Give the destination
            //TODO Improve that it trigger me
            if(split_PathAndQuery[0].contentEquals("/"))
            {   
                smartHttp = new http_smartHttp_server(Paths.get(WEB_ROOT.getPath(),DEFAULT_FILE));
                http_response_builder response = new http_response_builder(smartHttp.getFileContent());
                response.send(out);
            }else
            {
                if(Files.isReadable(Paths.get(WEB_ROOT.getPath(),split_PathAndQuery[0])))
                {
                    smartHttp = new http_smartHttp_server(Paths.get(WEB_ROOT.getPath(),split_PathAndQuery[0]));
                    smartHttp.setParsedParameters(form_hashmap);
                    http_response_builder response = new http_response_builder(smartHttp.getFileContent());
                    response.send(out);
                }else
                {
                    smartHttp = new http_smartHttp_server(Paths.get(WEB_ROOT.getPath(),FILE_NOT_FOUND));
                    http_response_builder response = new http_response_builder(smartHttp.getFileContent());
                    response.send(out);
                }
            }
        }
        public void NOTSUPPORTEDdhandler(String[] command,List<String> header_list ,  List<String> body_list,DataOutputStream out) throws IOException
        {
            http_response_builder http_response_builder = new http_response_builder(Files.readAllBytes(Paths.get(WEB_ROOT.getPath(), METHOD_NOT_SUPPORTED)));
            http_response_builder.send(out);
        }
    }