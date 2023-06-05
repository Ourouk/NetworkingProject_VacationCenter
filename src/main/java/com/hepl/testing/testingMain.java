package com.hepl.testing;

import com.hepl.customSmtpClient.smtpSender;

import java.io.File;
import java.nio.file.Path;

public class testingMain {
    static public void authServerConnectionTesting()
    {
        new authServerConnector();
    }
    static public void smtpClientTesting(){
        smtpSender smtp = new smtpSender("test@mail.com","Here's my test mail","",new File("camps_vacances_fr.pdf").toPath());
        Thread smtpSenderThread = new Thread(smtp);
        smtpSenderThread.run();

    };
}
