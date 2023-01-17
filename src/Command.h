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

//Encryption support
#include "aes.h"

void * CommandBuilder(string Command, string Attribute, int &lenght);
bool CommandReader(int,vector<string>&);
int recv_expectedLenght(int,char *, int);
void encrypt_string(uint8_t*, uint8_t*);
void decrypt_string(uint8_t*, uint8_t*);