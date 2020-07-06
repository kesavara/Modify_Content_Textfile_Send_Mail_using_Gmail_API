
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.Message;
import org.testng.annotations.Test;

import java.util.logging.*;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;


public class SendGmailAfterTextModification extends FileReadAndReplace {
    private static Logger Log
            = Logger.getLogger(SendGmailAfterTextModification.class.getName());
    private static final String APPLICATION_NAME = "SendGmailAfterTextModification";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String Email_Body = "Hi Vijay. Please find the enclosed directory in zip format. This directory contains the source code files" + "\n" +
            "****** Important Information ******" + "\n" + "For Security reasons,gmail does not allow you to download .zip files"
            + "\n" + "Please follow these two steps to open the zip file" + "\n" + "1 . Download the attachment kesav_assignment_auditoria.zi_ which is in .zi_ extension"
            + "\n" + "2. Rename the download file to kesav_assignment_auditoria.zip .(Please note we are changing .zi_ to .zip)"
            + "\n" + "If you find any difficulty .please let me know at k7raor@gmail.com . I would send you a gdrive sharable link having source code files." +
            "\n" + "Thank you," + "\n" + "kesav";
    private static final String Email_Subject = "Kesav_Assignment_Auditoria_SDET";
    private static final String To_Email = "ragiboston@gmail.com";
    private static final String From_Email = "test251191@gmail.com";

    /**
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);
    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/credentials.json";
    private static String sourceCodeDirectoryToSend = "../Kesava_SendGmail_ReplacedContent_Auditoria/kesav_assignment_auditoria.zi_";

    /**
     * Creates an authorized Credential object.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        FileInputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Test(dependsOnMethods = "Test_Compress_To_Zip_After_Text_Replcaement_In_Sample_FIle")
    public void Test_To_Send_Email_Aftter_Modification_InZip() throws IOException, GeneralSecurityException, MessagingException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the labels in the user's account.
        String user = "me";
        ListLabelsResponse listResponse = service.users().labels().list(user).execute();
        List<Label> labels = listResponse.getLabels();
        if (labels.isEmpty()) {
            Log.info("No labels found.");
        } else {
            Log.info("Labels:");
            for (Label label : labels) {
                Log.info(label.getName());
            }
        }

        MimeMessage createEmailWithAttachment = createEmailWithAttachment(To_Email, From_Email, Email_Subject, Email_Body, new File(sourceCodeDirectoryToSend));
        Message sendMail = sendMessage(service, user, createEmailWithAttachment);
        Log.info("Mail is sent successfully with the source code attachment");

    }


    /**
     * Create a message from an email.
     */
    private static Message createMessageWithEmail(MimeMessage emailContent)
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }


    private static Message sendMessage(Gmail service,
                                       String userId,
                                       MimeMessage emailContent)
            throws MessagingException, IOException {
        Message message = createMessageWithEmail(emailContent);
        message = service.users().messages().send(userId, message).execute();
        Log.info(message.toPrettyString());
        return message;
    }

    private static MimeMessage createEmailWithAttachment(String to,
                                                         String from,
                                                         String subject,
                                                         String bodyText,
                                                         File file)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(bodyText, "text/plain");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        mimeBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(file);

        mimeBodyPart.setDataHandler(new DataHandler(source));
        mimeBodyPart.setFileName(file.getName());

        multipart.addBodyPart(mimeBodyPart);
        email.setContent(multipart);

        return email;
    }

}

