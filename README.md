# Vacation Center Project

## Auth Server
This part of the project contain an authentication client and a server, that both implement the whole protocol that 
can be read below.
<details>
<summary>Protocol Specification</summary>
<br>

| Protocol Command | PacketSize | Attributes |

Protocol command =  2 byte

Packet Size  = 4 byte

Attribute "Unlimited size" Attribute separated by ','

- GetCustomer

| GD  PacketSize | CustomerID |
- PostCustomer

| PC  PacketSize | CustomerID | CustomerName | CustomerSurname | CustomerBirthdate | CustomerPresense |
- RemoveCustomer

| RC  PacketSize | CustomerID |
- PostIdentification

| PI  PacketSize | Login | Password |
- GetResult

| GR  PacketSize | Result |
- Auth_server

This server implement all the mentioned protocol specification.
He uses a pool of four thread to manage connection. The connection should be closed as quicly as possible by the client, to free mentionned client slot.
- Auth_Client

This client implement all the mentioned protocol specification.
</details>

## HTTP Server
### Supported path/protocol response
#### Active Path (Find the file)
All files in the www directory should be available
### Inactive Path (Doesn't find the file)
An error pages is directly shown
### Protocol Error
if a command is not supported redirected to a specific page

### Http Command Supported
Support 
- Get -> on any files present on the www folder (binary files included exemple image) 
Limitation doesn't find type automaticly
- Post -> Can get the good pages and parses argument to be pressessed in the dynamic content manager
Limitation doesn't read any other metadata
### Dynamic Content Manager
A shtml extensions signal to the server to look for specific String that can be read.

Small library that detect a insert function for example <! here is a function > and replace that content by a function as defined in the http_smartHttp_server.java
Limitation: Can be used only once by file
### Communication with Auth_server
Communicated with the server to check if the customer is available
### Communication with postgress
Get the list of activities from the server.
## FTP Server
The project contain too a ftp server.
### Function Available

## SMTP Client
## Docker
I didn't want to have manage complex network configuration between virtual machine and the host. So I configured the postgresql server with docker.
### Dockerfile
#### Postgresql
I use the dockerfile to :
- Add a default configuration file for empty servers
#### Custom-made ones
I copy compiled files inside the docker(ubuntu) and then run them for :
- Auth_server
- Customhttpserver


### DockerCompose
I use it to launch the whole suit of programs 
- Launch postgresql, auth_server, and the JAVA HTTP/FTP/SMTP server

        