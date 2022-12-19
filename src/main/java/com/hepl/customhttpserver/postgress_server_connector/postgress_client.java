/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.customhttpserver.postgress_server_connector;
import java.sql.*;
/**
 *
 * @author Andrea
 */
// https://jdbc.postgresql.org/documentation/
public class postgress_client{
    private Connection sqlDatabaseConnection;
    public postgress_client(){
        // Connect to the database
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ALEXDB"+"?characterEncoding=utf8", "root", "rootmysql11");
        

    }
    
    public void createDatabase(String databaseName){
        
    }
    
    public void createTable(String tableName){
        
    }
    
    public void insertIntoTable(String tableName, String values){
        
    }
    
    public void selectFromTable(String tableName, String values){
        
    }
    
    public void updateTable(String tableName, String values){
        
    }
    
    public void deleteFromTable(String tableName, String values){
        
    }
    
    public void dropTable(String tableName){
        
    }
    
    public void dropDatabase(String databaseName){
        
    }
}
