#Source Directory
SRC=./src
#Target Directory
TARGET=./target
#Compiler parameters
COMPILER_CPP= g++
CFLAGS = -g -Wall -D DEBUG

all: c_build java_build

docker: c_build java_build
	docker build --pull --rm -f "src/docker/customhttpserver/customhttpserver.dockerfile" -t project1vacationcenter2_custom_http_server:latest "./";
	docker build --pull --rm -f "src/docker/auth_server/authentificationServer.dockerfile" -t project1vacationcenter2_auth_server:latest "./";
	docker build --pull --rm -f "src/docker/sql-storage/postgreSQL.dockerfile" -t project1vacationcenter2_postgresql:latest "./";
	
docker_compose : docker
	docker-compose -f src/docker/docker-compose.yml up -d
c_build: auth_server auth_client
java_build: customhttpserver



# Java
customhttpserver: 
	mvn compile;
	mvn package;
# C++
auth_server: $(SRC)/auth_server.cpp Customer.o Customers.o Command.o
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/auth_server.cpp $(TARGET)/Customer.o $(TARGET)/Customers.o $(TARGET)/Command.o $(TARGET)/aes.o -o $(TARGET)/auth_server

auth_client: $(SRC)/auth_client.cpp Customer.o Customers.o Command.o
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/auth_client.cpp $(TARGET)/Customer.o $(TARGET)/Customers.o $(TARGET)/Command.o $(TARGET)/aes.o -o $(TARGET)/auth_client

#Libraries

Customer.o: $(SRC)/Customer.cpp $(SRC)/Customer.h
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/Customer.cpp -c -o $(TARGET)/Customer.o
Customers.o: $(SRC)/Customers.cpp $(SRC)/Customers.h 
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/Customers.cpp -c -o $(TARGET)/Customers.o
Command.o : $(SRC)/Command.cpp $(SRC)/Command.h
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/Command.cpp -c -o $(TARGET)/Command.o


#Cryptographic Libraries

sha256.o : $(SRC)/sha256.cpp $(SRC)/sha256.h
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/sha256.cpp -c -o $(TARGET)/sha256.o
hmac_sha256.o : $(SRC)/hmac_sha256.cpp $(SRC)/hmac_sha256.h sha256.o
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/hmac_sha256.cpp $(TARGET)/sha256.o -c -o $(TARGET)/hmac_sha256.o
aes.o : $(SRC)/aes.c $(SRC)/aes.h
	$(COMPILER_CPP) $(CFLAGS) $(SRC)/aes.c -c -o $(TARGET)/aes.o