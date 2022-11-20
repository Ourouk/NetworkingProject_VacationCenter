#include <iostream>

//C Include to make network work
#include <stdlib.h>
#include <stdio.h>
#include <string.h> 
#include <arpa/inet.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>


//Standart object
#include "Customer.h"
#include "Customers.h"
#include "Command.h"

using namespace std;

int CreateSocket();
void AddCustomer(int);
void DeleteCustomer(int);
void ShowCustomerList(int);
int Connect(int,bool);
void Disconnect(int,int,bool);
int Menu(bool);
// void* CommandBuilder(string, string, int * );


int main(int argc,char* argv[])
{
    int client_fd;
    int socket = CreateSocket();
    int choix;
    bool fini = false,connected = false;
    while(!fini)
    {
        if (argc == 2) { choix = atoi(argv[1]); fini = true; }
        else choix = Menu(connected);
        switch(choix)
        {
            case 1 : AddCustomer(socket); break;
            case 2 : AddCustomer(socket); break;
            case 3 : DeleteCustomer(socket); break;
            case 4 : ShowCustomerList(socket); break;
            case 5 : client_fd = Connect(socket,connected); break;
            case 6 : Disconnect(socket,client_fd,connected); break;
            default : fini = true ; break;
        }
    }
    return 0;
}
int CreateSocket()
{
  int sock;
  if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0)  {
        perror("Socket failed");
        exit(EXIT_FAILURE);
    }else
      return sock;
}

void AddCustomer(int sock){
    cout<< "---------------------------------------------------------------------------"<< endl;
    cout<< "-------Add Customers-------------------------------------------------------"<< endl;
    cout<< "---------------------------------------------------------------------------"<< endl<<endl;
    string id,name,surname,day,month,year,presence;
    cout << "Please enter Id : ";cout.flush();
    cin >> id;
    cout << "Please enter name : ";cout.flush();
    cin >> name;
    cout << "Please enter surname : ";cout.flush();
    cin >> surname;
    cout << "Please enter birtday : ";cout.flush();
    cout << "d : ";cout.flush();
    cin >> day;
    cout << "m : ";cout.flush();
    cin >> month;
    cout << "y : ";cout.flush();
    cin >> year;
    cout << "Please enter 1 or 0 if present or not : ";cout.flush();
    cin >> presence;
    string Command = "PC"; //Post Customer
    int lenght;
    void *buff = CommandBuilder(Command,string(id+','+name+','+surname+','+day+','+ month+','+ year+','+presence),lenght);
    send(sock, buff ,lenght,0);
}

void DeleteCustomer(int sock)
{
    cout<< "---------------------------------------------------------------------------"<< endl;
    cout<< "-------Remove Customer-----------------------------------------------------"<< endl;
    cout<< "---------------------------------------------------------------------------"<< endl<<endl;
    string id;
    cout << "Please enter Id : " << endl;cout.flush();
    cin >> id;
    int lenght;
    void *buff = CommandBuilder("RD",id,lenght);
    send(sock,buff,lenght,0);
}
void ShowCustomerList(int sock)
{
    cout<< "---------------------------------------------------------------------------"<< endl;
    cout<< "-------Show the customer List----------------------------------------------"<< endl;
    cout<< "---------------------------------------------------------------------------"<< endl<<endl;
    int lenght;
    void * buff = CommandBuilder("GD",string("all"),lenght);
    send(sock, buff, lenght, 0);
}
int Connect(int  sock,bool connected)
{
    struct sockaddr_in sockaddr;
    string ip,port;
    int port_int;
    int client_fd;
    cout<< "---------------------------------------------------------------------------"<< endl;
    cout<< "-------Connection Wizard---------------------------------------------------"<< endl;
    cout<< "---------------------------------------------------------------------------"<< endl<<endl;
    cout<< "Please Enter the targetted IP Address (expample 127.0.0.1 ): ";
    cin >> ip;cout<<endl;
    //Convert IP address to right format
    if (inet_pton(AF_INET, ip.c_str(), &sockaddr.sin_addr)<= 0) 
    {
        perror("\nInvalid sin_addr/ sin_addr not supported \n");
        exit(EXIT_FAILURE);
    }
    cout<< "Please Enter the port (example 5050): ";
    cin >> port;
    sockaddr.sin_family=AF_INET; //IPV4
    port_int = stoi(port);
    sockaddr.sin_port = htons(port_int);
    if ((  client_fd = connect(sock, (struct sockaddr*)&sockaddr,sizeof(sockaddr))) < 0) 
    {
        perror("Connection failed");
        exit(EXIT_FAILURE);
    }

    //Post Authentication
    cout<< "---------------------------------------------------------------------------"<< endl;
    cout<< "-------Login---------------------------------------------------------------"<< endl;
    cout<< "---------------------------------------------------------------------------"<< endl<<endl;
    string login,password;
    cout << "Please enter Id : ";cout.flush();
    cin >> login;
    cout << "Please enter name : ";cout.flush();
    cin >> password;
    int lenght;
    void *buff = CommandBuilder(string("PI"),string(login +',' + password),lenght);
    send(sock,buff,lenght, 0);
    vector<string> Properties;
    if(CommandReader(sock,Properties) == 0)
        exit(EXIT_FAILURE);
    if(Properties[0].compare("GR") == 0)
        if(Properties[1].compare("S") == 0)
            connected = true;
        else
        {
            connected = false;
        }
    else{
        connected = false;
    }
    if(connected)
    cout<< "-----------------You're connected------------------------------------------"<< endl;
    return client_fd;
}

void Disconnect(int sock, int client_fd, bool connected )
{
    close(client_fd);
    cout<< "-----------------You're disconnected---------------------------------------"<< endl;
}

int Menu(bool connected)
{
  cout << endl;
  cout << "---------------------------------------------------------------------------" << endl;
  cout << "--- Gestionnaire de Centre de Vacances-------------------------------------" << endl;
  cout << "----Please be connected before using features------------"<< "connected :" << connected << endl;
  cout << " 1. Ajouts de vacancier" << endl;
  cout << " 2. Modification de vacancier" << endl;
  cout << " 3. Suppression de vacancier" << endl;
  cout << " 4. Listes des vacanciers" << endl;
  cout << " 5. Connect" << endl;
  cout << " 6. Disconnect" << endl;
  cout << " 7. Exit" << endl ;
  int ch;
  cout << "  Choix : ";
  cin >> ch;
  cin.ignore();
  return ch;
}
