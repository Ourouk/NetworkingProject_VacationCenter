    /*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
     */
    package com.hepl.customHttpServer;
    import com.hepl.socket.crypto.ssl.secureSocketListener;

    import java.io.IOException;
    import java.net.*;
    import java.io.*;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.util.HashMap;

    import java.util.Map;
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



        //Default files
        static final File DIR_ROOT = new File("www");
        static final File DEFAULT_FILE = new File(DIR_ROOT.getAbsolutePath(),"index.html");
        static final File FILE_NOT_FOUND = new File(DIR_ROOT.getAbsolutePath(),"404.html");
        static final File METHOD_NOT_SUPPORTED = new File(DIR_ROOT.getAbsolutePath(),"not_supported.html");

        //Http request parsing variables
        private String httpMethod;
        private String httpPath;
        private String httpVersion;
        private String httpParts[];
        private String requestLine[];
        public String requestBody;





        private Map<String,String> httpHeaders = new HashMap<String,String>();



        public httpClientHandlerThread(Socket s)
        {
            Logger logger = Logger.getLogger(secureSocketListener.class.getName());
            try {
                logger.addHandler(new FileHandler("logs/" + "https_server_thread" + ".log"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if(DIR_ROOT.mkdir())
            {
                ConsoleLogging("Created Directory Containing HTML files");
            }
            this.socket = s;
        }
        @Override
        public void run() {
            ConsoleLogging("Http Thread Initialised : " + socket.getInetAddress().getHostAddress() + ':' + socket.getPort());
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new DataOutputStream(socket.getOutputStream());

                //Read the whole content
                StringBuilder sb = new StringBuilder();
                String inputLine; int emptyline =0;
                while ((inputLine = input.readLine()) != null) {
                    sb.append(inputLine);
                    sb.append("\r\n");
                    if (inputLine.isEmpty()) {
                            break;
                    }
                }
                String httpCommand = sb.toString();

                // Parse the HTTP command
                httpParts = httpCommand.split("\r\n");
                requestLine = httpParts[0].split("\\s+");
                httpMethod = requestLine[0];
                httpPath = requestLine[1];
                httpVersion = requestLine[2];
                httpHeaders = new HashMap<>();
                for (int i = 1; i < httpParts.length - 1; i++) {
                    String[] header = httpParts[i].split(":\\s+");
                    httpHeaders.put(header[0], header[1]);
                }
                if(httpMethod.equals("POST"))
                {
                    StringBuilder sb_body = new StringBuilder();
                    try {
                        int numChars = Integer.parseInt(httpHeaders.get("Content-Length"));
                        char[] buff = new char[numChars];
                        int numRead = 0;
                        while (numRead < numChars) {
                            int count = input.read(buff, numRead, numChars - numRead);
                            numRead += count;
                        }
                        requestBody = new String(buff);
                    }catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else {
                    requestBody = "";
                }

                ConsoleLogging("Receive Command : \n" + httpCommand);
                ConsoleLogging("Receive POST Body : \n" + requestBody);
                switch (httpMethod) {
                    case "GET" -> this.GEThandler();
//                    case "HEAD" -> this.HEADhandler();
                    case "POST" -> this.POSThandler();
                    default -> this.NOTSUPPORTEDdhandler();
                }
                output.flush();
                socket.close();
            }catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(httpClientHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        public void GEThandler() throws FileNotFoundException, IOException, SocketException
        {
            ConsoleLogging("Receive GET Command by " + socket.getInetAddress().getHostAddress() + " on \"" +  httpPath + "\"");
            httpSmartHttpHandler smartHttp;
            smartHttp = new httpSmartHttpHandler(this,pathHandler());
            httpResponseBuilder response = new httpResponseBuilder(smartHttp.getFileContent(), smartHttp.content_type);
            response.send(output);
        }
//        public void HEADhandler() throws IOException
//        {
//            httpSmartHttpHandler smartHttp = new httpSmartHttpHandler(pathHandler().toPath());
//            httpResponseBuilder response = new httpResponseBuilder(smartHttp.getFileContent());
//            response.send(output);
//        }
        public void POSThandler() throws IOException
        {
            ConsoleLogging("Receive POST Command by " + socket.getInetAddress().getHostAddress() + " on \"" +  httpPath + "\"");
            httpSmartHttpHandler smartHttp = new httpSmartHttpHandler(this,pathHandler());
            httpResponseBuilder response = new httpResponseBuilder(smartHttp.getFileContent(), smartHttp.content_type);
            response.send(output);
        }
        public void NOTSUPPORTEDdhandler() throws IOException
        {
            httpResponseBuilder response = new httpResponseBuilder(Files.readAllBytes(Paths.get(METHOD_NOT_SUPPORTED.getAbsolutePath())));
            try {
                response.send(output);
            }catch (Exception ex)
            {
                Logger.getLogger(httpClientHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         *
         * @return
         * @throws IOException
         */
        private Path pathHandler() throws IOException
        {
                Path file_path;
                if(!this.httpPath.equals("/"))
                    file_path = Paths.get(DIR_ROOT.getAbsolutePath(),httpPath);
                else
                     file_path = DEFAULT_FILE.toPath();
                return file_path;
        }
        public void ConsoleLogging(String log)
        {
            System.out.println("Thread " + Thread.currentThread().getId() + " at " +
                    java.time.LocalDateTime.now().getHour()+":"+java.time.LocalDateTime.now().getMinute()+ ":"+
                            java.time.LocalDateTime.now().getSecond() + " : " +  log);
        }
    }