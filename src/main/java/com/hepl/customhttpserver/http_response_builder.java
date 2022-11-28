/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.customhttpserver;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Andrea
 */
public class http_response_builder {
    public http_response_builder()
    {
        
    }
    public http_response_builder(byte[] Content)
    {
        this.CreateDefaultHeader();
        this.setContent(Content);
        this.setSize(this.getContent().length);
    }
    private void CreateDefaultHeader()
    {
        this.setVersion("HTTP/1.1");
        this.setCode(200);
        this.setHumand_readable_code("ok");
        this.setContent_type("text/html charset=utf-8");
    }
    public void send(DataOutputStream o) throws IOException
    {
        o.write((this.getVersion() + " " + this.getCode() + " " + this.getHumand_readable_code() + "\r\n").getBytes());
        o.write((this.getContent_type() +  "\r\n").getBytes());
        o.write(("Content-Lenght: " + this.getSize() + "\r\n\r\n").getBytes());
        o.write(this.getContent());
    }
    /**e
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return the humand_readable_code
     */
    public String getHumand_readable_code() {
        return humand_readable_code;
    }

    /**
     * @param humand_readable_code the humand_readable_code to set
     */
    public void setHumand_readable_code(String humand_readable_code) {
        this.humand_readable_code = humand_readable_code;
    }

    /**
     * @return the content_type
     */
    public String getContent_type() {
        return content_type;
    }

    /**
     * @param content_type the content_type to set
     */
    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    //TODO Check if the size given is right, i don't know if respond caracters should be counted
    public void setSize(int size) {
        //NOTE : This is to count for the /r/n at the end of the http packet
        this.size = size;
    }

    /**
     * @return the content
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * @param content the content to set note that it set the size
     */
    public void setContent(byte[] content) {
        this.content = content;
        this.setSize(content.length);
    }
    
    
    
    private String version;
    private int code;
    private String humand_readable_code;
    private String content_type;
    private int size;
    private byte[] content;
}
