# Projet Camp de Vacance
## Protocol
| Protocol Command | PacketSize | Attribute |
### GetCustomer
| GD | PacketSize | CustomerID |
### PostCustomer
| PD | PacketSize | CustomerID | CustomerName | CustomerSurname | CustomerBirthdate | CustomerPresense |
### RemoveCustomer
| RD | PacketSize | CustomerID |
### PostIdentification
| PI | PacketSize | Login | Password |
### GetResult
| GR | PacketSize | Result |


<!-- GD - GetData
    - id
example : RD,ag00

PD - CustomerData (if the data allready exist modify it)
    - id
    - name
    - surname
    - birthday
    - present
example packet : CD,ag00,Alexandre,Gallez,1,10/11/99

RD - Remove Data
    -id

PI - Post IDentification
    - username
    - password

GI - Get Identification Answer
    - Result  -->
## Auth_server
## Auth_Client