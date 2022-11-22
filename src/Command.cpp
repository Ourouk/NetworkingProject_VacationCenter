#include "Command.h"
void * CommandBuilder(string Command, string Attribute, int &lenght){
    int size = Command.length() + sizeof(int) + ',' + Attribute.length() + sizeof('\0');
    char* str = (char *)malloc(size);
    strcpy(str,Command.c_str());

    //These lines write the int as 4 char
    char bytes[4];
    bytes[0] = (size >> 24) & 0xFF;
    bytes[1] = (size >> 16) & 0xFF;
    bytes[2] = (size >> 8) & 0xFF;
    bytes[3] = size & 0xFF;

    strcpy(str+2,"aaaa"); //Put for empty share inside the str
    strcat(str,Attribute.c_str());
 
    memcpy(str+2,bytes,sizeof(bytes)); //Put brut int inside the tcp packet
    lenght = size;
    return str;
}

bool CommandReader(int new_socket,vector<string> & properties_buff){
    //All Message Start by a Command key of two char
        char command_type_buffer[3];
        if(recv_expectedLenght(new_socket,command_type_buffer,2 * sizeof(char)) == 0)
            return 0;
    //Convert the raw Command values to a proper string
        command_type_buffer[2] = '\0';
    //Add the command string to dispatch correctly to a functions
        properties_buff.push_back(command_type_buffer);
    //Read the Command size
        char command_size_buffer[4];
        if(recv_expectedLenght(new_socket,command_size_buffer,4*sizeof(char)) == 0)
            return 0;

    //Read the int following the Command_Type
        int command_size;
        command_size = ((command_size_buffer[0] << 24) & 0xFF)
            + ((command_size_buffer[1] << 16) & 0xFF) 
            +  ((command_size_buffer[2] << 8) & 0xFF) 
            +  ((command_size_buffer[3]) & 0xFF);
        int command_size_without_header = command_size - sizeof(int) - 2 + 1; //Header Contain two char followed by a int
    
    //Read the whole command
        char *command_content_buffer = (char*)malloc(command_size_without_header);
        if(recv_expectedLenght(new_socket,command_content_buffer,command_size_without_header) == 0)
            return 0;
    //Parse the command inside a vector using separator ','
        stringstream buffer_line(command_content_buffer);
        string propertie_buff,line_buff;
        while(getline(buffer_line,propertie_buff,','))
        {
            properties_buff.push_back(propertie_buff);
        }
        #ifdef DEBUG
        for(int i = 0; (size_t)i > properties_buff.size(); i++)
        {
            cout << properties_buff[i];cout.flush();
        }
        cout<< endl;
        #endif
        
    //Free all the memory allocation made by recv_expectedLenght
        free(command_content_buffer);
    return 1;
}
//New way to handle whole reciev using flag instead of complex partial data recovery
// int recv_expectedLenght(int sock,char * dest, int expected_size)
// {
//     return recv(sock,dest,expected_size,MSG_WAITALL);
//      #ifdef DEBUG
//         cout << dest;
//         #endif
// }
int recv_expectedLenght(int sock,char * dest, int expected_size)
{
    int i =0;char* buf = (char*)malloc(expected_size);
    int size;
    while(i < expected_size - 1)
    {
        if((size = read(sock,buf+i,expected_size - i)) == -1)
        {
            perror("recv");
            exit(EXIT_FAILURE);
        }
        memcpy(dest+i,buf,size);
        i =+ size;
    }
    return 1;
}