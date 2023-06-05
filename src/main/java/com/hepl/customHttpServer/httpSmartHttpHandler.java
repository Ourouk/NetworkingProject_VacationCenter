/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.customHttpServer;

import com.hepl.Logger;
import com.hepl.customHttpServer.PostgresqlJdbcLibrary.PostgresqlJdbcLibrary;
import com.hepl.customHttpServer.authServerConnector.authClient;
import com.hepl.customHttpServer.authServerConnector.data.Customer;
import com.hepl.customHttpServer.ftpClientConnector.ftpClient;
import com.hepl.customSmtpClient.smtpSender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 *
 * @author Andrea
 */
public class httpSmartHttpHandler {
    private httpClientHandlerThread httpClientHandlerThread;
    private Path file;
    private FileReader fileReader;
    private Boolean isSmart;

    /**
     *
     * @param path to the file to be sent
     */
    public httpSmartHttpHandler(httpClientHandlerThread httpClient,Path path)
    {
        this.httpClientHandlerThread = httpClient;
        this.setFile(path);
    }

    /**
     * Note : This Method contains the smart file detector
     * @param file_path The path to the file to set
     */
    private void setFile(Path file_path) {
        Path path;
        if(file_path.toFile().exists())
            path = file_path;
        else
            path = com.hepl.customHttpServer.httpClientHandlerThread.FILE_NOT_FOUND.toPath();

        String[] split = path.getFileName().toString().split("[.]");
            setIsSmart(split[1]);
        this.file = file_path;
        try {
            this.fileReader = new FileReader(this.file.toFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @return the file path
     */
    public Path getFile() {
        return file;
    }


    /**
     * @param FileType the FileType to set
     * Here we can parse differently certains types of files depending on their type
     */
    private void setIsSmart(String FileType) {
        switch(FileType){
            case "shtml": //smarthttp
                this.isSmart = true;
                break;
            default:
                this.isSmart = false;
        }
    }

    
    public byte[] getFileContent() throws IOException
    {
        if(this.isSmart)
            return smartInjectionHandler(Files.readAllBytes(file));
        else {
            return Files.readAllBytes(file);
        }
    }


    /**
     *
     * @param filecontent //Contain the all the content from the shtml file
     * @return modified filecontent
     * @throws IOException
     */
    private byte[] smartInjectionHandler(byte[] filecontent) throws IOException {
        byte[] new_filecontent;
        String buff = new String(filecontent);
        StringBuilder builder = new StringBuilder();
        for(String s : listOfAvailablesFunctions)
        {
            if(buff.contains(s))
            {
                String htmltoAdd = functionCaller(s);
                String[] split = buff.split(s);
                    builder.append(split[0]);
                    builder.append(htmltoAdd);
                    builder.append(split[1]);
                buff = builder.toString();
            }
        }
        new_filecontent = buff.getBytes();
        return new_filecontent;
    }
    
    // All Smart Handler should be added in the two next method and create a new function to add the content.
    private String[] listOfAvailablesFunctions = new String[]{"<!-- Add Here all available Activities -->","<!-- Add Here the mail sent -->","Error you have called an unset function"};
    
    private String functionCaller(String s) throws IOException {
        switch(s)
        {
            case "<!-- Add Here all available Activities -->":
                return availableActivitiesForm();
            case "<!-- Add Here the mail sent -->":
                return mailsent();
            default:
                System.out.println("Error you have called an unset function");
                return null;
        }
    }

    private String availableActivitiesForm(){
        Logger.log("Function availableActivitiesForm activated, trying to inject code in " + file.toString());
        HashMap parsedParameters = this.body_parser(httpClientHandlerThread.requestBody);

        String id = (String) parsedParameters.get("id");
        String to_add = new String();
        //Check if the users is present in the server Database;
        if (!id.isBlank()) {
//            httpClientHandlerThread.ConsoleLogging("Trying to find the users " + id + " inside the Server Database");
//            try {
//                authClient authClient = new authClient("127.0.0.1", 5050);
//                Customer customer = authClient.getCustomer(id);
//                to_add += "<h1>Choose Activity for user : " + customer.getName() + customer.getSurname() + "</h1>" + "\r\n";
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
            to_add += "<p>Select Activities you want to participate to :</p>" + "\r\n" +
                    "<!-- Add Here all available Activities -->" + "\r\n";
            try {
            Connection pg = PostgresqlJdbcLibrary.getConnection();
            Statement statement = pg.createStatement();
            //Download the available activities from the SQL Database;
            Logger.log("Trying to fetch information from the postgresql_server");
            ResultSet resultSet = statement.executeQuery("SELECT * FROM activities");
            if (resultSet != null) {
                to_add += "<form action=\"activitie_selected.html\" method=\"post\">";
                while (resultSet.next()) {
                    to_add += "<input type=\"checkbox\"id=\"" + resultSet.getString("name") + "\"name=\"" + resultSet.getString("name") + "\"value=\"Bike\">";
                    to_add += "<label for=\"" + resultSet.getString("name") + "\">" + resultSet.getString("name") + "</label><br>";
                }
                to_add += "<input name=\"Submit_button\" type=\"submit\" value=\"Submit\" />" + "\r\n" +
                        "</form>" + "\r\n";
                resultSet.close();
            }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            to_add += "<h1>No Id entered!</h1>";
        }
        return to_add;
    }
    private String mailsent() {
        HashMap parsedParameters = this.body_parser(httpClientHandlerThread.requestBody);
        //ftpClient ftpC = new ftpClient("127.0.0.1","guest","guest","camp.pdf");
        //Thread ftpClient_thread = new Thread(ftpC);
        //ftpClient_thread.run();
        String content =
                "The ip address is 127.0.0.1 and the login is guest:guest \n\n" +
                "Here are the instructions on how to use the FTP client in the Command Prompt (CMD) on Windows:\n" +
                "\n" +
                "    Open the Command Prompt: Press the Windows key, type \"cmd,\" and hit Enter to open the Command Prompt.\n" +
                "\n" +
                "    Launch the FTP client: In the Command Prompt window, type ftp and hit Enter. This will start the FTP client and display the ftp> prompt.\n" +
                "\n" +
                "    Connect to an FTP server: To connect to an FTP server, use the open command followed by the server's IP address or hostname. For example, type open ftp.example.com and hit Enter. If the connection is successful, you will be prompted to enter your FTP username and password.\n" +
                "\n" +
                "    Enter FTP credentials: After connecting to the FTP server, you need to provide your FTP username and password. Enter the username and hit Enter. Then enter the password and hit Enter. If the credentials are correct, you will be successfully logged in to the FTP server.\n" +
                "\n" +
                "    Explore FTP server contents: Once logged in, you can navigate the FTP server's directory structure using various commands. Some commonly used commands are:\n" +
                "        dir: List the files and directories in the current directory on the FTP server.\n" +
                "        cd: Change the current directory on the FTP server. For example, cd directory_name will change to the specified directory.\n" +
                "        get: Download a file from the FTP server to your local machine. For example, get file.txt will download the file named \"file.txt\".\n" +
                "        put: Upload a file from your local machine to the FTP server. For example, put local_file.txt will upload the file \"local_file.txt\" to the current directory on the FTP server.\n" +
                "\n" +
                "    Disconnect from the FTP server: To disconnect from the FTP server, type bye and hit Enter. This will close the FTP client.\n" +
                "\n" +
                "These are the basic steps to use the FTP client in the Command Prompt. Remember to replace ftp.example.com with the actual FTP server's IP address or hostname, and adjust the commands based on your requirements. You can also type help in the FTP client to get a list of available commands and their descriptions.";
        smtpSender mail_sender = new smtpSender(parsedParameters.get("email").toString(),content,"Camp.pdf",new File("buffer_file.tmp").toPath());
        Thread mail_thread = new Thread(mail_sender);
        mail_sender.run();
        String to_add;
        to_add = "Mail sent to " + parsedParameters.get("email");
        return to_add;
    }
    private HashMap body_parser(String body)
    {
        HashMap form_hashmap = new HashMap();
        String[] body_list = httpClientHandlerThread.requestBody.split("\n");
        for (String line: body_list) {
            String[] split_key = line.split("=");
            form_hashmap.put(split_key[0], split_key[1]);
        }
        return form_hashmap;
    }
}
