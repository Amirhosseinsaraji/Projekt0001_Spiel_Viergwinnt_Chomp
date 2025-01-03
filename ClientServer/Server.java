package ClientServer;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int MAX_CLIENTS = 5; // Maximale Anzahl an Verbindungen
    private ServerSocket listener; // ServerSocket für eingehende Verbindungen
    private final Map<String, String> userCredentials = new HashMap<>(); // Email-Passwort-Liste
    private final Map<String, Socket> connectedClients = Collections.synchronizedMap(new HashMap<>());

    // Startet den Server
    public void startServer(int port) throws IOException {
        listener = new ServerSocket(port); // ServerSocket initialisieren
        System.out.println("Server gestartet auf Port " + port);


        while (!listener.isClosed()) { // Solange der Server nicht gestoppt wurde
            try {
                if (connectedClients.size() >= MAX_CLIENTS) {// Prüfen, ob maximale Verbindungen erreicht sind
                    rejectConnection();
                    continue;
                }
                Socket client = listener.accept(); // Client wird hier akzeptiert
                DataInputStream in = new DataInputStream(client.getInputStream());
                DataOutputStream out = new DataOutputStream(client.getOutputStream());

                System.out.println("Neuer Client verbunden: " + client.getInetAddress());
                new ClientHandler(client, this, in, out).start();
            } catch (IOException e){
                System.out.println("Fehler bei der Verbindung: " + e.getMessage());
            }
        }
    }

    private void rejectConnection() throws IOException {
        try{
            if (listener.isClosed()) return; // Überprüfen, ob der Server noch läuft
            Socket tempClient = listener.accept();
            try (DataOutputStream out = new DataOutputStream(tempClient.getOutputStream())) {
                out.writeUTF("Maximale Anzahl an Verbindungen erreicht. Bitte später erneut versuchen.");
            }
            tempClient.close();
        } catch (IOException e) {
            System.out.println("Fehler beim Ablehnen der Verbindung: " + e.getMessage());
        }
    }


    // Beendet den Server
    public void stopServer() throws IOException {
        listener.close();
        System.out.println("Server gestoppt.");

    }



    // Führt die Registrierung durch
    public void handleRegistration(Socket client, DataInputStream in, DataOutputStream out) throws IOException {
        sendMessage(out, "Geben Sie Ihre E-Mail-Adresse ein:");
        String email = readMessage(in);

        while (!isValidEmail(email)) {
            sendMessage(out, "Ungültige E-Mail-Adresse. Bitte erneut eingeben:");
            email = readMessage(in);
        }

        sendMessage(out, "Wählen Sie einen Anzeigenamen:");
        String name = readMessage(in);

        while (email.toLowerCase().contains(name.toLowerCase())) {
            sendMessage(out, "Anzeigename darf nicht Teil der E-Mail-Adresse sein. Bitte erneut eingeben:");
            name = readMessage(in);
        }

        if (!userCredentials.containsKey(email)) {
            String password = generatePassword();
            userCredentials.put(email, password);
            sendPasswordByEmail(email, password);
            sendMessage(out, "Registrierung erfolgreich! Passwort wurde an Ihre E-Mail gesendet.");
        } else {
            sendMessage(out, "Diese E-Mail ist bereits registriert.");
        }
    }

    // Führt die Anmeldung durch
    public boolean handleLogin(Socket client, DataInputStream in, DataOutputStream out) throws IOException {
        sendMessage(out, "Geben Sie Ihre E-Mail-Adresse ein:");
        String email = readMessage(in);

        if (!userCredentials.containsKey(email)) {
            sendMessage(out, "E-Mail nicht registriert. Bitte registrieren Sie sich zuerst.");
            return false;
        }

        sendMessage(out, "Geben Sie Ihr Passwort ein:");
        String password = readMessage(in);

        if (authenticateUser(email, password)) {
            sendMessage(out, "Anmeldung erfolgreich!");
            connectedClients.put(email, client);
            sendClientList(out);
            return true;
        } else {
            sendMessage(out, "Falsches Passwort. Anmeldung fehlgeschlagen.");
            return false;
        }
    }

    // Entfernt den Client
    public void disconnectClient(Socket client) {
        connectedClients.values().remove(client);
        try {
            if (client != null && !client.isClosed()) {
                client.close();
            }
            System.out.println("Client wurde getrennt.");
        } catch (IOException e) {
            System.out.println("Fehler beim Trennen des Clients: " + e.getMessage());
        }
    }

    // Sendet eine Nachricht an alle Clients
    public void broadcast(String message) {
        for (Socket client : connectedClients.values()) {
            try {
                sendMessage(new DataOutputStream(client.getOutputStream()), message);
            } catch (IOException e) {
                System.out.println("Fehler beim Senden einer Nachricht: " + e.getMessage());
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
        System.out.println("Passwort für " + email + ": " + password);
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


    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.startServer(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}