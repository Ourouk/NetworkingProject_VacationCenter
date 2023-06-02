package com.hepl.customHttpServer.ftpClientConnector;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

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

            OutputStream outputStream = new FileOutputStream(localFile);
            boolean success = ftpClient.retrieveFile(remoteFile, outputStream);
            outputStream.close();

            if (success) {
                System.out.println("File downloaded successfully!");
            } else {
                System.out.println("File download failed!");
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
