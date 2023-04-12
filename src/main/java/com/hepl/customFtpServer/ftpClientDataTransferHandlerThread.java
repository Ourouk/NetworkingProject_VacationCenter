package com.hepl.customFtpServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class ftpClientDataTransferHandlerThread implements Runnable{

    String defaultFtpPath = "ftp";
    private ftpClientControlHandlerThread controller;
    private String[] full_command;

    public ftpClientDataTransferHandlerThread(ftpClientControlHandlerThread ftpClientControlHandlerThread, commandType commandType, String[] cSplitted) {
        this.controller = ftpClientControlHandlerThread;
        this.currentCommandType = commandType;
        this.full_command = cSplitted;
    }

    public enum commandType {STOR,RETR};
    private commandType currentCommandType;

    @Override
    public void run() {
        Socket socket = createDataConnection();
        Path path  = Path.of(defaultFtpPath, full_command[1]);
        File file = new File(path.toString());
        byte[] buffer = new byte[1024];
        int read_lenght = 0;
        switch (currentCommandType)
        {
            case STOR:
                try
                {
                    BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
                    FileOutputStream file_out = new FileOutputStream(file);
                    while((read_lenght = input.read(buffer,0,1024)) != -1){
                        file_out.write(buffer,0,read_lenght);
                    }
                }catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                break;
            case RETR:
                try
                {
                    BufferedOutputStream output  = new BufferedOutputStream(socket.getOutputStream());
                    FileInputStream file_in = new FileInputStream(file);
                    while((read_lenght = file_in.read(buffer,0,1024)) != -1){
                        output.write(buffer,0,read_lenght);
                    }
                }catch(IOException e)
                {
                    throw new RuntimeException(e);
                }
                break;
        }
    }

    public enum transferType {ASCII, BINARY}


    private Socket createDataConnection() {
        try {
            Socket socket = null;
            switch (controller.currentCryptStatus) {
                case PLAIN -> {
                    socket = createPlainDataConnection();
                }
                case TLS -> {
                    socket = createSecureDataConnection();
                }
            }
            return socket;
        }catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }
    private Socket createPlainDataConnection() throws IOException {
        Socket socket = null;
        switch (controller.currentTranferConnectionTypeStatus) {
            case PASSIVE -> {
                socket = createPlainPassiveDataConnection();
            }
            case ACTIVE -> {
                socket = createPlainActiveDataConnection();
            }
        }
        return socket;
    }
    private Socket createPlainActiveDataConnection() throws IOException {
        Socket socket = null;
        socket = new Socket(controller.s.getInetAddress(),controller.s.getPort()-1);
        return socket;
    }

    private Socket createPlainPassiveDataConnection() throws IOException {
        Socket socket = null;
        ServerSocket serverSocket = new ServerSocket(controller.s.getPort()-1);
        socket = serverSocket.accept();
        return socket;
    }

    private Socket createSecureDataConnection() throws IOException {
        Socket socket = null;
        switch (controller.currentTranferConnectionTypeStatus) {

            case PASSIVE -> {
                socket = createSecurePassiveDataConnection();
            }
            case ACTIVE -> {
                socket = createSecureActiveDataConnection();
            }
        }
        return socket;
    }

    /**
     * Init a listening socket and then handle the connection reusing the server certificate
     * @return
     */
    private Socket createSecureActiveDataConnection() throws IOException {
        Socket socket = null;
        socket = controller.sslContext.getSocketFactory().createSocket(controller.s.getInetAddress(),controller.s.getPort()-1);
        return socket;
    }

    /**
     * Try to connect to the client and then handle connection
     * @return
     */
    private Socket createSecurePassiveDataConnection() throws IOException {
        Socket socket = null;
        ServerSocket serverSocket = controller.sslContext.getServerSocketFactory().createServerSocket(controller.s.getPort()-1);
        socket =  serverSocket.accept();
        return socket;
    }
    //endregion
//endregion
}
