package com.example.twinfileshare;

import com.example.twinfileshare.repository.GoogleUserCREDRepository;
import com.example.twinfileshare.service.GoogleAuthorizationService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

@Service
public class MessageSender {

    @Autowired
    private GoogleUserCREDRepository googleUserCREDRepository;

    @Autowired
    private GoogleAuthorizationService googleAuthorizationService;

    @Value("${google.oauth2.client.application-name}")
    private String googleClientAppName;

    public Message sendEmail(String fromEmailAddress, String toEmailAddress,
                             String messageSubject, String bodyText, String link)
            throws MessagingException, IOException {
        var credential = getCredential(fromEmailAddress);
        Gmail service = getGmail(credential);

        var email = getMimeMessage(fromEmailAddress, toEmailAddress, messageSubject, bodyText, link);

        var message = getMessage(email);

        try {
            message = service.users().messages().send("me", message).execute();
            System.out.println("Message id: " + message.getId());
            System.out.println(message.toPrettyString());
            return message;
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                System.err.println("Unable to send message: " + e.getDetails());
            } else {
                throw e;
            }
        }

        return null;
    }

    private Message getMessage(MimeMessage email) throws IOException, MessagingException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    private MimeMessage getMimeMessage(String fromEmailAddress, String toEmailAddress, String messageSubject, String bodyText, String link) throws MessagingException {
        bodyText = bodyText + "<br> <br> " +
                "<a href='" + link + "'>Click here to download" +
                "</a>";

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(fromEmailAddress));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(toEmailAddress));
        email.setSubject(messageSubject);
        email.setContent(bodyText, "text/html; charset=utf-8");
        return email;
    }

    private Credential getCredential(String fromEmailAddress) {
        var googleUserCRED = googleUserCREDRepository.findByEmail(fromEmailAddress);
        var credential = googleAuthorizationService.toGoogleCredential(googleUserCRED);
        return credential;
    }

    private Gmail getGmail(Credential credential) {
        Gmail service = new Gmail.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(googleClientAppName)
                .build();
        return service;
    }
}
