package com.hepl.customFtpServer;

import com.hepl.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 *  This class is used to handle the data transfer between the server and the client
 */
public class ftpClientDataTransferHandlerThread implements Runnable{


    private final ftpClientControlHandlerThread controller;
    private Socket socket;
    private int timeoutMillis = 30000;

    public ftpClientDataTransferHandlerThread(ftpClientControlHandlerThread ftpClientControlHandlerThread) {
        this.controller = ftpClientControlHandlerThread;
    }

    public enum commandType {STOR,RETR,NLST,PORT,PASV}
    public enum transferType {ASCII,BINARY,WAITING}
    private transferType currentTransferType;

    @Override
    public void run() {
        while(true) {
            synchronized (this.controller.dataThreadLock){
                switch (controller.currentCommand) {
                    case STOR, RETR -> currentTransferType = transferType.BINARY;
                    case NLST -> currentTransferType = transferType.ASCII;
                    case PORT, PASV -> {
                        try {
                            if(this.socket !=  null && this.socket.isConnected())
                                socket.close();
                            this.socket = createDataConnection();
                            controller.dataThreadLock.notify();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        currentTransferType = transferType.WAITING;
                    }
                    default -> currentTransferType = transferType.WAITING;
                }


                if (currentTransferType.equals(transferType.WAITING)) {
                    try {
                        this.controller.dataThreadLock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    sendControllerMsgToClient("150 Opening data connection for file transfer");
                    if(socket == null || socket.isClosed()) {
                        try {
                            this.socket = createDataConnection();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (socket != null && socket.isConnected()) {
                        switch (controller.currentCommand) {
                            case STOR -> storFile(pathHandler());
                            case RETR -> retrFile(pathHandler());
                            case NLST -> nlstHandler();
                            default -> throw new RuntimeException("Unexpected value: " + controller.currentCommand);
                        }
                    } else {
                        sendControllerMsgToClient("425 No data connection was established");
                    }
                    try {
                        socket.close();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        }
    }

    private void sendControllerMsgToClient(String s) {
        synchronized (controller.dataThreadLock) {
            controller.sendMsgToClient(s);
            try {
                controller.dataThreadLock.wait();
                wait(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private File pathHandler() {
        try {
            File file;
            if(this.controller.currentCommentParameters[1].getBytes()[1] == '/')
            {
                file = new File(controller.rootDirectory + File.separator + this.controller.currentCommentParameters[1]);
            }else {
                file = new File(controller.rootDirectory + File.separator + controller.currDirectory + File.separator + this.controller.currentCommentParameters[1]);
            }
            return file;
        }catch (NullPointerException e)
        {
            sendControllerMsgToClient("501 Syntax error in parameters or arguments.");
            throw new RuntimeException("501 Syntax error in parameters or arguments.");
        }
    }
    private Socket createDataConnection() throws IOException {
    Socket socket = null;
    switch (controller.currentTransferConnectionTypeStatus) {
        case ACTIVE -> socket = createActiveDataConnection();
        case PASSIVE -> socket = createPassiveDataConnection();
    }
    return socket;
    }
    //Init socket for data transfer
    private Socket createActiveDataConnection() throws IOException {
        Socket socket = null;
        switch (controller.currentCryptStatus) {
            case PLAIN ->{
                socket = createPlainActiveDataConnection();
            }
            case TLS -> {
                socket = createSecureActiveDataConnection();
            }
        }
        return socket;
    }
    private Socket createPlainActiveDataConnection() throws IOException {
        Socket socket;
        String[] buff = actvMessageParsing();
        socket = new Socket(buff[0],Integer.parseInt(buff[1]));
        return socket;
    }

    private Socket createPlainPassiveDataConnection() throws IOException {
        Socket socket;
        try (ServerSocket serverSocket = new ServerSocket(controller.dataPassivePort)) {
            serverSocket.setSoTimeout(timeoutMillis); // Set the timeout for accepting connections
            sendControllerMsgToClient(this.pasvMessageParsing());
            socket = serverSocket.accept();
        }
        return socket;
    }

    private Socket createPassiveDataConnection() throws IOException {
        Socket socket = null;
        switch (controller.currentCryptStatus) {
            case PLAIN -> socket = createPlainPassiveDataConnection();
            case TLS -> socket = createSecurePassiveDataConnection();
        }

        return socket;
    }

    /**
     * Init a listening socket and then handle the connection reusing the server certificate
     * @return Socket
     */
    private Socket createSecureActiveDataConnection() throws IOException {
        Socket socket;
        String[] buff = actvMessageParsing();
        socket = controller.sslContext.getSocketFactory().createSocket(buff[0],Integer.parseInt(buff[1]));
        return socket;
    }

    /**
     * Try to connect to the client and then handle connection
     * @return Socket
     */
    private Socket createSecurePassiveDataConnection() throws IOException {
        Socket socket;
        try (ServerSocket serverSocket = controller.sslContext.getServerSocketFactory().createServerSocket(controller.dataPassivePort)) {
            serverSocket.setSoTimeout(timeoutMillis);
            sendControllerMsgToClient(this.pasvMessageParsing());
            socket = serverSocket.accept();
        }
        return socket;
    }
    //endregion
    //region Interaction with file system
    private void storFile(File file)
    {
        Logger.log("Storing file" + file.getName());
        if(file.isFile()) {
            byte[] buffer = new byte[8192];
            int read_lenght;
            try {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream file_out = new DataOutputStream(new FileOutputStream(file));
                while ((read_lenght = input.read(buffer, 0, buffer.length)) != -1) {
                    file_out.write(buffer, 0, read_lenght);
                }
                file_out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sendControllerMsgToClient("226 Closing data connection. stor file action successful");
        }
    }
    private void retrFile(File file)
    {
        Logger.log("Retrieving file: " + file.getAbsolutePath());
        if(file.isFile()) {
            byte[] buffer = new byte[8192];
            int read_lenght;
            try {
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                DataInputStream file_in = new DataInputStream(new FileInputStream(file));
                while ((read_lenght = file_in.read(buffer, 0, buffer.length)) != -1) {
                    output.write(buffer, 0, read_lenght);
                }
                file_in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sendControllerMsgToClient("226 Closing data connection. retr file action successful");
        }
    }
    private void nlstHandler() {
        Logger.log("Listing Files :");
        File[] files;
        if(this.controller.currentCommentParameters.length == 1) {
            files = nlstHelper(null);
        } else {
            files = nlstHelper(this.controller.currentCommentParameters[1]);
        }
        if (files == null) {
            sendControllerMsgToClient("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
        } else {
            try {
                BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream(), 1024);
                //Refactoring based on https://forum.filezilla-project.org/viewtopic.php?t=18556
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d HH:mm", Locale.ENGLISH);
                for (File file : files) {
                    if(file.isDirectory()) {
                        sendDataMsgToClient( controller.getCurrentUsersRightsPosix() + "\t" + "1" +
                                controller.getCurrentUser() + "\t" + controller.getCurrentUser() + "\t"+"4096"+ "\t" +
                                dateFormat.format(new Date(file.lastModified())) + "\t" + file.getName(), output);
                    }
                    else {
                        sendDataMsgToClient( controller.getCurrentUsersRightsPosix() + "\t" + "1" + "\t" +
                                controller.getCurrentUser() + "\t" + controller.getCurrentUser() + "\t"+file.length()+ "\t" +
                                dateFormat.format(new Date(file.lastModified())) + "\t" + file.getName(), output);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sendControllerMsgToClient("226 Closing data connection. LIST/NLST file action successful");
        }
    }

    // Extracted from https://github.com/pReya/ftpServer/blob/master/src/ftpServer/Worker.java
    // Return a list of files from a path String
    private File[] nlstHelper(String args) {
        // Construct the name of the directory to list.
        String filename = controller.rootDirectory+controller.currDirectory;
        if (args != null) {
            filename = filename + File.separator + args;
        }
        // Now get a File object, and see if the name we got exists and is a
        // directory.
        File f = new File(filename);

        if (f.exists() && f.isDirectory()) {
            return f.listFiles();
        } else if (f.exists() && f.isFile()) {
            File[] f_list = new File[1];
            f_list[0] = f;
            return f_list;
        } else {
            return null;
        }
    }
    //endregion
    private void sendDataMsgToClient(String msg,BufferedOutputStream dataOutWriter) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(dataOutWriter)));
        Logger.log(msg);
        bufferedWriter.write(msg);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }
//endregion
    private String pasvMessageParsing()
    {
        String myIp = controller.s.getLocalAddress().getHostAddress(); //TODO handle IPV6
        String[] myIpSplit = myIp.split("\\.");
        int p1 = controller.dataPassivePort / 256;
        int p2 = controller.dataPassivePort % 256;
        return("227 Entering Passive Mode (" + myIpSplit[0] + "," + myIpSplit[1] + "," + myIpSplit[2] + ","
                + myIpSplit[3] + "," + p1 + "," + p2 + ")");
    }
    private String[] actvMessageParsing()
    {
        String[] parts = this.controller.currentCommentParameters[1].split(",");
        String ipAddress = String.join(".", parts[0], parts[1], parts[2], parts[3]);
        int port = Integer.parseInt(parts[4]) * 256 + Integer.parseInt(parts[5]);
        return new String[]{ipAddress, String.valueOf(port)};
    }
}
