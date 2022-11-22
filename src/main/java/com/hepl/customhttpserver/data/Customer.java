/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hepl.customhttpserver.data;

/**
 *
 * @author Andrea
 */
public class Customer {
    private String id;
    private String name;
    private String surname;
    private int day,month,year;
    private boolean atVacationCenter;
    //Generate empty Customer
    public Customer()
    {
        
    }
    //Construct From_string a Customer
    public Customer(String s)
    {
        
    }
    //Encapsulation
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * @return the day
     */
    public int getDay() {
        return day;
    }

    /**
     * @param day the day to set
     */
    public void setDay(int day) {
        this.day = day;
    }

    /**
     * @return the month
     */
    public int getMonth() {
        return month;
    }

    /**
     * @param month the month to set
     */
    public void setMonth(int month) {
        this.month = month;
    }

    /**
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * @param year the year to set
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * @return the atVacationCenter
     */
    public boolean isAtVacationCenter() {
        return atVacationCenter;
    }

    /**
     * @param atVacationCenter the atVacationCenter to set
     */
    public void setAtVacationCenter(boolean atVacationCenter) {
        this.atVacationCenter = atVacationCenter;
    }
    public String to_string()
    {
        return this.getId()+','+this.getName()+','+this.getSurname()+','+this.getDay()+','+this.getMonth()+','+this.getYear()+','+this.isAtVacationCenter();
    }
}
