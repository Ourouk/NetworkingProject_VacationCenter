package com.hepl.customFtpServer;


import com.hepl.Logger;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.Socket;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class ftpClientControlHandlerThread implements Runnable{

//region Data Handler shared Variable
    private Thread dataThread;
    public final ReentrantLock dataThreadLock = new ReentrantLock();
//endregion

//region Properties Type
    /**
     * Status of the server
     */
    public enum transferConnectionStatus {PASSIVE,ACTIVE}


    public enum CommandName {
        ACTV,
        CWD,
        LIST,
        NLST,
        PASS,
        PASV,
        PORT,
        PWD,
        QUIT,
        RETR,
        STOR,
        TYPE,
        USER

    }

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
    public String dataIP;
    public int dataPassivePort;
    public cryptStatus currentCryptStatus;

    public transferConnectionStatus currentTransferConnectionTypeStatus;

    /**
     * Permit the dataHandler and controlHandler to share the same parameters
     */
    public CommandName currentCommand;
    public String[] currentCommentParameters;

    public final Socket s;
    private boolean quitCommandLoop=false;

    /**
     * Users Configuration Variable
     */

    boolean defaultUsers = true; //If false You have to add manually users
    private userStatus currentUserStatus;


    /**
     * Default Output Used to communicate with c;oemt
     */
    private BufferedWriter out;


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
        dataPassivePort = s.getLocalPort() -1 ;
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
            //Handle bad connection closing
            new RuntimeException(e);
        }
    }
    private void executeCommand(String c)
    {
        Logger.log(c);
        if(c != null)
            this.currentCommentParameters = c.split(" ");
        else
            throw new NullPointerException("No command received");
        switch (this.currentCommentParameters[0]) {
            case "PWD":
                currentCommand = CommandName.PWD;
                this.PWDhandler();
                break;
            case "CWD":
                currentCommand = CommandName.CWD;
                CWDhandler(this.currentCommentParameters);
                break;
            case "NLST":
                currentCommand = CommandName.NLST;
                NLSThandler(this.currentCommentParameters);
                break;
            case "LIST":
                currentCommand = CommandName.NLST;
                NLSThandler(this.currentCommentParameters);
                break;
            case "TYPE":
                currentCommand = CommandName.TYPE;
                TYPEhandler(this.currentCommentParameters);
                break;
            case "PORT":
                currentCommand = CommandName.PORT;
                PORThandler(this.currentCommentParameters);
                break;
            case "USER":
                currentCommand = CommandName.USER;
                USERhandler(this.currentCommentParameters);
                break;
            case "PASS":
                currentCommand = CommandName.PASS;
                PASShandler(this.currentCommentParameters);
                break;
            case "PASV":
                currentCommand = CommandName.PASV;
                PASVhandler(this.currentCommentParameters);
                break;
            case "ACTV":
                currentCommand = CommandName.ACTV;
                PORThandler(this.currentCommentParameters);
                break;
            case "RETR":
                currentCommand = CommandName.RETR;
                RETRhandler(this.currentCommentParameters);
                break;
            case "STOR":
                currentCommand = CommandName.STOR;
                STORhandler(this.currentCommentParameters);
                break;
            case "QUIT":
                currentCommand = CommandName.QUIT;
                QUIThandler();
                break;
            default:
                sendMsgToClient("500 Command not implemented: " + this.currentCommentParameters[0]);
                break;
        }
    }

    private void PORThandler(String[] cSplit) {
        this.currentTransferConnectionTypeStatus = transferConnectionStatus.ACTIVE;
        this.activateDataThread();
        this.sendMsgToClient("200 PORT command successful.");
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
        this.activateDataThread();
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
    private void PASVhandler(String[] cSplit) {
        currentTransferConnectionTypeStatus = transferConnectionStatus.PASSIVE;
        this.activateDataThread();
    }
    private void ACTVhandler() {
        currentTransferConnectionTypeStatus = transferConnectionStatus.ACTIVE;
        sendMsgToClient("227 Server switched to active mode");
    }
    private void RETRhandler(String[] cSplit){
        if(currentuser.right != right.W)
        {
            this.activateDataThread();
        }
        else {
            sendMsgToClient("530 No right to retrieve file");
        }
    }
    private void STORhandler(String[] cSplit){
        if(currentuser.right != right.R)
        {
            this.activateDataThread();
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
//region datahandle help
    private Thread activateDataThread()
    {
        synchronized (this.dataThreadLock) {
            if (dataThread != null  && dataThread.isAlive()) {
                dataThreadLock.notify();
            } else {
                dataThread = new Thread(new ftpClientDataTransferHandlerThread(this));
                dataThread.start();
                try {
                    this.dataThreadLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return dataThread;
    }
//endregion

//region Class Functions tdr: Here are the functions that are used by the data Socket thread and the control Socket
    public void sendMsgToClient(String s){
        synchronized (dataThreadLock) {
            try {
                Logger.log(s);
                out.write(s);
                out.newLine();
                out.flush();
                dataThread.notify();
            } catch (Exception e) {
                new RuntimeException(e);
            }
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