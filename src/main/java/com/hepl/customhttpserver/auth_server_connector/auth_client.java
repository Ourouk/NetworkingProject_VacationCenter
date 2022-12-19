/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.customhttpserver.auth_server_connector;

import com.hepl.customhttpserver.data.Customer;
import java.io.IOException;
import java.net.*;
import java.io.*;

/**
 *
 * @author Andrea
 */
public class auth_client{
    private Socket s = null;
    
    public auth_client(String tcp_ipaddress,int tcp_port) throws IOException
    {
        s = new Socket(tcp_ipaddress, tcp_port);
    }
    
    private byte[] command_Formater(String Command,String content)
    {
        byte[] buff = null;
        byte[] read_buff;
        read_buff = Command.getBytes();
        buff[0] = read_buff[0];
        buff[1] = read_buff[1];
        int size = content.length() + 1; //For the end of line char
        buff[2] = (byte) ((size >> 24) & 0xFF);
        buff[3] = (byte) ((size >> 16) & 0xFF);
        buff[4] = (byte) ((size >> 8) & 0xFF);
        buff[5] = (byte) (size & 0xFF);
        int i = 1;
        read_buff = content.getBytes();
        for(byte c: read_buff)
        {
            buff[5+i] = c;
            i++;
        }
        buff[5+i+1] = '\0';
        return buff;
    }
    private String[] command_Parser(InputStream inputStream) throws IOException
    {
        String[] buff = null;
        byte[] in = null;
        int counter = 0,byte_counter = 0;
        int command_size_buffer = 0;
        while((byte_counter = inputStream.read(in, byte_counter, 2 - byte_counter)) <= 2)
        {
            byte_counter += byte_counter;
        }
        for(byte i: in)
            buff[0] += Character.toChars(i);
        
        //reset
        in = null;
        counter = 0;
        
        while((byte_counter = inputStream.read(in, byte_counter + byte_counter, 4 - byte_counter)) <= 4)
        {
            byte_counter += byte_counter;
        }
        command_size_buffer = ((in[3] << 24) & 0xFF)+ ((in[4] << 16) & 0xFF) +  ((in[5] << 8) & 0xFF) +  ((in[6]) & 0xFF);
        
        //reset
        in = null;
        counter = 1;
        
        while((byte_counter = inputStream.read(in, byte_counter + byte_counter, command_size_buffer - byte_counter)) <= command_size_buffer)
        {
            byte_counter += byte_counter;
        }
        for ( int i = 0; i < command_size_buffer; i++) {
            buff[counter] +=  Character.toString((char)in[i]);
            if(Character.toString((char)in[0]).contentEquals(","))
            {
                counter++;
            }
        }
        return buff;
    }
    public Customer getCustomer(String id) throws IOException
    {
        //Use the Command GC see readme for more information
        OutputStream outputStream = this.s.getOutputStream();
        outputStream.write(this.command_Formater("GC", id));
        InputStream inputStream = this.s.getInputStream();
        String[] command_Parser = this.command_Parser(inputStream);
        return new Customer(command_Parser[1],command_Parser[2],command_Parser[3],Integer.getInteger(command_Parser[4]),Integer.getInteger(command_Parser[5]),Integer.getInteger(command_Parser[6]),Integer.getInteger(command_Parser[7]));
    }
    public void Close() throws IOException {
        s.close();
    }
}
