package com.hepl.customFtpServer;


import com.hepl.Logger;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class ftpClientControlHandlerThread implements Runnable{
    public String dataIP;
    public int dataPort;

//region Properties Type
    /**
     * Status of the server
     */
    public enum transferConnectionStatus {PASSIVE,ACTIVE}
    public transferConnectionStatus currentTransferConnectionTypeStatus;

    /**
     * Indicates the authentication status of a user
     */
    private enum userStatus {LOGGED, NOLOGGING, PASSWORD}

    /**
     *  Cryptographic status
     */
    public enum cryptStatus {PLAIN,TLS}
//endregion
//region Actual Object Properties
    public cryptStatus currentCryptStatus;

    public final Socket s;
    private boolean quitCommandLoop=false;

    /**
     * Users Configuration Variable
     */

    boolean defaultUsers = true; //If false You have to add manually users
    private userStatus currentUserStatus;


    //Output stream
    BufferedWriter out;

    /**
     * Right level management
     */
    private enum right
    {
        R,W,RW
    }


    /**
     * Class that define user needed by the server
     */
    private static class User{
        public String username;
        public String password;
        public right right;
        public User(String username,String password,right right)
        {
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ftpClientControlHandlerThread.class.getName());
            try {
                logger.addHandler(new FileHandler("logs/" + "ftpClientHandlerThread" + ".log"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.username= username;
            this.password= password;
            this.right = right;
        }
    }
    private User currentuser = null;

    //TODO Refactor if dynamic user needed
    private final ArrayList<User> users = new ArrayList<>();


    public SSLContext sslContext;

    /**
    * File management
    */
    public String rootDirectory = "ftp";
    public String currDirectory;

    //endregion
//region Constructors
    public ftpClientControlHandlerThread(Socket s)
    {
        currentCryptStatus = cryptStatus.PLAIN;
        this.s = s;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(this.s.getOutputStream())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public ftpClientControlHandlerThread(Socket s, SSLContext sslContext)
    {
        currentCryptStatus = cryptStatus.TLS;
        this.sslContext =  sslContext;
        this.s = s;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(this.s.getOutputStream())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //endregion
//region Command Management
    @Override
    public void run() {
        currentUserStatus = userStatus.NOLOGGING;
        currentTransferConnectionTypeStatus = transferConnectionStatus.ACTIVE;
        currDirectory = "/";
        if(defaultUsers) {
            this.users.add(new User("admin", "admin", right.RW));
            this.users.add(new User("guest", "guest", right.R));
            this.users.add(new User("anonymous", "anonymous", right.R));
        }
        dataIP = s.getLocalAddress().getHostAddress(); //Default Ip address
        dataPort = s.getLocalPort();
        //Set the default port
        if (sslContext == null) {
            currentCryptStatus = cryptStatus.PLAIN;
        } else{
            currentCryptStatus = cryptStatus.TLS;
        }
        try {
            Logger.log("Thread Initialised : " + s.getInetAddress().getHostAddress() +':'+  s.getLocalPort());
            //Respond to the connection try
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            sendMsgToClient("220 Welcome to the Custom FTP-Server made by Andrea Spelgatti");
            String line;
            while (!quitCommandLoop) {
                //Read the whole content
                while ((line = socketReader.readLine()) != null)
                {
                    this.executeCommand(line);
                }
            }
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(ftpClientControlHandlerThread.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    private void executeCommand(String c)
    {
        Logger.log(c);
        String [] cSplit;
        if(c != null)
            cSplit = c.split(" ");
        else
            throw new NullPointerException("No command received");
        String Command = cSplit[0];
        switch (Command) {
            case "PWD" -> PWDhandler();
            case "CWD" -> CWDhandler(cSplit);
            case "NLST", "LIST" -> NLSThandler(cSplit);
            case "TYPE" -> TYPEhandler(cSplit);
            case "PORT" -> PORThandler(cSplit);
            case "USER" -> USERhandler(cSplit);
            case "PASS" -> PASShandler(cSplit);
            case "PASV" -> PASVhandler();
            case "ACTV" -> ACTVhandler();
            case "RETR" -> RETRhandler(cSplit);
            case "STOR" -> STORhandler(cSplit);
            case "QUIT" -> QUIThandler();
            default -> sendMsgToClient("500 Command not implemented :" + Command);
        }
    }

    private void PORThandler(String[] cSplit) {
        if(cSplit.length > 1) {
            String[] port = cSplit[1].split(",");
            if (port.length == 6) {
                this.dataPort = Integer.parseInt(port[4]) * 256 + Integer.parseInt(port[5]);
                this.dataIP = port[0] + "." + port[1] + "." + port[2] + "." + port[3];
                sendMsgToClient("200 PORT command successful. Set to "+ this.dataPort + ". Consider using PASV.");
            } else {
                sendMsgToClient("501 Syntax error in parameters or arguments.");
            }
        }
        else
            sendMsgToClient("501 Syntax error in parameters or arguments.");
    }

    private void TYPEhandler(String[] cSplit) {
        if(cSplit[1].equals("I"))
            sendMsgToClient("200 Command OK"); //SUPPORT ONLY BINARY
        else
            sendMsgToClient("504 Command not implemented for that parameter");
    }


    private void PWDhandler() {
        sendMsgToClient("257 \""+this.currDirectory+"\" is the current directory");
    }

    private void CWDhandler(String[] cSplit) {
        if (cSplit.length > 1) {
            String directory = cSplit[1];
            switch(directory)
            {
                case "/":
                    currDirectory = "/";
                    sendMsgToClient("250 Directory successfully changed");
                    break;
                case "..":
                    if (currDirectory.equals("/")) {
                        sendMsgToClient("550 Can't go up from root directory");
                    } else {
                        currDirectory = currDirectory.substring(0, currDirectory.lastIndexOf('/'));
                        sendMsgToClient("250 Directory successfully changed");
                    }
                    break;
                default:
                    File f = new File(currDirectory + File.separator + directory);
                    if (f.exists() && f.isDirectory()) {
                        currDirectory = currDirectory +  File.separator + directory;
                        sendMsgToClient("250 Directory successfully changed");
                    } else {
                        sendMsgToClient("550 Directory doesn't exist");
                    }
                    break;
            }
            if (directory.equals("..")) {

            } else {
                    File f = new File(currDirectory + "/" + directory);
                    if (f.exists() && f.isDirectory()) {
                        currDirectory = currDirectory + "/" + directory;
                        sendMsgToClient("250 Directory successfully changed");
                    } else {
                        sendMsgToClient("550 Directory doesn't exist");
                    }
            }
        } else {
            sendMsgToClient("550 Directory doesn't exist");
        }
    }

    private void NLSThandler(String[] cSplit) {
        Thread dataThread = new Thread(new ftpClientDataTransferHandlerThread(this,ftpClientDataTransferHandlerThread.commandType.NLST,cSplit));
        dataThread.start();
    }

    private void USERhandler(String[] cSplit){
        if (cSplit.length > 1) {
            String username = cSplit[1];
            if (currentUserStatus == userStatus.NOLOGGING) {
                for (User u : users) {
                    if (u.username.equals(username)) {
                        sendMsgToClient("331 User name okay, need password");
                        currentUserStatus = userStatus.PASSWORD;
                        currentuser = u;
                    }
                }
            } else {
                sendMsgToClient("530 User already logged in");
            }
        }else {
            sendMsgToClient("530 Not logged in (empty password)");
        }
    }
    private void PASShandler(String[] cSplit){
        if (cSplit.length > 1) {
            String password = cSplit[1];
            if (currentuser != null) {
                if(currentuser.password.equals(password)) {
                    currentUserStatus = userStatus.LOGGED;
                    sendMsgToClient("230 Welcome to "+ currentuser.username);
                    sendMsgToClient("230 User logged in successfully");
                }else{
                    sendMsgToClient("530 Not logged in (wrong password)");
                }
            }
        }else{
            sendMsgToClient("530 Not logged in (empty password)");
        }
    }
    private void PASVhandler() {
        currentTransferConnectionTypeStatus = transferConnectionStatus.PASSIVE;
        //Trying to give a hint to the client on how to connect properly to the server in passive mode
        String myIp = s.getLocalAddress().getHostAddress(); //TODO handle IPV6
        String[] myIpSplit = myIp.split("\\.");

        int p1 = dataPort / 256;
        int p2 = dataPort % 256;

        sendMsgToClient("227 Entering Passive Mode (" + myIpSplit[0] + "," + myIpSplit[1] + "," + myIpSplit[2] + ","
                + myIpSplit[3] + "," + p1 + "," + p2 + ")");
    }
    private void ACTVhandler() {
        currentTransferConnectionTypeStatus = transferConnectionStatus.ACTIVE;
        sendMsgToClient("227 Server switched to active mode");
    }
    private void RETRhandler(String[] cSplit){
        if(currentuser.right != right.W)
        {
            Thread dataThread = new Thread(new ftpClientDataTransferHandlerThread(this,ftpClientDataTransferHandlerThread.commandType.RETR,cSplit));
            dataThread.start();
        }
        else {
            sendMsgToClient("530 No right to retrieve file");
        }
    }
    private void STORhandler(String[] cSplit){
        if(currentuser.right != right.R)
        {
            Thread dataThread = new Thread(new ftpClientDataTransferHandlerThread(this,ftpClientDataTransferHandlerThread.commandType.STOR,cSplit));
            dataThread.start();
        }
        else
        {
            sendMsgToClient("530 No right to store file");
        }
    }
    private void QUIThandler(){
        this.quitCommandLoop = true;
        sendMsgToClient("221 Service closing control connection");
    }
//endregion
//region Class Functions tdr: Here are the functions that are used by the data Socket thread and the control Socket
    public void sendMsgToClient(String s){
        try
        {
            Logger.log(s);
            out.write(s);
            out.newLine();
            out.flush();
        } catch (Exception e)
        {
            java.util.logging.Logger.getLogger(ftpClientControlHandlerThread.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    public String getCurrentUser()
{
        return currentuser.username;
    }
    public String getCurrentUsersRightsPosix() {
        //I used chatGPT to have the default posix formatting in Java for that kind of use
        String permission = "";
        switch (currentuser.right) {
            case R -> {
                Set<PosixFilePermission> permissions = EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.GROUP_READ,
                        PosixFilePermission.OTHERS_READ
                );
                permission = PosixFilePermissions.toString(permissions);
            }
            case W -> {
                Set<PosixFilePermission> permissions = EnumSet.of(
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.GROUP_WRITE,
                        PosixFilePermission.OTHERS_WRITE
                );
                permission = PosixFilePermissions.toString(permissions);
            }
            case RW -> {
                Set<PosixFilePermission> permissions = EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.GROUP_READ,
                        PosixFilePermission.GROUP_WRITE,
                        PosixFilePermission.OTHERS_READ,
                        PosixFilePermission.OTHERS_WRITE
                );
                permission = PosixFilePermissions.toString(permissions);
            }
        }
        return permission;
    }
//endregion
}
