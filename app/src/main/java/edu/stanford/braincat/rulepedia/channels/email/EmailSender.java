package edu.stanford.braincat.rulepedia.channels.email;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

/**
 * Created by gcampagn on 5/29/15.
 */
public class EmailSender {
    private static final String USERNAME = "sabrina.assistant.app@gmail.com";
    private static final String PASSWORD = "sabrinaisanawesomeassistant";

    private static void expect(BufferedReader reader, int code) throws IOException {
        String line = reader.readLine();
        while (line.startsWith("250-")) // hack
            line = reader.readLine();

        String[] tokens = line.split("\\s+");
        if (tokens.length < 2)
            throw new IOException("Invalid response from server: " + line);

        try {
            if (Integer.parseInt(tokens[0]) != code)
                throw new IOException("Server returned code: " + tokens[0]);
        } catch(NumberFormatException e) {
            throw new IOException("Server returned something that does not parse: " + tokens[0]);
        }
    }

    public static void sendEmail(String email, String subject, String body) throws IOException {
        InetAddress address = InetAddress.getByName("smtp.gmail.com");
        try (Socket socket = SSLSocketFactory.getDefault().createSocket(address, 456)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                    expect(reader, 220);
                    writer.write("EHLO rulepedia.stanford.edu\r\n");
                    writer.flush();
                    expect(reader, 250);
                    writer.write("AUTH PLAIN " + Base64.encodeToString((USERNAME + "\0" + USERNAME + "\0" + PASSWORD).getBytes(), 0) + "\r\n");
                    writer.flush();
                    expect(reader, 235);
                    writer.write("MAIL FROM: <" + USERNAME + ">\r\n");
                    writer.flush();
                    expect(reader, 250);
                    writer.write("RCPT TO: <" + email + ">\r\n");
                    writer.flush();
                    expect(reader, 354);
                    writer.write("From: Sabrina Braincat <" + USERNAME + ">\r\n");
                    writer.write("To: <" + email + ">\r\n");
                    writer.write("Subject: " + subject + "\r\n");
                    writer.write("\r\n");
                    writer.write(body);
                    writer.write("\r\n.\r\n");
                    writer.flush();
                    expect(reader, 250);
                    writer.write("QUIT\r\n");
                    writer.flush();
                    expect(reader, 221);
                }
            }
        }
    }
}
