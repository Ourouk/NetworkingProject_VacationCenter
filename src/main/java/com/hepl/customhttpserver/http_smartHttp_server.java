/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.customhttpserver;

import com.hepl.customhttpserver.PostgresqlJdbcLibrary.PostgresqlJdbcLibrary;
import com.hepl.customhttpserver.auth_server_connector.auth_client;
import com.hepl.customhttpserver.data.Customer;
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
public class http_smartHttp_server {
    private Path File;
    private Boolean isSmart;
    private HashMap parsedParameters;

    /**
     * @return the File
     */
    public Path getFile() {
        return File;
    }

    /**
     * @param File the File to set
     */
    private void setFile(Path File) {
        String[] split = File.toUri().getPath().split("[.]");
        setIsSmart(split[1]);
        this.File = File;
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
    /**
     * @param parsedParameters the parsedParameters to set
     */
    public void setParsedParameters(HashMap parsedParameters) {
        this.parsedParameters = parsedParameters;
    }
    
    public http_smartHttp_server(Path path)
    {
        this.setFile(path);
    }
    
    public byte[] getFileContent() throws IOException
    {
        byte[] filecontent = Files.readAllBytes(File);
        if(this.isSmart)
            filecontent = this.smartHandler(filecontent);
        return filecontent;
    }
    
    
    //The smart Handler assume that You use only one time each function
    private byte[]  smartHandler(byte[] filecontent) throws IOException {
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
            }
        }
        return filecontent;
    }
    

    private String[] listOfAvailablesFunctions = new String[]{"<!-- Add Here all available Activities -->"};
    
    private String functionCaller(String s) throws IOException {
        switch(s)
        {
            case "<!-- Add Here all available Activities -->":
                return availableActivitiesForm(parsedParameters);
            default:
                System.out.println("Error you have called an unset function");
        }

        return null;
    }

    private String availableActivitiesForm(HashMap parsedParameters){
        System.out.println("Trying to fetch information from the auth_server");
        //Check if the users is present in the server Database;
        String id = (String) parsedParameters.get("id");
        String to_add = new String();
        System.out.println("");
        if (!id.isBlank()) {
            try {
                auth_client auth_client = new auth_client("127.0.0.1", 5050);
                Customer customer = auth_client.getCustomer(id);
                to_add += "<h1>Choose Activity for user : " + customer.getName() + customer.getSurname() + "</h1>" + "\r\n";
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            to_add += "<p>Select Activities you want to participate to :</p>" + "\r\n" +
                    "<!-- Add Here all available Activities -->" + "\r\n";
            try {
                Connection pg = PostgresqlJdbcLibrary.getConnection();
                Statement statement = pg.createStatement();
                //Download the available activities from the SQL Database;
                System.out.println("Trying to fetch information from the postgresql_server");
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
}
