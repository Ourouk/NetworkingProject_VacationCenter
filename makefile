#Source Directory
SRC=./src
#Target Directory
TARGET=./target
LOGS = ./logs
#Compiler parameters
COMPILER_CPP= g++
CFLAGS = -g -Wall -D DEBUG

all: c_build java_build

docker_compose : all
	docker-compose -f src/docker/docker-compose.yml up -d

c_build: auth_server auth_client
java_build: customhttpserver



# Java
customhttpserver: 
	mvn package;
	cp $(TARGET)/Project1_VacationCenter2-1.0-SNAPSHOT.jar $(SRC)/docker/custom_http_server
# C++
auth_server: $(SRC)/auth_server.cpp Customer.o Customers.o Command.o aes.o hmac_sha256.o sha256.o
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/auth_server.cpp $(TARGET)/Customer.o $(TARGET)/Customers.o $(TARGET)/Command.o $(TARGET)/aes.o -o $(TARGET)/auth_server
# Dirty fix
	cp $(TARGET)/auth_server  $(SRC)/docker/auth_server/

auth_client: $(SRC)/auth_client.cpp Customer.o Customers.o Command.o aes.o aes.o hmac_sha256.o sha256.o
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/auth_client.cpp $(TARGET)/Customer.o $(TARGET)/Customers.o $(TARGET)/Command.o $(TARGET)/aes.o -o $(TARGET)/auth_client

#Libraries

Customer.o: $(SRC)/Customer.cpp $(SRC)/Customer.h
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/Customer.cpp -c -o $(TARGET)/Customer.o
Customers.o: $(SRC)/Customers.cpp $(SRC)/Customers.h 
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/Customers.cpp -c -o $(TARGET)/Customers.o
Command.o : $(SRC)/Command.cpp $(SRC)/Command.h
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/Command.cpp -c -o $(TARGET)/Command.o


#Cryptographic Libraries

sha256.o : $(SRC)/sha256.c $(SRC)/sha256.h
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/sha256.c -c -o $(TARGET)/sha256.o
hmac_sha256.o : $(SRC)/hmac_sha256.c $(SRC)/hmac_sha256.h sha256.o
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/hmac_sha256.c $(TARGET)/sha256.o -c -o $(TARGET)/hmac_sha256.o
aes.o : $(SRC)/aes.c $(SRC)/aes.h
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/aes.c -c -o $(TARGET)/aes.o

clean:
	rm -f -r $(TARGET)/*
	rm -f -r $(LOGS)/*