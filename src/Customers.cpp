#include "Customers.h"
Customers::Customers()
{
    data_file.open("data.csv", ios::in | ios::out);
}
Customers::Customers(const Customers& c){
    this->customers = c.customers;
    data_file.open("data.csv", ios::in | ios::out);
}
Customers::~Customers()
{
   this->data_file.close();
}
void Customers::save_insert(string s)
{
    data_file<<s.c_str()<<endl;
}
void Customers::save_delete(string customer)
{
    this->eraseFileLine(customer);
}
Customer Customers::get(int i){
    return this->customers[i];
}
Customer Customers::get(string id){
    vector<Customer>::iterator ptr;
    for (ptr = customers.begin(); ptr < customers.end(); ptr++)
    {
        if(ptr.base()->getId().compare(id) == 0)
            return *(ptr.base());
    }
    return Customer();
}
//insert a customer in the vector and in the file + check if already exist
void Customers::insert(Customer c){
    for (int i = 0; i < this->customers.size(); i++)
    {
        if(this->customers[i].getId().compare(c.getId()) == 0)
        {
            this->modify(this->customers[i],c);
            return;
        }
    }
    this->save_insert(c.to_string());
    this->customers.push_back(c);
}
void Customers::modify(Customer c1,Customer c2){
            c1=c2;
            this->save_delete(c1.to_string());
            this->save_insert(c2.to_string());
}
void Customers::remove(string id){
    Customer c = get(id);
    this->save_delete(c.getId());
    vector<Customer>::iterator ptr;
    for (ptr = customers.begin(); ptr < customers.end(); ptr++)
    {
        if(ptr.base()->getId().compare(id) == 0)
        {
            this->customers.erase(ptr);
            this->save_delete(ptr.base()->to_string());
            return;
        }
    }
}
int Customers::size()
{
    return this->customers.size();
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
    std::string line;
    while(getline(this->data_file, line))
    {
        customers.push_back(line);
    }
}