package com.hepl.customFtpServer;

import com.hepl.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ftpClientDataTransferHandlerThread implements Runnable{

    private final ftpClientControlHandlerThread controller;
    private final String[] full_command;
    private Socket socket;

    public ftpClientDataTransferHandlerThread(ftpClientControlHandlerThread ftpClientControlHandlerThread, commandType commandType, String[] cSplitted) {
        this.controller = ftpClientControlHandlerThread;
        this.currentCommandType = commandType;
        this.full_command = cSplitted;
    }

    public enum commandType {STOR,RETR,NLST}
    public enum transferType {ASCII,BINARY}
    private final commandType currentCommandType;
    private transferType currentTransferType;

    @Override
    public void run() {
        switch(currentCommandType)
        {
            case STOR, RETR -> currentTransferType = transferType.BINARY;
            case NLST -> currentTransferType = transferType.ASCII;
        }
        this.controller.sendMsgToClient("150 Opening " + currentTransferType + " mode data transfer for " + currentCommandType + " command");
        this.socket = createDataConnection();
        if(socket != null) {
            switch (currentCommandType) {
                case STOR -> storFile(pathHandler());
                case RETR -> retrFile(pathHandler());
                case NLST -> nlstHandler();
            }
        }else {
            controller.sendMsgToClient("425 No data connection was established");
        }
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File pathHandler() {
        try {
            File file;
            if(full_command[1].getBytes()[1] == '/')
            {
                file = new File(controller.rootDirectory + File.separator + full_command[1]);
            }else {
                file = new File(controller.rootDirectory + File.separator + controller.currDirectory +
                        File.separator + full_command[1]);
            }
            return file;
        }catch (NullPointerException e)
        {
            controller.sendMsgToClient("501 Syntax error in parameters or arguments.");
            throw new RuntimeException("501 Syntax error in parameters or arguments.");
        }
    }
    private Socket createDataConnection() {
        Logger.log("Creating data connection on " + controller.dataIP + ":" + controller.dataPort);
        try {
            Socket socket = null;
            switch (controller.currentCryptStatus) {
                case PLAIN -> socket = createPlainDataConnection();
                case TLS -> socket = createSecureDataConnection();
            }
            return socket;
        }catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    //Init socket for data transfer
    private Socket createPlainDataConnection() throws IOException {
        Socket socket = null;
        switch (controller.currentTransferConnectionTypeStatus) {
            case PASSIVE -> socket = createPlainPassiveDataConnection();
            case ACTIVE -> socket = createPlainActiveDataConnection();
        }
        return socket;
    }
    private Socket createPlainActiveDataConnection() throws IOException {
        Socket socket;
        socket = new Socket(controller.dataIP,controller.dataPort);
        return socket;
    }

    private Socket createPlainPassiveDataConnection() throws IOException {
        Socket socket;
        try (ServerSocket serverSocket = new ServerSocket(controller.dataPort)) {
            socket = serverSocket.accept();
        }
        return socket;
    }

    private Socket createSecureDataConnection() throws IOException {
        Socket socket = null;
        switch (controller.currentTransferConnectionTypeStatus) {

            case PASSIVE -> socket = createSecurePassiveDataConnection();
            case ACTIVE -> socket = createSecureActiveDataConnection();
        }
        return socket;
    }

    /**
     * Init a listening socket and then handle the connection reusing the server certificate
     * @return Socket
     */
    private Socket createSecureActiveDataConnection() throws IOException {
        Socket socket;
        socket = controller.sslContext.getSocketFactory().createSocket(controller.dataIP,controller.dataPort);
        return socket;
    }

    /**
     * Try to connect to the client and then handle connection
     * @return Socket
     */
    private Socket createSecurePassiveDataConnection() throws IOException {
        Socket socket;
        try (ServerSocket serverSocket = controller.sslContext.getServerSocketFactory().createServerSocket(controller.dataPort)) {
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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            controller.sendMsgToClient("226 Closing data connection. Requested file action successful");
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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            controller.sendMsgToClient("226 Closing data connection. Requested file action successful");
        }
    }
    private void nlstHandler() {
        File[] files;
        if(full_command.length == 1) {
            files = nlstHelper(null);
        } else {
            files = nlstHelper(full_command[1]);
        }
        if (files == null) {
            controller.sendMsgToClient("550 Requested action not taken. File unavailable (e.g., file not found, no access).");
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
            controller.sendMsgToClient("226 Closing data connection. Requested file action successful");
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
        //TODO: Remove This is only usefully during debugging
        System.out.println(msg);
        dataOutWriter.write((msg + '\n').getBytes());
        dataOutWriter.flush();
    }
//endregion
}
