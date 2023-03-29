/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.customHttpServer.authServerConnector;

import com.hepl.customHttpServer.authServerConnector.data.Customer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 *
 * @author Andrea
 */
public class authClient {
    private Socket s = null;
    private byte[] key = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
    public authClient(String tcp_ipaddress, int tcp_port) throws IOException
    {
        s = new Socket(tcp_ipaddress, tcp_port);
    }
    
    private byte[] command_Formatter(String Command, String content) throws UnsupportedEncodingException {
        byte[] buff = new byte[6];
        byte[] read_buff;
        read_buff = Command.getBytes("ASCII");
        buff[0] = read_buff[0];
        buff[1] = read_buff[1];
        int size;
        if(content.length() % 16 == 0)
             size = content.length(); //We ignore end of line char
        else
            size = content.length() - (content.length() % 16) + 16;
        buff[2] = (byte) ((size >> 24) & 0xFF);
        buff[3] = (byte) ((size >> 16) & 0xFF);
        buff[4] = (byte) ((size >> 8) & 0xFF);
        buff[5] = (byte) (size & 0xFF);
        try {
            read_buff = AES.encrypt(content,key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        byte[] final_buff = new byte[6 + read_buff.length];
        System.arraycopy(buff, 0, final_buff, 0, 6);
        System.arraycopy(read_buff, 0, final_buff, 6, read_buff.length);
        return final_buff;
    }
    private String[] command_Parser(InputStream inputStream) throws IOException
    {
        String[] buff = null;
        byte[] command_type = new byte[2];
        int counter = 0,byte_counter = 0;
        int command_size_buffer = 0;
        while((byte_counter = inputStream.read(command_type, byte_counter, 2 - byte_counter)) <= 2)
        {
            byte_counter += byte_counter;
        }
        for(byte i: command_type)
            buff[0] += Character.toChars(i);
        
        //reset
        command_type = new byte[4];
        counter = 0;
        
        while((byte_counter = inputStream.read(command_type, byte_counter + byte_counter, 4 - byte_counter)) <= 4)
        {
            byte_counter += byte_counter;
        }
        command_size_buffer = ((command_type[3] << 24) & 0xFF)+ ((command_type[4] << 16) & 0xFF) +  ((command_type[5] << 8) & 0xFF) +  ((command_type[6]) & 0xFF);
        int command_size_buffer_without_header = command_size_buffer - 6;
        //reset
        command_type = null;
        counter = 1;
        
        while((byte_counter = inputStream.read(command_type, byte_counter + byte_counter, command_size_buffer_without_header - byte_counter)) <= command_size_buffer_without_header)
        {
            byte_counter += byte_counter;
        }
        try {
            AES.decrypt(command_type,key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for ( int i = 0; i < command_size_buffer; i++) {
            buff[counter] +=  Character.toString((char)command_type[i]);
            if(Character.toString((char)command_type[0]).contentEquals(","))
            {
                counter++;
            }
        }
        return buff;
    }
    public Customer getCustomer(String id) throws IOException
    {
        String[] command_Parser;
        //Use the Command GC see readme for more information
        OutputStream outputStream = this.s.getOutputStream();
        InputStream inputStream = this.s.getInputStream();
        outputStream.write(this.command_Formatter("PI","admin,admin"));
        command_Parser = this.command_Parser(inputStream);
        if(command_Parser[0].contentEquals("S"))
            System.out.println("Successful connection to auth_server");
        else {
            System.out.println("Identification failed");
            return null;
        }
        outputStream.write(this.command_Formatter("GC", id));
        command_Parser = this.command_Parser(inputStream);
        return new Customer(command_Parser[1],command_Parser[2],command_Parser[3],Integer.getInteger(command_Parser[4]),Integer.getInteger(command_Parser[5]),Integer.getInteger(command_Parser[6]),Integer.getInteger(command_Parser[7]));
    }
    public void Close() throws IOException {
        s.close();
    }
}
