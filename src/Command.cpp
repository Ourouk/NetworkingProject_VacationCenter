#include "Command.h"



//Global Key Variable Extremely Unsecure but for testing purpose
uint8_t key[16] = {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F};

//Build formatted Command and apply encryption to it.
void * CommandBuilder(string Command, string Attribute_in, int &lenght){
    //Encrypt Attribute before formatting the command
    //Padding the Attribute to a multiple of 16 bc AES-128
    int aes_imposed_size;
    if (Attribute_in.length() % 16 == 0) {
        aes_imposed_size = Attribute_in.length();
    } else {
        aes_imposed_size = Attribute_in.length() + 16;
        for (int i = 0; i < aes_imposed_size - Attribute_in.length(); i++) {
            Attribute_in += "\0"; //Empty char
        }
    }
    u_int8_t* Attribute = (u_int8_t*)malloc(aes_imposed_size);
    //Copy the content without the '\0' char
    memcpy((char*)Attribute,Attribute_in.c_str(),Attribute_in.length());
    //Encrypt Attribute
    encrypt_string(Attribute,key);

    //Formatting the command type inside
    //int size = Command.length() + sizeof(int) + aes_imposed_size;
    int size = 6 + aes_imposed_size; //Should be the same ...
    char* str = (char *)malloc(size);
    memcpy(str,Command.c_str(),2);

    //These lines write the int as 4 char
    char bytes[4];
    bytes[0] = (size >> 24) & 0xFF;
    bytes[1] = (size >> 16) & 0xFF;
    bytes[2] = (size >> 8) & 0xFF;
    bytes[3] = size & 0xFF;

    //Formatting the attribute size
    memcpy(str+2,bytes,sizeof(bytes)); 
    //Formatting the Encrypted attribute the size respect the padding.
    memcpy(str+6,(char*)Attribute,aes_imposed_size);

    //Communicate the size of the byte array to be sent
    lenght = size;

    //Note the message doesn't contain the '\0' char
    return str;
}

bool CommandReader(int new_socket,vector<string> & properties_buff){
    //All Message Start by a Command key of two char
        char command_type_buffer[3];
        if(recv_expectedLenght(new_socket,command_type_buffer,2 * sizeof(char)) == 0)
            return 0;
    //Convert the raw Command values to a proper string
        //command_type_buffer[2] = '\0'; This is legacy code should be updated ...
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
        int command_size_without_header = command_size - 6 ;//sizeof(int) - 2; //Header Contain two char followed by a int
    
    //Read the whole command
        char *command_content_buffer = (char*)malloc(command_size_without_header);
        if(recv_expectedLenght(new_socket,command_content_buffer,command_size_without_header) == 0)
            return 0;


    //Decrypt command_content_buffer using AES-128
    
        decrypt_string((uint8_t*)command_content_buffer,(uint8_t*)key);

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

void encrypt_string(uint8_t* message, uint8_t* key) {
    // Initialize AES context
    struct AES_ctx ctx;
    AES_init_ctx(&ctx, key);

    // Encrypt the message
    AES_ECB_encrypt(&ctx, message);
}

void decrypt_string(uint8_t* message, uint8_t* key) {
    // Initialize AES context
    struct AES_ctx ctx;
    AES_init_ctx(&ctx, key);

    // Decrypt the message
    AES_ECB_decrypt(&ctx, message);
}

/*int recv_expectedLenght(int sock,char * dest, int expected_size)
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
}*/

int recv_expectedLenght(int sock, char *dest, int expected_size) {
    int bytes_received = 0;
    while (bytes_received < expected_size) {
        int result = recv(sock, dest + bytes_received, expected_size - bytes_received, 0);
        if (result == -1) {
            perror("recv");
            return -1;
        } else if (result == 0) {
            printf("Connection closed by the remote host\n");
            return -1;
        }
        bytes_received += result;
    }
    return bytes_received;
}