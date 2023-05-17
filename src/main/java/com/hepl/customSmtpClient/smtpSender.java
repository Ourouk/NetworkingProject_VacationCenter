import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SmtpClient {
    private String SMTP_SERVER = "mail.example.com";
    private int SMTP_PORT = 25;
    private String sender_mail;
    private String recipient_mail;
    private String subject_mail;
    private String body_mail;
    public SmtpClient(String SMTP_SERVER,int SMTP_PORT)
    {
        this.SMTP_SERVER = SMTP_SERVER;
        this.SMTP_PORT = SMTP_PORT;
    }

    public void sendMail() {
        try {
            // Connect to the SMTP server
            Socket socket = new Socket(SMTP_SERVER, SMTP_PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            
            // Read the server's response
            String response = reader.readLine();
            System.out.println("Server response: " + response);
            
            // Send the HELO command
            sendCommand(writer, "HELO localhost");
            response = reader.readLine();
            System.out.println("Server response: " + response);
            
            // Send the MAIL FROM command
            sendCommand(writer, "MAIL FROM:<sender@example.com>");
            response = reader.readLine();
            System.out.println("Server response: " + response);
            
            // Send the RCPT TO command
            sendCommand(writer, "RCPT TO:<recipient@example.com>");
            response = reader.readLine();
            System.out.println("Server response: " + response);
            
            // Send the DATA command
            sendCommand(writer, "DATA");
            response = reader.readLine();
            System.out.println("Server response: " + response);
            
            // Send the message content
            sendCommand(writer, "Subject: Example Subject");
            sendCommand(writer, ""); // Empty line indicating end of headers
            
            // Body of the email
            sendCommand(writer, "This is the body of the email.");
            sendCommand(writer, "."); // End the message
            
            response = reader.readLine();
            System.out.println("Server response: " + response);
            
            // Send the QUIT command
            sendCommand(writer, "QUIT");
            response = reader.readLine();
            System.out.println("Server response: " + response);
            
            // Close the connection
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void sendCommand(PrintWriter writer, String command) {
        writer.println(command);
        System.out.println("Sent command: " + command);
    }

    public String getSender_mail() {
        return sender_mail;
    }

    public void setSender_mail(String sender_mail) {
        this.sender_mail = sender_mail;
    }

    public String getRecipient_mail() {
        return recipient_mail;
    }

    public void setRecipient_mail(String recipient_mail) {
        this.recipient_mail = recipient_mail;
    }

    public String getSubject_mail() {
        return subject_mail;
    }

    public void setSubject_mail(String subject_mail) {
        this.subject_mail = subject_mail;
    }

    public String getBody_mail() {
        return body_mail;
    }

    public void setBody_mail(String body_mail) {
        this.body_mail = body_mail;
    }
}