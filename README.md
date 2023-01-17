# Projet Camp de Vacance

## Protocol Specification
| Protocol Command | PacketSize | Attributes |
Protocol command 2 byte
Packet Size  4 byte
Attribute "Unlimited size" Attribute separated by ','
### GetCustomer
| GD  PacketSize | CustomerID |
### PostCustomer
| PC  PacketSize | CustomerID | CustomerName | CustomerSurname | CustomerBirthdate | CustomerPresense |
### RemoveCustomer
| RC  PacketSize | CustomerID |
### PostIdentification
| PI  PacketSize | Login | Password |
### GetResult
| GR  PacketSize | Result |
## Auth_server
This server implement all the mentioned protocol specification.
He uses a pool of four thread to manage connection. The connection should be closed as quicly as possible by the client, to free mentionned client slot.
## Auth_Client
This client implement all the mentioned protocol specification.

The client provide : 
- A Basic but functional Interface

## Website
### Index
Contain a form to enter user
### User
User info + Activities selector

### Activies Selected
Congratulate the use to have made a choice

### 404
all content not found is redirected to that pages

### not supported
if a command is not supported redirected to that pages

### Http Server
Support 
- Get -> on any files present on the www folder (binary files included exemple image) 
Limitation doesn't find type automaticly
- Post -> Can get the good pages and parses argument to be pressessed in the dynamic content manager
Limitation doesn't read any other metadata
- Respond
Read the file if it is a smart https type launch the Dynamic Content Manager
### Dynamic Content Manager

Small library that detect a insert function for example <! here is a function > and replace that content by a function as defined in the http_smartHttp_server.java
Limitation: Can be used only once by file
### Communication with Auth_server
Communicated with the server to check if the customer is available
### Communication with postgress
Get the list of activities from the server.
## Docker
I didn't want to have manage complex network configuration between virtual machine and the host. So I configured the postgresql server with docker.
### Dockerfile
#### Postgresql
I use the dockerfile to :
- Update the software on the docker
- Add a default configuration file for empty servers
#### Custom-made ones
I copy compiled files inside the docker and then run them.
- Auth_server
Wrap the server in an ubuntu docker
- Customhttpserver
Wrap the java server in an ubuntu docker
### DockerCompose
I use it to launch the whole suit of programs 
- Launch postgresql docker with data persistence.
- Launch 

        