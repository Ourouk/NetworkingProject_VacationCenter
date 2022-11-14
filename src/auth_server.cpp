#include <iostream>

//C Include to make network work
#include <arpa/inet.h>
#include <stdio.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>
#include <climits>

//Standart Objects 
#include "customer.h"
#include "customers.h"

void command_reader(string[],Customers,int);
void getData(string[],Customers,int);
void postData(string[],Customers,int);
void removeData(string[],Customers,int);
void postIdentification(string[],Customers,int);
void dontKnowWhatToDo(string[],Customers,int);

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
    //TODO incase these parameters in a proper config file
    string IpAdd = "127.0.0.1";
    int port = 5050;
    serv_addr.sin_family=AF_INET;//Configure the protocol used
    //Convert Properly Ip address
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
    string propertie_buff,line_buff;
    char buffer[1024],command_type_buffer[2],command_size_buffer[sizeof(int)],*command_content_buffer;
    
    vector<string> properties_buff;
    bool notfinished =false;

    while(notfinished)
    {
        //All Message Start by a Command key of two char
        if(recv(server_fd, command_type_buffer, sizeof(command_type_buffer),0)== -1);
        {
            perror("Recv Command Type");
            exit(EXIT_FAILURE);
        }
        //Add the command string to dispatch correctly to a functions
        properties_buff.push_back(command_type_buffer);
        if( recv(server_fd, command_size_buffer, sizeof(command_size_buffer),0) == -1);
        {
            perror("Recv Command Size");
            exit(EXIT_FAILURE);
        }
        command_content_buffer = (char*)(malloc( sizeof(char) * std::stoi(command_size_buffer)));
        
        if(recv(server_fd, command_content_buffer, sizeof(command_content_buffer),0) == -1);
        {
            perror("Recv Command Size");
            exit(EXIT_FAILURE);
        }
        stringstream buffer_line(command_content_buffer);
        int it = 0;
        while(getline(buffer_line,propertie_buff,','))
        {

            properties_buff.push_back(propertie_buff);
            it++;
        }
        free(command_content_buffer);
    }
    // closing the connected socket
    close(new_socket);
    // closing the listening socket
    shutdown(server_fd, SHUT_RDWR);
    return 0;
}
void command_reader(string s[],Customers c,int socket)
{
    //GetData >> GD,customer.to_string()
    if (!strcmp(s[0].c_str(), "GD"))
    {
        cout<<"GD";cout.flush();
        getData(s,c,socket);
    }
    //GetData >> GD,customer.to_string()
    else if (!strcmp(s[0].c_str(), "PD"))
    {
        cout<<"PD";cout.flush();
        postData(s,c,socket);
    }
    //GetData >> GD,customer.to_string()
    else if (!strcmp(s[0].c_str(), "RD"))
    {
        cout<<"RD";cout.flush();
        removeData(s,c,socket);
    }
    //GetData >> GD,customer.to_string()
    else if (!strcmp(s[0].c_str(), "PI"))
    {
        cout<<"PI";cout.flush();
        postIdentification(s,c,socket);
    }      
}
void getData(string s[],Customers c,int socket){

}
void postData(string s[],Customers c,int socket){

}
void removeData(string s[],Customers c,int socket){

}
void postIdentification(string s[],Customers c,int socket){

}