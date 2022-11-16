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

void command_reader(vector<string>,Customers,int);
void getData(vector<string>,Customers,int);
void postData(vector<string>,Customers,int);
void removeData(vector<string>,Customers,int);
void postIdentification(vector<string>,Customers,int);
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
    //The Command size is encoded in int -> 32bit signed
    char command_type_buffer[2],command_size_buffer[8];
    char command_type_buffer_temp[2],command_size_buffer_temp[8];
    vector<string> properties_buff;
    bool finished =false;

    while(!finished)
    {
        //Thanks to https://stackoverflow.com/questions/31867598/reading-the-maximum-size-of-an-expected-packet-from-a-c-socket for the way of handling partial message
        int recv_lenght, used_buff = 0;
        bool recieved_whole = false;
        //All Message Start by a Command key of two char
        while(!recieved_whole){
            if( (recv_lenght= recv(new_socket, command_type_buffer_temp + used_buff, sizeof(command_type_buffer) - used_buff,0)) == -1)
            {
                perror("Recv Command Type");
                exit(EXIT_FAILURE);
            }if(recv_lenght == 0 )
            {
                cout<<"Connection Closed";cout.flush();
                finished =true;
                break;
            }
            used_buff += recv_lenght;
            if(used_buff == sizeof(command_type_buffer)) 
                recieved_whole = true;
            else{
                break;
            }
        }
        //Add the command string to dispatch correctly to a functions
        properties_buff.push_back(command_type_buffer);
        //TODO Handle partial Recieved message
        if( (recv_lenght = recv(new_socket, command_size_buffer, sizeof(command_size_buffer),0)) == -1)
        {
            perror("Recv Command Size");
            exit(EXIT_FAILURE);
        }
        int command_size;
        //Read the int following the Command_Type
        command_size = ((command_size_buffer[0] << 0) & 0xFF) + ((command_size_buffer[1] << 8) & 0xFF) +  ((command_size_buffer[2] << 16) & 0xFF) +  ((command_size_buffer[3] << 24) & 0xFF);
        int command_size_without_header = command_size - sizeof(int) - 2 + 1; //Header Contain two char followed by a int
        
        //Alloc the good size
        char *command_content_buffer = (char*)(malloc( sizeof(char) * command_size_without_header));
        char *command_content_buffer_persistant = (char*)(malloc( sizeof(char) * command_size_without_header));

        //Read the whole command
        
        int buff_used = 0;
        //TODO Handle partial Recieved message
        while(!recieved_whole)
        {
            if((recv_lenght = recv(new_socket, command_content_buffer, sizeof(command_size_without_header) - buff_used,0)) == -1)
            {
                perror("Recv Command Size");
                exit(EXIT_FAILURE);
            } 

        }

        free(command_content_buffer);
        
        //Parse the command inside a vector using separator ','
        stringstream buffer_line(command_content_buffer_persistant);
        while(getline(buffer_line,propertie_buff,','))
        {
            properties_buff.push_back(propertie_buff);
        }
        free(command_content_buffer_persistant);

        //Call Appropriate Function to handle the response
        command_reader(properties_buff,Customers,server_fd);

        //reset the buffer to avoid undetermined size buffer
        properties_buff.clear();

    }
    // closing the connected socket
    close(new_socket);
    // closing the listening socket
    shutdown(server_fd, SHUT_RDWR);
    return 0;
}
//Read the command type and dispatch to the right handler
void command_reader(vector<string> s,Customers c,int socket)
{
    //GetData >> GD,customer.to_string()
    if (!strcmp((s[0]).c_str(), "GD"))
    {
        cout<<"GC";cout.flush();
        getData(s,c,socket);
    }
    //GetData >> GD,customer.to_string()
    else if (!strcmp((s[0]).c_str(), "PD"))
    {
        cout<<"PC";cout.flush();
        postData(s,c,socket);
    }
    //GetData >> GD,customer.to_string()
    else if (!strcmp((s[0]).c_str(), "RD"))
    {
        cout<<"RC";cout.flush();
        removeData(s,c,socket);
    }
    //GetData >> GD,customer.to_string()
    else if (!strcmp((s[0]).c_str(), "PI"))
    {
        cout<<"PI";cout.flush();
        postIdentification(s,c,socket);
    }  
}
void getData(vector<string> s,Customers c,int socket){

}
void postData(vector<string> s,Customers c,int socket){

}
void removeData(vector<string> s,Customers c,int socket){

}
void postIdentification(vector<string> s,Customers c,int socket){
    string command_buff = "GI,1,";
    
    if(s[1].compare("admin") && s[2].compare("admin"))
    {
        command_buff.insert(command_buff.back(),sizeof('S'),'S');
        if ( send(socket,"S", sizeof("S"), 0) == -1)
        {
            perror("Send");
            exit(EXIT_FAILURE);
        }
    }else
    {
        command_buff.insert(command_buff.back(),sizeof('E'),'E');
        if ( send(socket, "E", sizeof("E"), 0) == -1)
        {
            perror("Send");
            exit(EXIT_FAILURE);
        }
    }
    
}