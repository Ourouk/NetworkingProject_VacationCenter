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

using namespace std;

int CreateSocket();
void AddCustomer(int);
void DeleteCustomer(int);
void ShowCustomerList(int);
int Connect(int);
void Disconnect(int,int);
int Menu();
void* CommandBuilder(string, string, int * );
vector<string> command_reader(string[],Customers,int);


int main(int argc,char* argv[])
{
    int client_fd;
    int socket = CreateSocket();
    int choix;
    bool fini = false;
    while(!fini)
    {
        if (argc == 2) { choix = atoi(argv[1]); fini = true; }
        else choix = Menu();
        switch(choix)
        {
            case 1 : AddCustomer(socket); break;
            case 2 : AddCustomer(socket); break;
            case 3 : DeleteCustomer(socket); break;
            case 4 : ShowCustomerList(socket); break;
            case 5 : client_fd = Connect(socket); break;
            case 6 : Disconnect(socket,client_fd); break;
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
    string id,name,surname,birthday,presence;
    cout << "Please enter Id : " << endl;cout.flush();
    cin >> id;
    cout << "Please enter name : " << endl;cout.flush();
    cin >> name;
    cout << "Please enter surname : "<< endl;cout.flush();
    cin >> surname;
    cout << "Please enter birtday : "<< endl;cout.flush();
    cin >> birthday;
    cout << "Please enter 1 or 0 if present or not : ";cout.flush();
    cin >> presence;
    //Forge the string to send
    // int stringsize = sizeof("PD,"+id+','+name+','+surname+','+birthday+','+presence) + sizeof(int);
    // string buff = "PD,"+stringsize+','+id+','+name+','+surname+','+birthday+','+presence;
    // send(sock, buff.c_str(), buff.length(), 0);
    string Command = "PC"; //Post Customer
    int *lenght;
    void *buff = CommandBuilder(Command,string(id+','+name+','+surname+','+birthday+','+presence),lenght);
    send(sock, buff ,*lenght,0);
}

void DeleteCustomer(int sock)
{
    cout<< "---------------------------------------------------------------------------"<< endl;
    cout<< "-------Remove Customer-----------------------------------------------------"<< endl;
    cout<< "---------------------------------------------------------------------------"<< endl<<endl;
    string id;
    cout << "Please enter Id : " << endl;cout.flush();
    cin >> id;
    int stringsize = sizeof("rd"+id);
    string buff = "RD"+','+ to_string(stringsize) +','+id;
    send(sock,buff.c_str(),buff.length(),0);
}
void ShowCustomerList(int sock)
{
    cout<< "---------------------------------------------------------------------------"<< endl;
    cout<< "-------Show the customer List----------------------------------------------"<< endl;
    cout<< "---------------------------------------------------------------------------"<< endl<<endl;
    string id;
    //TODO fix this string
    int stringsize = (sizeof("Gd,,all")+sizeof(int));
    string buff;
    buff = "GD," + stringsize + string(",all");
    send(sock, buff.c_str(), buff.length(), 0);
}
int Connect(int  sock)
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
    return client_fd;

    //Post Authentication
    cout<< "---------------------------------------------------------------------------"<< endl;
    cout<< "-------Login---------------------------------------------------------------"<< endl;
    cout<< "---------------------------------------------------------------------------"<< endl<<endl;
    string login,password;
    cout << "Please enter Id : " << endl;cout.flush();
    cin >> login;
    cout << "Please enter name : " << endl;cout.flush();
    cin >> password;
    int *lenght;
    void *buff = CommandBuilder(string("PI"),string(login +',' + password),lenght);
    send(sock,buff,*lenght, 0);
    
    cout<< "-----------------You're connected------------------------------------------"<< endl;
}
void * CommandBuilder(string Command, string Attribute, int *lenght){
    int size = Command.length() + sizeof(int) + ',' + Attribute.length() + sizeof('\0');
    char* str = (char *)malloc(size);
    strcpy(str,Command.c_str());

    //These lines write the int as 4 char
    char bytes[4];
    bytes[0] = (size >> 24) & 0xFF;
    bytes[1] = (size >> 16) & 0xFF;
    bytes[2] = (size >> 8) & 0xFF;
    bytes[3] = size & 0xFF;

    strcpy(str,"aaaa"); //Put for empty share inside the str
    strcat(str,",");
    strcat(str,Attribute.c_str());
 
    memcpy(str+2,bytes,sizeof(bytes)); //Put brut int inside the tcp packet
    *lenght = size;
    return str;
}
vector<string> command_reader(string s[],Customers c,int socket)
{
    //GetData >> GD,customer.to_string()
    if (!strcmp(s[0].c_str(), "GD"))
    {
        cout<<"GC";cout.flush();
    }
    //GetData >> GD,customer.to_string()    
    else if (!strcmp(s[0].c_str(), "PD"))
    {
        cout<<"PC";cout.flush();
    }
    //GetData >> GD,customer.to_string()
    else if (!strcmp(s[0].c_str(), "RD"))
    {
        cout<<"RC";cout.flush();
    }
    //GetData >> GD,customer.to_string()
    else if (!strcmp(s[0].c_str(), "PI"))
    {
        cout<<"PI";cout.flush();
    }  
}
void Disconnect(int sock, int client_fd)
{
    close(client_fd);
    cout<< "-----------------You're disconnected---------------------------------------"<< endl;
}

int Menu()
{
  cout << endl;
  cout << "---------------------------------------------------------------------------" << endl;
  cout << "--- Gestionnaire de Centre de Vacances-------------------------------------" << endl;
  cout << "----Please be connected before using features------------------------------" << endl;
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
