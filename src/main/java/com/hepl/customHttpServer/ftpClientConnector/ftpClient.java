package com.hepl.customHttpServer.ftpClientConnector;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.*;

public class ftpClient implements Runnable {
    private String host;
    private String username;
    private String password;
    private String remoteFile;

    public ftpClient(String host, String username, String password, String remoteFile) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.remoteFile = remoteFile;
    }

    @Override
    public void run() {
        String localFile = "buffer_file.tmp";

        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), false));
            ftpClient.connect(host);
            ftpClient.login(username, password);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // Specify the input stream
            InputStream inputStream;
            // Specify the output file path
            String outputPath = "path/to/output/file.txt";

            try {
                // Create the input stream (replace 'inputStream' with your actual InputStream)
                inputStream = ftpClient.retrieveFileStream(this.remoteFile);

                // Create the output file
                File outputFile = new File(localFile);

                // Create the output stream to write data to the file
                OutputStream outputStream = new FileOutputStream(outputFile);

                // Create a buffer to read data from the input stream
                byte[] buffer = new byte[1024];
                int bytesRead;

                // Read from the input stream and write to the output stream
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Close the input and output streams
                inputStream.close();
                outputStream.close();

                System.out.println("File successfully written to: " + outputPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
