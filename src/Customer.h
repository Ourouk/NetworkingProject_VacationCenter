#ifndef CUSTOMER
#define CUSTOMER
#include <iostream>
#include <ctime>

using namespace std;
    class Customer 
    {
        private:
            string id;
            string name;
            string surname;
            string birthDate;
            string atVacationCenter;
            //Birth date
            int_fast8_t day,month;
            int year;

        public:
            Customer();
            Customer(string);
            ~Customer();
            //Getter
            const string getId() const;
            const string getName() const;
            const string getSurname() const;
            const string getBirthDate() const;
            //TODO bool
            const string is_atVacationCenter() const;
            //Setter
            void setId(string);
            void setName(string);
            void setSurname(string);
            void setBirthDate(int_fast8_t,int_fast8_t,int);
            void setBirthDate(string,string,string);
            void setDay(int_fast8_t);
            void setMonth(int_fast8_t);
            void setYear(int);
            void setIs_atVacationCenter(string);
            void setIs_atVacationCenter(bool);

            //Others
            string to_string();
            //operator
            friend bool operator==(Customer const c1,Customer const c2)
            {
                if((c1.getId() == c2.getId()) && (c1.getName() == c2.getName()) && (c1.getSurname() == c2.getSurname()) && (c1.getBirthDate() == c2.getBirthDate())&& (c1.is_atVacationCenter() == c2.is_atVacationCenter()))
                    return true;
                else
                    return false;
            }
    };
#endif