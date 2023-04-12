package com.hepl.customSmtpClient;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.sun.mail.smtp.SMTPMessage;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

public class smtpSender implements Runnable{
    public smtpSender(String host,int port,String exp,String dest,String subject,String text_body)
    {
        this.host = host;
        this.port = port;
        this.exp = exp;
        this.dest = dest;
        this.sujet = subject;
        this.texte = text_body;
    }
    private String host;
    private int port;
    private String exp;
    private String dest;
    private String sujet;
    private String texte;


    @Override
    public void run() {

        try {
            //Init a fake smtp Server
            SimpleSmtpServer server = SimpleSmtpServer.start(25);





            //************  SMTP CLIENT ************
            Properties prop = System.getProperties();
            prop.put("mail.smtp.host", host);
            System.out.println("Création d'une session mail");
            Session sess = Session.getDefaultInstance(prop, null);
            prop.list(System.out);
            try
            {
                //Create a message
                MimeMessage msg = new MimeMessage (sess);
                msg.setFrom (new InternetAddress(exp));
                msg.setRecipient (Message.RecipientType.TO, new InternetAddress (dest));
                msg.setSubject(sujet);
                msg.setText (texte);


                try {
                    //Configure a socket to send the message
                    Socket smtpSocket;
                    smtpSocket = new Socket(host, port);
                    OutputStream out = smtpSocket.getOutputStream();
                    //Send the message
                    out.write(msg.toString().getBytes());
                } catch (Exception e) {
                    System.out.println("Erreur de connexion au serveur SMTP");
                    return;
                }


            }catch (Exception e)
            {
                System.out.println("Errreur sur message : " + e.getMessage());
            }





            //Get All message received by the fake server
            List<SmtpMessage> messages =  server.getReceivedEmails();
            //Print the messages
            messages.forEach((message) -> {
                System.out.println("Message received : " + message.getBody());
            });
            //Close the fake Server
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
