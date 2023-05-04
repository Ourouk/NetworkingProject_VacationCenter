package com.hepl.customFtpServer;


import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.Socket;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ftpClientControlHandlerThread implements Runnable{
    public String dataIP;
    public int dataPort;

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

    /**
    * File management
    */
    public String rootDirectory = "ftp";
    public String currDirectory;

    //endregion
//region Constructors
    public ftpClientControlHandlerThread(Socket s)
    {
        //Default State
        currentUserStatus = userStatus.NOTLOGGEDIN;
        currentTranferConnectionTypeStatus = transferConnectionStatus.ACTIVE;
        currentCryptStatus = cryptStatus.PLAIN;
        currDirectory = "/";
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
        dataIP = s.getLocalAddress().getHostAddress(); //Default Ip address
        //Set the default port
        if (sslContext == null) {
            currentCryptStatus = cryptStatus.PLAIN;
            dataPort = 20;
        } else{
            currentCryptStatus = cryptStatus.TLS;
            dataPort = 990;
        }
        try {
            ConsoleLogging("Thread Initialised : " + s.getInetAddress().getHostAddress() +':'+  s.getLocalPort());
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
            case "PWD" : PWDhandler(cSplitted); break;
            case "CWD" : CWDhandler(cSplitted); break;
            case "NLST","LIST" : NLSThandler(cSplitted); break;
            case "TYPE" : TYPEhandler(cSplitted); break;
            case "PORT" : PORThandler(cSplitted); break;
            case "USER": USERhandler(cSplitted); break;
            case "PASS": PASShandler(cSplitted); break;
            case "PASV": PASVhandler(cSplitted); break;
            case "ACTV": ACTVhandler(cSplitted); break;
            case "RETR": RETRhandler(cSplitted); break;
            case "STOR": STORhandler(cSplitted); break;
            case "QUIT": QUIThandler(cSplitted); break;
            case default: sendMsgToClient("500 Command not implemented :" + Command); break;
        }
    }

    private void PORThandler(String[] cSplitted) {
        if(cSplitted.length > 1) {
            String[] port = cSplitted[1].split(",");
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

    private void TYPEhandler(String[] cSplitted) {
        if(cSplitted[1].equals("I"))
            sendMsgToClient("200 Command OK"); //SUPPORT ONLY BINARY
        else
            sendMsgToClient("504 Command not implemented for that parameter");
    }


    private void PWDhandler(String[] cSplitted) {
        sendMsgToClient("257 \""+this.currDirectory+"\" is the current directory");
    }

    private void CWDhandler(String[] cSplitted) {
        if (cSplitted.length > 1) {
            String directory = cSplitted[1];
            if (directory.equals("..")) {
                if (currDirectory.equals("/")) {
                    sendMsgToClient("550 Can't go up from root directory");
                } else {
                    currDirectory = currDirectory.substring(0, currDirectory.lastIndexOf('/'));
                    sendMsgToClient("250 Directory successfully changed");
                }
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

    private void NLSThandler(String[] cSplitted) {
        Thread dataThread = new Thread(new ftpClientDataTransferHandlerThread(this,ftpClientDataTransferHandlerThread.commandType.NLST,cSplitted));
        dataThread.start();
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
        //Trying to give a hint to the client on how to connect properly to the server in passive mode
        String myIp = "127.0.0.1";
        String myIpSplit[] = myIp.split("\\.");

        int p1 = dataPort / 256;
        int p2 = dataPort % 256;

        sendMsgToClient("227 Entering Passive Mode (" + myIpSplit[0] + "," + myIpSplit[1] + "," + myIpSplit[2] + ","
                + myIpSplit[3] + "," + p1 + "," + p2 + ")");
    }
    private void ACTVhandler(String[] cSplitted) {
        currentTranferConnectionTypeStatus = transferConnectionStatus.ACTIVE;
        sendMsgToClient("227 Server switched to active mode");
    }
    private void RETRhandler(String[] cSplitted){
        if(currentuser.right != right.W)
        {
            Thread dataThread = new Thread(new ftpClientDataTransferHandlerThread(this,ftpClientDataTransferHandlerThread.commandType.RETR,cSplitted));
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
//endregion
//region Class Functions tdr: Here are the functions that are used by the data Socket thread and the control Socket
    public void sendMsgToClient(String s){
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
//region Helper Functions
/**
 * Simple "Macro" Managing the console log
 * @param log String containing the message that is needed to be displayed on the console
 */
private void ConsoleLogging(String log)
{
    System.out.println("Thread " + Thread.currentThread().getId() + " at " + java.time.LocalDateTime.now().getHour()+":"+java.time.LocalDateTime.now().getMinute()+ ":"+ java.time.LocalDateTime.now().getSecond() + " : " +  log);
}
//endregion

}
