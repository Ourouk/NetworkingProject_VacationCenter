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

    private String availableActivitiesForm(HashMap parsedParameters) throws IOException {

        //Check if the users is present in the server Database;
        String id = (String) parsedParameters.get("id");
        String to_add = new String();
        if (!id.isBlank()) {
            auth_client auth_client = new auth_client("127.0.0.1", 5050);
            Customer customer = auth_client.getCustomer(id);
            PostgresqlJdbcLibrary postgresql = new PostgresqlJdbcLibrary(127.0.0.1,5432);
            to_add += "<h1>Choose Activity for user : "+ customer.getName() + customer.getSurname() + "</h1>" + "\r\n";
            to_add += "<p>Select Activities you want to participate to :</p>" + "\r\n" + 
                      "<!-- Add Here all available Activities -->" + "\r\n";
            
                      //Here I will Enter All the available activities
                    /*<form action="activitie_selected.html" method="post">
                      <input type="checkbox" id="vehicle1" name="vehicle1" value="Bike">
                      <label for="vehicle1"> I have a bike</label><br>
                      <input type="checkbox" id="vehicle2" name="vehicle2" value="Car">
                      <label for="vehicle2"> I have a car</label><br>
                      <input type="checkbox" id="vehicle3" name="vehicle3" value="Boat">
                      <label for="vehicle3"> I have a boat</label>
                      <input name="Submit_button" type="submit" value="Submit" />
                    </form>*/
        }else
        {
            to_add += "<h1>No Id entered!</h1>";
        }
        

        //Download the available activities from the SQL Database;
        return to_add;
    }


}
