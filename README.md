#Projet Camp de vacance
##Auth_server and auth_admin_client
### Detail about the protocol
Each properties are separated by a , and a full command end by /n
1. Command letter, permit to communicate the kind of data to be expected

GD - GetData
    - id
example : RD,ag00

PD - CustomerData (if the data allready exist modify it)
    - id
    - name
    - surname
    - present
    - birthday
example packet : CD,ag00,Alexandre,Gallez,1,10/11/99

RD - Remove Data
    -id

PI - Post IDentification
    - username
    - password

GI - Get Identification Answer
    - Result 