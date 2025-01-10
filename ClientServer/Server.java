package ClientServer;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.SwingUtilities;







public class Server {
    private static final int MAX_CLIENTS = 5; // Maximale Anzahl an Verbindungen
    private ServerSocket listener; // ServerSocket für eingehende Verbindungen
    private final Map<String, String> userCredentials = new HashMap<>(); // Email-Passwort-Liste
    private final Map<String, Socket> connectedClients = Collections.synchronizedMap(new HashMap<>());
    private ServerGUI gui;  // GUI wird über den Konstruktor übergeben


    public Server(ServerGUI gui) {
        this.gui = gui; // GUI-Objekt der Server-Instanz zuweisen
    }

    // Startet den Server
    public void startServer(int port) throws IOException {
        try {
            listener = new ServerSocket(port); // ServerSocket initialisieren
            gui.logMessage("Server gestartet auf Port " + port);


            while (!listener.isClosed()) { // Solange der Server nicht gestoppt wurde
                try {
                    if (connectedClients.size() >= MAX_CLIENTS) {// Prüfen, ob maximale Verbindungen erreicht sind
                        rejectConnection();
                        continue;
                    }
                    Socket client = listener.accept(); // Client wird hier akzeptiert
                    DataInputStream in = new DataInputStream(client.getInputStream());
                    DataOutputStream out = new DataOutputStream(client.getOutputStream());

                    gui.logMessage("Neuer Client verbunden: " + client.getInetAddress());
                    new ClientHandler(client, this, in, out).start();
                } catch (IOException e) {
                    gui.logMessage("Fehler bei der Verbindung: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            gui.logMessage("Fehler beim Starten des Servers: " + e.getMessage());
        }
    }

    private void rejectConnection() throws IOException {
        try {
            if (listener.isClosed()) return; // Überprüfen, ob der Server noch läuft
            Socket tempClient = listener.accept();
            try (DataOutputStream out = new DataOutputStream(tempClient.getOutputStream())) {
                out.writeUTF("Maximale Anzahl an Verbindungen erreicht. Bitte später erneut versuchen.");
            }
            tempClient.close();
        } catch (IOException e) {
            gui.logMessage("Fehler beim Ablehnen der Verbindung: " + e.getMessage());
        }
    }


    // Server stoppen
    public void stopServer() {
        try {
            if (listener != null && !listener.isClosed()) {
                listener.close();
                gui.logMessage("Server wurde gestoppt.");
            }
        } catch (IOException e) {
            gui.logMessage("Fehler beim Stoppen des Servers: " + e.getMessage());
        }
    }

    // Benutzer hinzufügen
    public void addUser(String username, Socket client) {
        connectedClients.put(username, client);
        if (gui != null) {
            gui.addUser(username); // Benutzer zur GUI-Liste hinzufügen
            gui.logMessage("Benutzer hinzugefügt: " + username);
        }
    }

    // Benutzer entfernen
    public void removeUser(String username) {
        connectedClients.remove(username);
        if (gui != null) {
            gui.removeUser(username); // Benutzer aus der GUI-Liste entfernen
            gui.logMessage("Benutzer entfernt: " + username);
        }
    }


    // Führt die Registrierung durch
    public void handleRegistration(Socket client, DataInputStream in, DataOutputStream out) {
        try {
            // Schritt 1: Benutzer auffordern, die E-Mail-Adresse einzugeben
            String email;
            while (true) {
                try {
                    sendMessage(out, "Geben Sie Ihre E-Mail-Adresse ein:");
                    email = readMessage(in);

                    // Validierung der E-Mail-Adresse
                    if (isValidEmail(email)) {
                        if (userCredentials.containsKey(email)) {
                            sendMessage(out, "Diese E-Mail ist bereits registriert. Bitte versuchen Sie es mit einer anderen.");
                        } else {
                            break; // E-Mail ist gültig und nicht registriert
                        }
                    } else {
                        sendMessage(out, "Ungültige E-Mail-Adresse. Bitte erneut eingeben.");
                    }
                } catch (IOException e) {
                    gui.logMessage("Fehler bei der Eingabe der E-Mail-Adresse: " + e.getMessage());
                    sendMessage(out, "Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.");
                    return; // Beende die Registrierung
                }
            }


            // Schritt 2: Passwort generieren und per E-Mail senden
            String generatedPassword = generatePassword();
            try {
                String username = email.split("@")[0]; // Benutzername aus der E-Mail extrahieren
                JavaMail emailService = new JavaMail(email, generatedPassword, username);
                sendMessage(out, "Ein Passwort wurde an Ihre E-Mail gesendet. Bitte geben Sie es ein, um fortzufahren:");
            } catch (Exception e) {
                gui.logMessage("Fehler beim Senden der E-Mail: " + e.getMessage());
                sendMessage(out, "Fehler beim Senden der E-Mail. Bitte versuchen Sie es später erneut.");
                return; // Beende die Registrierung, wenn das Senden fehlschlägt
            }

            // Schritt 3: Passwort eingeben und überprüfen
            String enteredPassword;
            try {
                sendMessage(out, "Geben Sie das Passwort ein, das Sie per E-Mail erhalten haben:");
                enteredPassword = readMessage(in);

                if (!generatedPassword.equals(enteredPassword)) {
                    sendMessage(out, "Das eingegebene Passwort ist falsch. Registrierung abgebrochen.");
                    return;
                }
            } catch (IOException e) {
                gui.logMessage("Fehler beim Lesen des Passworts: " + e.getMessage());
                sendMessage(out, "Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.");
                return;
            }

            // Schritt 4: Anzeigenamen auswählen
            try {
                sendMessage(out, "Wählen Sie einen Anzeigenamen:");
                String name = readMessage(in);
                while (email.toLowerCase().contains(name.toLowerCase())) {
                    sendMessage(out, "Anzeigename darf nicht Teil der E-Mail-Adresse sein. Bitte erneut eingeben:");
                    name = readMessage(in);
                }

                // Speichern der Benutzerdaten
                userCredentials.put(email, generatedPassword);
                sendMessage(out, "Registrierung erfolgreich! Sie können sich jetzt einloggen.");
            } catch (IOException e) {
                gui.logMessage("Fehler beim Eingeben des Anzeigenamens: " + e.getMessage());
                sendMessage(out, "Ein Fehler ist aufgetreten. Bitte versuchen Sie es später erneut.");
            }

        } catch (IOException e) {
            gui.logMessage("Ein allgemeiner Fehler ist aufgetreten: " + e.getMessage());
            try {
                sendMessage(out, "Ein interner Fehler ist aufgetreten. Verbindung wird beendet.");
            } catch (IOException ioException) {
                gui.logMessage("Fehler beim Senden der Fehlermeldung: " + ioException.getMessage());
            }
        }
    }

    // Führt die Anmeldung durch
    public boolean handleLogin(Socket client, DataInputStream in, DataOutputStream out) throws IOException {
        String email;

        // Schleife zur wiederholten Eingabe der E-Mail-Adresse
        while (true) {
            sendMessage(out, "Geben Sie Ihre E-Mail-Adresse ein:");
            email = readMessage(in);

            if (userCredentials.containsKey(email)) {
                break; // Wenn die E-Mail registriert ist, verlasse die Schleife
            }

            // Wenn die E-Mail nicht registriert ist
            sendMessage(out, "E-Mail nicht registriert. Möchten Sie es erneut versuchen? (Ja/Nein)");
            String retry = readMessage(in);

            if ("Nein".equalsIgnoreCase(retry)) {
                sendMessage(out, "Verbindung wird beendet.");
                return false; // Verbindung beenden, wenn der Benutzer "Nein" wählt
            }
        }

        // Passwortabfrage nach erfolgreicher E-Mail-Validierung
        sendMessage(out, "Geben Sie Ihr Passwort ein:");
        while (true) {
            String password = readMessage(in);

            if (authenticateUser(email, password)) {
                sendMessage(out, "Anmeldung erfolgreich!");
                connectedClients.put(email, client); // Client zur Liste der verbundenen Clients hinzufügen
                sendClientList(out); // Liste der aktiven Clients an den Benutzer senden
                return true; // Anmeldung erfolgreich
            } else {
                // Passwort falsch: Möglichkeit zum erneuten Versuch
                sendMessage(out, "Falsches Passwort. Möchten Sie es erneut versuchen? (Ja/Nein)");
                String retry = readMessage(in);

                if ("Nein".equalsIgnoreCase(retry)) {
                    sendMessage(out, "Verbindung wird beendet.");
                    return false; // Verbindung beenden, wenn der Benutzer "Nein" wählt
                }

                // Benutzer wählt "Ja", Passwort erneut eingeben
                sendMessage(out, "Bitte geben Sie Ihr Passwort erneut ein:");
            }
        }
    }


    // Entfernt den Client
    public void disconnectClient(Socket client) {
        connectedClients.values().remove(client);
        try {
            if (client != null && !client.isClosed()) {
                client.close();
            }
            gui.logMessage("Client wurde getrennt.");
        } catch (IOException e) {
            gui.logMessage("Fehler beim Trennen des Clients: " + e.getMessage());
        }
    }

    // Sendet eine Nachricht an alle Clients
    public void broadcast(String message) {
        for (Socket client : connectedClients.values()) {
            try {
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                out.writeUTF(message);
            } catch (IOException e) {
                gui.logMessage("Fehler beim Senden der Nachricht: " + e.getMessage());
            }
        }
    }



    // Sendet eine Nachricht
    public void sendMessage(DataOutputStream out, String message) throws IOException {
        out.writeUTF(message);
        out.flush();
    }

    // Liest eine Nachricht
    public String readMessage(DataInputStream in) throws IOException {
        return in.readUTF();

    }

    // Prüft das E-Mail-Format
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    // Prüft Benutzeranmeldung
    private boolean authenticateUser(String email, String password) {
        return userCredentials.getOrDefault(email, "").equals(password);
    }

    // Generiert ein zufälliges Passwort
    private String generatePassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // Simuliert den E-Mail-Versand
    private void sendPasswordByEmail(String email, String password) {
        // SMTP-Server-Einstellungen
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // Authentifizierung mit der Absender-E-Mail und dem Passwort
        final String senderEmail = "your-email@gmail.com"; // Deine E-Mail-Adresse
        final String senderPassword = "your-email-password"; // Dein Passwort

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // E-Mail erstellen
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Ihr Passwort für VierGewinnt");
            message.setText("Hallo,\n\nIhr Passwort lautet: " + password + "\n\nViel Spaß beim Spielen!");


            // E-Mail senden
            Transport.send(message);
            gui.logMessage("E-Mail wurde erfolgreich an " + email + " gesendet.");
        } catch (MessagingException e) {
            e.printStackTrace();
            gui.logMessage("Fehler beim Senden der E-Mail: " + e.getMessage());
        }

    }

    // Gibt den Client-Namen zurück
    public String getClientName(Socket client) {
        for (Map.Entry<String, Socket> entry : connectedClients.entrySet()) {
            if (entry.getValue().equals(client)) {
                return entry.getKey();
            }
        }
        return "Unbekannt";
    }
    // Sendet die Liste der aktiven Clients an den verbundenen Client
    private void sendClientList(DataOutputStream out) throws IOException {
        StringBuilder clientList = new StringBuilder("Aktive Clients:\n");
        for (String clientName : connectedClients.keySet()) {
            clientList.append("- ").append(clientName).append("\n");
        }
        sendMessage(out, clientList.toString());
    }

    public void setGUI(ServerGUI gui) {
        this.gui = gui;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI(); // Erstelle das GUI-Objekt
            Server server = new Server(gui);
            gui.setServer(server);
        });
    }
}