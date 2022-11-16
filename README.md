# Projet Camp de Vacance
## Protocol Specification
| Protocol Command | PacketSize | Attributes |
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