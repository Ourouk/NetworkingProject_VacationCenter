//
// Created by ourouk on 9/01/23.
//

#ifndef PROJECT1_VACATIONCENTER2_PARAMETERS_H
#define PROJECT1_VACATIONCENTER2_PARAMETERS_H

#include <iostream>
#include <iterator>
#include <fstream>
#include <list>
#include <cstring>
#include <sstream>
#include <vector>


//Cryptographic libraries
#include "sha256.h"
#include "hmac_sha256.h"


class Parameters
{
private :
    std::fstream data_file;

public :
    Parameters();
    std::string authenticate(std::string,std::string);

};
#endif //PROJECT1_VACATIONCENTER2_PARAMETERS_H
