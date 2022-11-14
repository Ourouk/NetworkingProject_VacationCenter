# Projet Camp de Vacance
## Protocol Specification
| Protocol Command | PacketSize | Attributes |
### GetCustomer
| GD  PacketSize | CustomerID |
### PostCustomer
| PD  PacketSize | CustomerID | CustomerName | CustomerSurname | CustomerBirthdate | CustomerPresense |
### RemoveCustomer
| RD  PacketSize | CustomerID |
### PostIdentification
| PI  PacketSize | Login | Password |
### GetResult
| GR  PacketSize | Result |
## Auth_server

## Auth_Client