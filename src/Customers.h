#ifndef CUSTOMERS_LIST
#define CUSTOMERS_LIST
#include "Customer.h"
#include <iostream>
#include <iterator>
#include <fstream>
#include <list>
#include <cstring>
#include <sstream>
#include <vector>


using  namespace std;
class Customers
{
    private:
    list<Customer> customers;
    //Files
    fstream data_file;
    //Encapsulated files interaction
    void save_insert(string);
    void save_delete(string);
    void eraseFileLine(string);
    void eras();

    public:
    Customers();
    Customers(const Customers&);
    ~Customers();


    //Getters
    // const list<Customer> getCustomers() const;


    //Create Serialised Data from the object
    string to_string();


    //Custom interaction with files IO
    void get(string);
    void insert(Customer);
    void modify(Customer,Customer);
    void remove(Customer);


    //Find Customer in memory
    Customer findCustomer(string);


    //Load from files
    void load();
    };
#endif