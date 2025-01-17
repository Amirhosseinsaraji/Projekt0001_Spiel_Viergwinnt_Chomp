package ClientServer;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class JavaMail {
    private final Session session;

    public JavaMail(String email, String password) {
        // SMTP-Server-Konfiguration
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Session mit Authentifizierung
        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });
    }

    public void send(String subject, String recipient, String messageBody) throws MessagingException {
        // Nachricht erstellen
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("YOUR_EMAIL@gmail.com")); // Absender
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient)); // Empfänger
        message.setSubject(subject); // Betreff
        message.setContent(messageBody, "text/html;charset=utf-8"); // Nachrichtentext

        // Nachricht senden
        Transport.send(message);
        System.out.println("E-Mail erfolgreich gesendet an: " + recipient);
    }

    public static class MainClass {
        public static void main(String[] args) {
            try {
                String email = "Amirhosseinsaraji1374@gmail.com"; // Gmail-Absender
                String password = "12345";    // Passwort des Absenders

                JavaMail javaMail = new JavaMail(email, password);
                javaMail.send("Test Email", "Amirhosseinsaraji1374@gmail.com", "<h1>Hallo Welt</h1>");
            } catch (Exception e) {
                System.out.println("Fehler beim Senden der E-Mail:");
                e.printStackTrace();
            }
        }
    }
}
