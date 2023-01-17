//
// Created by ourouk on 9/01/23.
//
#include "Users.h"
#include "hmac_sha256.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Generate a random salt
void generate_salt(uint8_t* salt, size_t salt_len) {
    int i;
    for (i = 0; i < salt_len; i++) {
        salt[i] = rand();
    }
}

// Hash the provided password with the provided salt
void hash_password(const uint8_t* password, size_t password_len,
                   const uint8_t* salt, size_t salt_len,
                   uint8_t* hashed_password, size_t hashed_password_len) {
    uint8_t salted_password[password_len + salt_len];
    memcpy(salted_password, password, password_len);
    memcpy(salted_password + password_len, salt, salt_len);

    hmac_sha256(salted_password, sizeof(salted_password), hashed_password, hashed_password_len);
}

// Verify a provided password against a stored hashed password and salt
bool verify_password(const uint8_t* provided_password, size_t provided_password_len,
                     const uint8_t* stored_salt, size_t stored_salt_len,
                     const uint8_t* stored_hashed_password, size_t stored_hashed_password_len) {
    uint8_t calculated_hashed_password[SHA256_HASH_SIZE];
    size_t calculated_hashed_password_len;

    hash_password(provided_password, provided_password_len, stored_salt, stored_salt_len, calculated_hashed_password, sizeof(calculated_hashed_password));

    if(stored_hashed_password_len == calculated_hashed_password_len && memcmp(stored_hashed_password, calculated_hashed_password, stored_hashed_password_len) == 0) 
        return true;
    return false;
}
Parameters::Parameters()
{
    //Prepare the file to be read.
    data_file.open("login.csv", std::ios::in | std::ios::out);
}

std::string Parameters::authenticate(std::string in_login,std::string in_password)
{
    std::string line;
    data_file.open("login.csv", std::ios::in | std::ios::out);
    while(getline(this->data_file, line))
    {
        //Separe login & password
        size_t delimiter_pos = line.find(',');
        std::string login = line.substr(0, delimiter_pos);
        //Find login
        if(login.compare(in_login) == 0)
        {
            std::string password = line.substr(delimiter_pos + 1);

            const uint8_t* provided_password ;
            const size_t provided_password_len;
            const uint8_t* stored_salt;
            const size_t stored_salt_len;
            const uint8_t* stored_hashed_password;
            const size_t stored_hashed_password_len;
            uint8_t salted_password[provided_password_len + stored_salt_len];
            uint8_t calculated_hashed_password[SHA256_HASH_SIZE];
            size_t calculated_hashed_password_len;
            memcpy(salted_password, provided_password, provided_password_len);
            memcpy(salted_password + provided_password_len, stored_salt, stored_salt_len);
            calculated_hashed_password_len = hmac_sha256(salted_password, sizeof(salted_password), calculated_hashed_password, sizeof(calculated_hashed_password));

            //Find if password correspond
            if((stored_hashed_password_len == calculated_hashed_password_len && memcmp(stored_hashed_password, calculated_hashed_password, stored_hashed_password_len) == 0))
            {
                //Could be improved to support multiple role WILL not be implemented
                return "admin";
            }
            return "wrongPassword";
            break;
        }



        //Compare entry and file to find recurrence



    }
    return "noUser";
}
