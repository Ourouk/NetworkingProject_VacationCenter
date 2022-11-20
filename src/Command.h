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

void * CommandBuilder(string Command, string Attribute, int &lenght);
bool CommandReader(int,vector<string>&);
int recv_expectedLenght(int,char *, int);