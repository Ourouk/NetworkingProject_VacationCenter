#Source Directory
SRC=./src
#Target Directory
TARGET=./build
#Compiler parameters
COMPILER_CPP= g++
CFLAGS = -g -Wall -D DEBUG

all: default 

default: auth_server auth_client

auth_server: $(SRC)/auth_server.cpp Customer.o Customers.o
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/auth_server.cpp $(TARGET)/Customer.o $(TARGET)/Customers.o -o $(TARGET)/auth_server

auth_client: $(SRC)/auth_client.cpp Customer.o Customers.o
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/auth_client.cpp $(TARGET)/Customer.o $(TARGET)/Customers.o -o $(TARGET)/auth_client

#Libraries

Customer.o: $(SRC)/Customer.cpp $(SRC)/Customer.h
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/Customer.cpp -c -o $(TARGET)/Customer.o
Customers.o: $(SRC)/Customers.cpp $(SRC)/Customers.h
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/Customers.cpp $(TARGET)/Customer.o -c -o $(TARGET)/Customers.o