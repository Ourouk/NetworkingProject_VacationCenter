#include "Customer.h"
Customer::Customer()
{

}
Customer::Customer(string id,string name,string string,int_fast8_t day,int_fast8_t month,int year,bool presence)
{
    this->setId(id);
    this->setName(name);
    this->setSurname(string);
    this->setDay(day);
    this->setMonth(month);
    this->setYear(year);
    this->setIs_atVacationCenter(presence);
}
Customer::Customer(string from_string)
{
    char * content_buffer = (char*)malloc(from_string.size() + 1);
    stringstream buffer_line(content_buffer);
    string propertie_buff;
    vector<string> properties_buff;
    while(getline(buffer_line,propertie_buff,','))
    {
        properties_buff.push_back(propertie_buff);
    }
    this->setId(properties_buff[0]);
    this->setName(properties_buff[1]);
    this->setSurname(properties_buff[2]);
    this->setBirthDate(stoi(properties_buff[3]),stoi(properties_buff[4]),stoi(properties_buff[5]));
    this->setIs_atVacationCenter(stoi(properties_buff[6]));
}
Customer::~Customer()
{
    
}
 //Getter
const string Customer::getId() const
{
    return this->id;
}
const string Customer::getName() const
{
    return this->name;
}
const string Customer::getSurname() const
{
    return this->surname;
}
const string Customer::getBirthDate() const
{
    return  std::to_string(this->day) + ',' + std::to_string(this->month) + ',' + std::to_string(this->year);
}
const string Customer::is_atVacationCenter() const
{
    return this->atVacationCenter;
}
//Setter
void Customer::setId(string id)
{
    this->id = id;
}
void Customer::setName(string name)
{
    this->name = name;
}
void Customer::setSurname(string Surname)
{
    this->surname = Surname;
}
void Customer::setBirthDate(int_fast8_t day,int_fast8_t month ,int year)
{
    this->day = day;
    this->month= month;
    this->year= year;
}
void Customer::setBirthDate(string day,string month,string year)
{
    this->day = (std::stoi(day));
    this->month = (std::stoi(month));
    this->year = (std::stoi(year));
}
void Customer::setDay(int_fast8_t d)
{
    this->day = d;
}
void Customer::setMonth(int_fast8_t m)
{
    this->month = m;
}
void Customer::setYear(int y)
{
    this->year = y;
}
void Customer::setIs_atVacationCenter(bool presentVacation)
{
    this->atVacationCenter = presentVacation;
}

void Customer::setIs_atVacationCenter(string atvacationcenter)
{
    this->atVacationCenter = atvacationcenter;
}
//Others
string Customer::to_string()
{
    //Create the string in csv format
    return this->getId() + ',' + this->getName() + ',' + this->getSurname() + ',' + this->getName() + ',' + this->getBirthDate() + ',' + this->is_atVacationCenter();
}
// bool Customer::operator==(Customer const c1,Customer const c2)
