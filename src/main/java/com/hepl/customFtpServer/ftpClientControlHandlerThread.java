package com.hepl.customFtpServer;


import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ftpClientControlHandlerThread implements Runnable{
//region Properties Type
    /**
     * A list of available command
     */
    public enum commandType {USER,PASS,RETR,STOR,PASV,QUIT}
    /**
     * Status of the server
     */
    public enum transferConnectionStatus {PASSIVE,ACTIVE}
    public transferConnectionStatus currentTranferConnectionTypeStatus;
    /**
     * Indicating the last set transfer type
     */

    /**
     * Indicates the authentication status of a user
     */
    private enum userStatus {NOTLOGGEDIN, ENTEREDUSERNAME, LOGGEDIN}

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


    //Oupout stream

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
    private class User{
        public String username;
        public String password;
        public right right;
        public User(String username,String password,right right)
        {
            Logger logger = Logger.getLogger(ftpClientControlHandlerThread.class.getName());
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
    private  ArrayList<User> users = new ArrayList<User>();


    public SSLContext sslContext;
//endregion
//region Constructors
    public ftpClientControlHandlerThread(Socket s)
    {
        //Default State
        currentUserStatus = userStatus.NOTLOGGEDIN;
        currentTranferConnectionTypeStatus = transferConnectionStatus.ACTIVE;
        currentCryptStatus = cryptStatus.PLAIN;

        this.s = s;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(this.s.getOutputStream())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(defaultUsers) {
            this.users.add(new User("admin","admin",right.RW));
            this.users.add(new User("guest","guest",right.R));
            this.users.add(new User("anonymous","anonymous",right.R));
        }
    }
    public ftpClientControlHandlerThread(Socket s, SSLContext sslContext)
    {
        //Default State
        currentUserStatus = userStatus.NOTLOGGEDIN;
        currentTranferConnectionTypeStatus = transferConnectionStatus.ACTIVE;
        currentCryptStatus = cryptStatus.TLS;
        this.sslContext =  sslContext;
        this.s = s;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(this.s.getOutputStream())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(defaultUsers) {
            this.users.add(new User("admin","admin",right.RW));
            this.users.add(new User("guest","guest",right.R));
            this.users.add(new User("anonymous","anonymous",right.R));
        }
    }
    //endregion
//region Command Management
    @Override
    public void run() {
        try {
            ConsoleLogging("Thread Initialised : " + s.getInetAddress().toString() +':'+  s.getPort());
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
            Logger.getLogger(ftpClientControlHandlerThread.class.getName()).log(Level.SEVERE, null, e);
        }

    }
    private void executeCommand(String c)
    {
        ConsoleLogging(c);
        String [] cSplitted;
        if(c != null)
            cSplitted = c.split(" ");
        else
            throw new NullPointerException("No command received");
        String Command = cSplitted[0];
        switch(Command)
        {
            case "USER": USERhandler(cSplitted); break;
            case "PASS": PASShandler(cSplitted); break;
            case "PASV": PASVhandler(cSplitted); break;
            case "ACTV": ACTVhandler(cSplitted); break;
            case "RETR": RETRhandler(cSplitted); break;
            case "STOR": STORhandler(cSplitted); break;
            case "QUIT": QUIThandler(cSplitted); break;
        }
    }




    private void USERhandler(String[] cSplitted){
        if (cSplitted.length > 1) {
            String username = cSplitted[1];
            if (currentUserStatus == userStatus.NOTLOGGEDIN) {
                for (User u : users) {
                    if (u.username.equals(username)) {
                        sendMsgToClient("331 User name okay, need password");
                        currentUserStatus = userStatus.ENTEREDUSERNAME;
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
    private void PASShandler(String[] cSplitted){
        if (cSplitted.length > 1) {
            String password = cSplitted[1];
            if (currentuser != null) {
                if(currentuser.password.equals(password)) {
                    currentUserStatus = userStatus.LOGGEDIN;
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
    private void PASVhandler(String[] cSplitted) {
        currentTranferConnectionTypeStatus = transferConnectionStatus.PASSIVE;
        sendMsgToClient("227 Server switched to passive mode");
    }
    private void ACTVhandler(String[] cSplitted) {
        currentTranferConnectionTypeStatus = transferConnectionStatus.ACTIVE;
        sendMsgToClient("227 Server switched to active mode");
    }
    private void RETRhandler(String[] cSplitted){
        if(currentuser.right != right.W)
        {
            Thread dataThread = new Thread(new ftpClientDataTransferHandlerThread(this,ftpClientDataTransferHandlerThread.commandType.RETR,cSplitted));
            sendMsgToClient("150 Opening binary mode data connection for requested file " + cSplitted[1]);
            dataThread.start();
        }
        else {
            sendMsgToClient("530 No right to retrieve file");
        }
    }
    private void STORhandler(String[] cSplitted){
        if(currentuser.right != right.R)
        {
            Thread dataThread = new Thread(new ftpClientDataTransferHandlerThread(this,ftpClientDataTransferHandlerThread.commandType.STOR,cSplitted));
            sendMsgToClient("150 Opening binary mode data connection for requested file " + cSplitted[1]);
            dataThread.start();
        }
        else
        {
            sendMsgToClient("530 No right to store file");
        }
    }
    private void QUIThandler(String[] cSplitted){
        this.quitCommandLoop = true;
    }
    private void sendMsgToClient(String s){
        try
        {
            ConsoleLogging(s);
            out.write(s+'\n');
            out.flush();
        } catch (Exception e)
        {
            Logger.getLogger(ftpClientControlHandlerThread.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    //region Manage Connection for Data Transfer
    //TODO Re-Implement that in a thread

    /**
     * Simple "Macro" Managing the console log
     * @param log String containing the message that is needed to be displayed on the console
     */
    private void ConsoleLogging(String log)
    {
        System.out.println("Thread " + Thread.currentThread().getId() + " at " + java.time.LocalDateTime.now().getHour()+":"+java.time.LocalDateTime.now().getMinute()+ ":"+ java.time.LocalDateTime.now().getSecond() + " : " +  log);
    }
}
