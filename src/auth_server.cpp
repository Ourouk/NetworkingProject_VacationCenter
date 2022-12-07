#include <iostream>

//C Include to make network work
#include <arpa/inet.h>
#include <stdio.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>
#include <climits>

//Standart Objects 
#include "Customer.h"
#include "Customers.h"
#include "Command.h"

//Function Declaration
    int recv_expectedLenght(int ,char * , int);
    void CommandDispatcher(vector<string>,Customers,int);
    void getCustomer(vector<string>,Customers,int);
    void postData(vector<string>,Customers,int);
    void removeData(vector<string>,Customers,int);
    bool postIdentification(vector<string>,Customers,int);
    void dontKnowWhatToDo(vector<string>,Customers,int);

int main(int argc,char* argv[])
{
    cout<<"Program is starting"<<endl;cout.flush();
    Customers Customers;
    Customers.load();
    //Create a socket to recieve Data
        int server_fd, new_socket,opt =1;
        struct sockaddr_in serv_addr;
        int addrlen = sizeof(serv_addr);
        if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == -1) 
        {
            perror("Socket Creation");
            exit(EXIT_FAILURE);
        }
        cout << "Socket Creation Done" ;cout.flush();
    // Forcefully attaching socket to the port 5050
        if (setsockopt(server_fd, SOL_SOCKET,
                    SO_REUSEADDR | SO_REUSEPORT, &opt,
                    sizeof(opt))) {
            perror("setsockopt");
            exit(EXIT_FAILURE);
        }
    //Convert Properly Ip address
        //TODO incase these parameters in a proper config file
        string IpAdd = "127.0.0.1";
        int port = 5050;
        serv_addr.sin_family=AF_INET;//Configure the protocol used
        if (inet_pton(AF_INET, IpAdd.c_str(), &serv_addr.sin_addr)<= 0) 
        {
            perror("\nIp Address\n");
            exit(EXIT_FAILURE);
        }
    //Convert properly port
        serv_addr.sin_port = htons(port);
    //Bind the ip address to the socket
        if (bind(server_fd, (struct sockaddr*)&serv_addr,sizeof(serv_addr))< 0) 
        {
            perror("bind");
            exit(EXIT_FAILURE);
        }
        cout << "Socket bind to" << IpAdd << "  " << port<<endl;cout.flush();

    //Listen on the socket
        if (listen(server_fd, 1) < 0)
        {
            perror("listen");
            exit(EXIT_FAILURE);
        }

    //Accept a connection
        cout<<"Server is listening"<<endl ;cout.flush();
            if ((new_socket= accept(server_fd, (struct sockaddr*)&serv_addr,(socklen_t*)&addrlen))< 0) 
        {
            perror("Accept");
            exit(EXIT_FAILURE);
        }
        cout<<"Connection Made on socket nbr " << new_socket<<endl;cout.flush();

    //Prepare the main loop
    vector<string> properties_buff;
    CommandReader(new_socket,properties_buff);
    postIdentification(properties_buff,Customers,new_socket);
    properties_buff.clear();
    while(CommandReader(new_socket,properties_buff))
    {
        CommandDispatcher(properties_buff,Customers,new_socket);
        properties_buff.clear();
    }
    //Closing the connected socket
    close(new_socket);
    // Closing the listening socket
    shutdown(server_fd, SHUT_RDWR);
    return 0;
}
//Dispatch Command to Real Function present on the auth server and client
void CommandDispatcher(vector<string> s,Customers c,int socket)
{
    //GetCustomer >> GC,customer.to_string()
    if (!strcmp((s[0]).c_str(), "GC"))
    {
        cout<<"GC";cout.flush();
        getCustomer(s,c,socket);
    }
    //GetData >> GD,customer.to_string()
    else if (!strcmp((s[0]).c_str(), "PC"))
    {
        cout<<"PC";cout.flush();
        postData(s,c,socket);
    }
    //GetData >> GD,customer.to_string()
    else if (!strcmp((s[0]).c_str(), "RC"))
    {
        cout<<"RC";cout.flush();
        removeData(s,c,socket);
    }
}

void getCustomer(vector<string> s,Customers c,int socket){
    //Handle the ID all
    if(s[1].compare("all") == 0)
    {
        for(int i=0;i != c.size();i++)
        {
            
        }
    }else{
        Customer c_buff = c.get(s[1]);
        int lenght;
        if ( send(socket, CommandBuilder("",c_buff.to_string(),lenght),lenght, 0) == -1)
        {
            perror("Send");
            exit(EXIT_FAILURE);
        }
    }
}
void postData(vector<string> s,Customers c,int socket){
    c.insert(Customer(s[1],s[2],s[3],stoi(s[4]),stoi(s[5]),stoi(s[6]),stoi(s[7])));
}
void removeData(vector<string> s,Customers c,int socket){
    c.remove(s[1]);
}
bool postIdentification(vector<string> s,Customers c,int socket){
    string command_buff ;
    int lenght;
    void * buff;
    if((s[1].compare("admin") == 0) && (s[2].compare("admin") == 0))
    {
        command_buff = "S";
        buff = CommandBuilder("GR",command_buff,lenght);
        if ( send(socket,buff,lenght, 0) == -1)
        {
            perror("Send");
            exit(EXIT_FAILURE);
        }
        return 0;
    }else
    {
        command_buff = "E";
        buff = CommandBuilder("GR",command_buff,lenght);
        if ( send(socket, buff, lenght, 0) == -1)
        {
            perror("Send");
            exit(EXIT_FAILURE);
        }
        return 1;
    }
}