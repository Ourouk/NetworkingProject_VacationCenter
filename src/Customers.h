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
    vector<Customer> customers;
    //Files
    fstream data_file;
    //Encapsulated files interaction
    void save_insert(string);
    void save_delete(string);
    void eraseFileLine(string);
    void erase();

    public:
    Customers();
    Customers(const Customers&);
    ~Customers();


    //Getters
    // const list<Customer> getCustomers() const;


    //Create Serialised Data from the object
    string to_string();


    //Custom interaction with files IO
    Customer get(int);
    Customer get(string);
    void insert(Customer);
    void modify(Customer,Customer);
    void remove(string);
    int size();

    //Load from files
    void load();
    };
#endif