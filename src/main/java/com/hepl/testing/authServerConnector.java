package com.hepl.testing;
import com.hepl.Logger;
import com.hepl.customHttpServer.authServerConnector.*;
import com.hepl.customHttpServer.authServerConnector.data.Customer;

import java.io.IOException;

public class authServerConnector {
    public authServerConnector() {

        String id = "Al00";

        try {
            authClient authClient = new authClient("127.0.0.1", 5050);
            Logger.log("The socket connection is done");
            Logger.log("Trying to find the users " + id + " inside the Server Database");
            Customer customer = authClient.getCustomer(id);
            Logger.log ("<h1>Choose Activity for user : " + customer.getName() + customer.getSurname() + "</h1>" + "\r\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
