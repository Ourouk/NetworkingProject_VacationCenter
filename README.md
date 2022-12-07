# Projet Camp de Vacance
## Special storing CustomerID
all
start
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

## Auth_Client


## Website
### Index
Contain a form to enter user
### User
User info + Activies selector

### Activies Selected
Congrulate the use to have made a choice

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
Get the list of activities

        