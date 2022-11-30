/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.customhttpserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Andrea
 */
public class http_smartHttp_server {
    private Path File;
    private Boolean isSmart;
    private String[] parsedParameters;

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
        String[] split = File.toUri().getPath().split("/.");
        setIsSmart(split[1]);
        this.File = File;
    }

    /**
     * @param FileType the FileType to set
     * Here we can parse differently certains types of files depending on their type
     */
    private void setIsSmart(String FileType) {
        switch(FileType){
            case "shttp": //smarthttp
                this.isSmart = true;
            default:
                this.isSmart = false;
        }
    }
    /**
     * @param parsedParameters the parsedParameters to set
     */
    public void setParsedParameters(String[] parsedParameters) {
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
    private byte[]  smartHandler(byte[] filecontent) {
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
    private String functionCaller(String s) {
        switch(s)
        {
            case "<!-- Add Here all available Activities -->":
                return availableActivitiesForm(parsedParameters);
            default:
                System.out.println("Error you have called an unset function");
        }

        return null;
    }

    private String availableActivitiesForm(String[] parsedParameters) {
        return "        <form action=\"activitie_selected.html\" method=\"post\">\n" +
                "          <input type=\"checkbox\" id=\"vehicle1\" name=\"vehicle1\" value=\"Bike\">\n" +
                "          <label for=\"vehicle1\"> I have a bike</label><br>\n" +
                "          <input type=\"checkbox\" id=\"vehicle2\" name=\"vehicle2\" value=\"Car\">\n" +
                "          <label for=\"vehicle2\"> I have a car</label><br>\n" +
                "          <input type=\"checkbox\" id=\"vehicle3\" name=\"vehicle3\" value=\"Boat\">\n" +
                "          <label for=\"vehicle3\"> I have a boat</label>\n" +
                "          <input name=\"Submit_button\" type=\"submit\" value=\"Submit\" />\n" +
                "        </form>";
    }
}
