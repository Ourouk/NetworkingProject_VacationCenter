/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.customHttpServer;

import com.hepl.customHttpServer.PostgresqlJdbcLibrary.PostgresqlJdbcLibrary;
import com.hepl.customHttpServer.authServerConnector.authClient;
import com.hepl.customHttpServer.authServerConnector.data.Customer;
import com.hepl.customSmtpClient.smtpSender;

import java.io.BufferedReader;
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
    public String content_type;

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
        String contentType = "";
        switch(FileType.toLowerCase()){
            case "shtml": //smarthttp
                this.isSmart = true;
                 content_type = "text/html; charset=utf-8";
                break;
            default:
                this.isSmart = false;
                switch (FileType.toLowerCase()) {
                    case "html":
                        contentType = "text/html; charset=utf-8";
                        break;
                    case "css":
                        contentType = "text/css; charset=utf-8";
                        break;
                    case "js":
                        contentType = "application/javascript; charset=utf-8";
                        break;
                    case "jpg":
                    case "jpeg":
                        contentType = "image/jpeg";
                        break;
                    case "png":
                        contentType = "image/png";
                        break;
                    case "gif":
                        contentType = "image/gif";
                        break;
                    case "svg":
                        contentType = "image/svg+xml";
                        break;
                    case "webp":
                        contentType = "image/webp";
                        break;
                    case "bmp":
                        contentType = "image/bmp";
                        break;
                    case "mp4":
                        contentType = "video/mp4";
                        break;
                    case "avi":
                        contentType = "video/x-msvideo";
                        break;
                    case "mkv":
                        contentType = "video/x-matroska";
                        break;
                    case "mp3":
                        contentType = "audio/mpeg";
                        break;
                    case "wav":
                        contentType = "audio/wav";
                        break;
                    case "ogg":
                        contentType = "audio/ogg";
                        break;
                    case "pdf":
                        contentType = "application/pdf";
                        break;
                    case "doc":
                    case "docx":
                        contentType = "application/msword";
                        break;
                    case "xls":
                    case "xlsx":
                        contentType = "application/vnd.ms-excel";
                        break;
                    case "ppt":
                    case "pptx":
                        contentType = "application/vnd.ms-powerpoint";
                        break;
                    case "json":
                        contentType = "application/json; charset=utf-8";
                        break;
                    case "xml":
                        contentType = "application/xml; charset=utf-8";
                        break;
                    case "zip":
                        contentType = "application/zip";
                        break;
                    case "rar":
                        contentType = "application/x-rar-compressed";
                        break;
                    case "tar":
                        contentType = "application/x-tar";
                        break;
                    default:
                        contentType = "application/octet-stream";
                        break;
                }
            }
        this.content_type = contentType;
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
    private String[] listOfAvailablesFunctions = new String[]{"<!-- Add Here all available Activities -->","Error you have called an unset function"};
    
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
        httpClientHandlerThread.ConsoleLogging("Function availableActivitiesForm activated, trying to inject code in " + file.toString());
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
                if(pg != null)
                {
                    Statement statement = pg.createStatement();
                    //Download the available activities from the SQL Database;
                    httpClientHandlerThread.ConsoleLogging("Trying to fetch information from the postgresql_server");
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM public.activities");
                    if (resultSet != null) {
                        to_add += "<form action=\"activities_selected.html\" method=\"post\">";
                        while (resultSet.next()) {
                            to_add += "<input type=\"checkbox\"id=\"" + resultSet.getString("name") + "\"name=\"" + resultSet.getString("name") + "\"value=\"Bike\">";
                            to_add += "<label for=\"" + resultSet.getString("name") + "\">" + resultSet.getString("name") + "</label><br>";
                        }
                    to_add += "<input name=\"Submit_button\" type=\"submit\" value=\"Submit\" />" + "\r\n" +
                            "</form>" + "\r\n";
                    resultSet.close();
                }
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
    private String mailsent()
    {
        String to_add;
        smtpSender mail_sender = new smtpSender();

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
