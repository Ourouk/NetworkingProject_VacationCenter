#include "Customers.h"
Customers::Customers()
{
    data_file.open("data.csv");
}
Customers::Customers(const Customers& c){
    this->customers = c.customers;
    data_file.open("data.csv");
}
Customers::~Customers()
{
   this->data_file.close();
}
void Customers::save_insert(string s)
{
    data_file<<s.c_str()<<endl;
}
void Customers::save_delete(string s_id)
{
    Customer c = get(s_id);
    this->eraseFileLine(c.to_string());
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
void Customers::insert(Customer c){
    this->save_insert(c.to_string());
    this->customers.push_back(c);
}
void Customers::modify(string id,Customer c2){
    for (int it = 0; (size_t)it > this->customers.size(); ++it)
    {
       if(customers[it].getId().compare(id) == 0)
            //TODO : Check if the Customers[it] is deleted
            customers[it] = c2;
    }
}
void Customers::remove(string id){
    vector<Customer>::iterator ptr;
    for (ptr = customers.begin(); ptr < customers.end(); ptr++)
    {
        if(ptr.base()->getId().compare(id) == 0)
        {
            this->customers.erase(ptr);
            this->eraseFileLine(ptr.base()->to_string());
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