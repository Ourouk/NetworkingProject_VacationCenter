    /*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
     */
    package com.hepl.customhttpserver;
    import com.hepl.socket.crypto.ssl.secureSocketListener;

    import java.io.IOException;
    import java.net.*;
    import java.io.*;
    import java.nio.file.Files;
    import java.nio.file.Paths;
    import java.util.HashMap;
    import java.util.ArrayList;

    import java.util.List;
    import java.util.logging.FileHandler;
    import java.util.logging.Level;
    import java.util.logging.Logger;
    /**
     *
     * @author Andrea
     */
    public class httpClientHandlerThread implements Runnable{
        private Socket socket = null;
        private BufferedReader input = null;
        private DataOutputStream output = null;

        static final File WEB_ROOT = new File("www");
        static final String DEFAULT_FILE = "index.html";
        static final String FILE_NOT_FOUND = "404.html";
        static final String METHOD_NOT_SUPPORTED = "not_supported.html";

        public httpClientHandlerThread(Socket s)
        {
            if(WEB_ROOT.mkdir())
            {
                System.out.println("Created Directory Containing HTML files");
            }
            System.out.println("New http/https request Received");
            this.socket = s;
        }
        @Override
        public void run() {

            Logger logger = Logger.getLogger(secureSocketListener.class.getName());
            try {
                logger.addHandler(new FileHandler("logs/" + "https_server_thread" + ".log"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

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
            }catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(httpClientHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        public void GEThandler(String[] command, List<String> header_list ,  List<String> body_list, DataOutputStream out) throws FileNotFoundException, IOException, SocketException
        {
            String[] split_PathAndQuery = command[1].split("\\?");
            httpSmartHttpHandler smarthttp;
            //TODO Improve that it trigger me
            if(split_PathAndQuery[0].contentEquals("/"))
            {
                smarthttp = new httpSmartHttpHandler(Paths.get(WEB_ROOT.getPath(),DEFAULT_FILE));
                httpResponseBuilder response = new httpResponseBuilder(smarthttp.getFileContent());
                response.send(out);
            }else
            {
                if(Files.isReadable(Paths.get(WEB_ROOT.getPath(),split_PathAndQuery[0])))
                {
                    smarthttp = new httpSmartHttpHandler(Paths.get(WEB_ROOT.getPath(),split_PathAndQuery[0]));
                    httpResponseBuilder response = new httpResponseBuilder(smarthttp.getFileContent());
                    try {
                        response.send(out);
                    }catch (Exception ex)
                    {
                        Logger.getLogger(httpClientHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }else
                {
                    smarthttp = new httpSmartHttpHandler(Paths.get(WEB_ROOT.getPath(),FILE_NOT_FOUND));
                    httpResponseBuilder response = new httpResponseBuilder(smarthttp.getFileContent());
                    try {
                        response.send(out);
                    }catch (Exception ex)
                    {
                        Logger.getLogger(httpClientHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
            System.out.println(body_list);
            for (String line: body_list) {
                System.out.println(line);
                String[] split_key = line.split("=");
                form_hashmap.put(split_key[0], split_key[1]);
            }
            //Manage the return pages
            httpSmartHttpHandler smartHttp;
            String[] split_PathAndQuery = command[1].split("\\?"); //Give the destination
            //TODO Improve that it trigger me
            if(split_PathAndQuery[0].contentEquals("/"))
            {   
                smartHttp = new httpSmartHttpHandler(Paths.get(WEB_ROOT.getPath(),DEFAULT_FILE));
                httpResponseBuilder response = new httpResponseBuilder(smartHttp.getFileContent());
                response.send(out);
            }else
            {
                if(Files.isReadable(Paths.get(WEB_ROOT.getPath(),split_PathAndQuery[0])))
                {
                    smartHttp = new httpSmartHttpHandler(Paths.get(WEB_ROOT.getPath(),split_PathAndQuery[0]));
                    smartHttp.setParsedParameters(form_hashmap);
                    httpResponseBuilder response = new httpResponseBuilder(smartHttp.getFileContent());
                    response.send(out);
                }else
                {
                    smartHttp = new httpSmartHttpHandler(Paths.get(WEB_ROOT.getPath(),FILE_NOT_FOUND));
                    httpResponseBuilder response = new httpResponseBuilder(smartHttp.getFileContent());
                    response.send(out);
                }
            }
        }
        public void NOTSUPPORTEDdhandler(String[] command,List<String> header_list ,  List<String> body_list,DataOutputStream out) throws IOException
        {
            httpResponseBuilder response = new httpResponseBuilder(Files.readAllBytes(Paths.get(WEB_ROOT.getPath(), METHOD_NOT_SUPPORTED)));
            try {
                response.send(out);
            }catch (Exception ex)
            {
                Logger.getLogger(httpClientHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }