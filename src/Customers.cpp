#include "Customers.h"
Customers::Customers()
{
    data_file.open("data.csv",ios::in | ios::out | ios::app);
}
Customers::Customers(const Customers& c){
    this->customers = c.customers;
}
Customers::~Customers()
{
    data_file.close();
}
void Customers::save_insert(string s)
{
    data_file.write(s.c_str() + '\n',s.length()+sizeof('\n'));
}
void Customers::save_delete(string s_id)
{
    Customer c = findCustomer(s_id);
    this->eraseFileLine(c.to_string());
}
Customer Customers::findCustomer(string id)
{
    list<Customer>::iterator it;
    for (it = this->customers.begin(); it != this->customers.end(); ++it)
    {
        if((*it).getId().compare(id) == 0) //If find Customer with same Id
        {
            return (*it);
        }
    }
    return Customer();
}

// https://stackoverflow.com/questions/26576714/deleting-specific-line-from-file   
void Customers::eraseFileLine(string eraseLine) {
    std::string line;

    // contents of path must be copied to a temp file then
    // renamed back to the path file
    std::ofstream temp;
    temp.open("temp.txt");

    while (getline(this->data_file, line)) {
        // write all lines to temp other than the line marked for erasing
        if (line != eraseLine)
            temp << line << std::endl;
    }
    temp.close();
}
void Customers::load()
{
    
}