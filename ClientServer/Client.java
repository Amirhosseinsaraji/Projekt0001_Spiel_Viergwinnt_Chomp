package ClientServer;

import java.io.*;
import java.net.*;

public class Client {
    private Socket clientSocket; // Socket für die Verbindung zum Server
    private DataInputStream dataInputStream; // Eingabestream zum Empfangen von Nachrichten vom Server
    private DataOutputStream dataOutputStream; // Ausgabestream zum Senden von Nachrichten an den Server
    private BufferedReader consoleReader; // Eingabe von der Konsole
    private boolean running; // Status, ob der Client läuft

    // Verbindet den Client mit dem Server
    public void connect(String host, int port) {
        try {
            // Verbindungsaufbau zum Server
            clientSocket = new Socket(host, port);
           /* Umwandeln  PrintStream oder BufferedReader
                    DataInput und DataOutputStream
                    aufrufeb readUTF writeUTF
                    eventuell flush() aufrufen*/
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            consoleReader = new BufferedReader(new InputStreamReader(System.in));
            running = true;


            System.out.println("Verbindung zum Server hergestellt!");

            // Startet einen Thread, um Nachrichten vom Server zu empfangen
            new Thread(this::listenForServerMessages).start();

            // Bearbeitet die Benutzereingabe
            handleUserInput();
        } catch (IOException e) {
            System.out.println("Fehler beim Verbinden mit dem Server: " + e.getMessage());
        }
    }

    // Trennt die Verbindung zum Server
    public void disconnect() {
        try {
            running = false; // Beendet den Client
            if(dataInputStream != null) dataInputStream.close();
            if(dataOutputStream != null) dataOutputStream.close();
            if(clientSocket != null) clientSocket.close();
            System.out.println("Verbindung zum Server beendet.");
        } catch (IOException e) {
            System.out.println("Fehler beim Trennen der Verbindung: " + e.getMessage());
        }
    }



    // Bearbeitet die Benutzereingabe und sendet Nachrichten an den Server
    private void handleUserInput() {
        try {
            while (running) {


                String userInput = consoleReader.readLine();

                if (userInput == null || userInput.trim().isEmpty()) {
                    synchronized (System.out) {
                        System.out.println("Leere Eingabe. Bitte erneut versuchen.");
                    }
                    continue;
                }

                if (userInput.equalsIgnoreCase("exit")) {
                    disconnect();
                    break;
                }

                sendMessage(userInput.trim());
            }
        } catch (IOException e) {
            synchronized (System.out) {
                System.out.println("Fehler beim Lesen der Benutzereingabe: " + e.getMessage());
            }
        }
    }

    private void listenForServerMessages() {
        try {
            while (running) {
                String message = dataInputStream.readUTF();

                synchronized (System.out) { // Synchronisiere die Konsolenausgabe
                     System.out.println("Nachricht vom Server: " + message);

                }
            }
        } catch (IOException e) {
            synchronized (System.out) {
                System.out.println("Verbindung zum Server verloren: " + e.getMessage());
            }
            disconnect();
        }
    }

    // Sendet eine Nachricht an den Server
    private void sendMessage(String message) throws IOException {
        dataOutputStream.writeUTF(message);
        dataOutputStream.flush();
    }

    // Einstiegspunkt des Clients
    public static void main(String[] args) {
        new Thread(() -> { // Neuer Thread für den Client
            Client client = new Client();
            client.connect("localhost", 8080); // Verbindet sich mit dem Server
        }).start();

    }
}
