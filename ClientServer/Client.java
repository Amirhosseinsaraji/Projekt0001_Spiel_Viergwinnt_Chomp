package ClientServer;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;





    public class Client {
        private ClientGUI gui; // Referenz zur GUI
        private Socket clientSocket; // Socket für die Verbindung zum Server
        private DataInputStream dataInputStream; // Eingabestream zum Empfangen von Nachrichten vom Server
        private DataOutputStream dataOutputStream; // Ausgabestream zum Senden von Nachrichten an den Server
        private BufferedReader consoleReader; // Eingabe von der Konsole
        private boolean running; // Status, ob der Client läuft

        public Client(ClientGUI gui) {
            this.gui = gui; // Verknüpft den Client mit der GUI
        }



        // Verbindet den Client mit dem Server
        public void connect(String host, int port)throws IOException {
            try {
                // Verbindungsaufbau zum Server
                clientSocket = new Socket(host, port);
                dataInputStream = new DataInputStream(clientSocket.getInputStream());
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                consoleReader = new BufferedReader(new InputStreamReader(System.in));
                running = true;
                gui.appendMessage("Verbindung zum Server hergestellt!");

                // Startet einen Thread, um Nachrichten vom Server zu empfangen
                new Thread(this::listenForServerMessages).start();
            }catch (IOException e){
                System.out.println("Fehler beim Verbindung des Servers");
                throw e;
            }

        }

        // Trennt die Verbindung zum Server
        public void disconnect() {
            try {
                running = false; // Beendet den Client
                if (dataInputStream != null) dataInputStream.close();
                if (dataOutputStream != null) dataOutputStream.close();
                if (clientSocket != null) clientSocket.close();
                gui.appendMessage("Verbindung zum Server getrennt");
            } catch (IOException e) {
                gui.appendMessage("Fehler beim Trennen der Verbindung: " + e.getMessage());
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

        // Eingehende Nachrichten verarbeiten
        private void listenForServerMessages() {
            try {
                while (running) {
                    String message = dataInputStream.readUTF();
                    SwingUtilities.invokeLater(() -> gui.appendMessage("Nachricht vom Server: " + message ));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> gui.appendMessage("Verbindung zum Server verloren: " + e.getMessage()));
                disconnect();
            }
        }



        // Sendet eine Nachricht an den Server
        public void sendMessage(String message) throws IOException {
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        }

        // In der Client-Klasse hinzufügen
        public boolean isRunning() {
            return running;
        }
        // Benutzerliste empfangen und GUI aktualisieren
        private void receiveUserList() {
            try {
                int userCount = dataInputStream.readInt();
                List<String> users = new ArrayList<>();
                for (int i = 0; i < userCount; i++) {
                    users.add(dataInputStream.readUTF());
                }
                SwingUtilities.invokeLater(() -> gui.updateUserList(users.toArray(new String[0])));
            } catch (IOException e) {
                System.err.println("Fehler beim Empfangen der Benutzerliste: " + e.getMessage());
            }
        }



        // Einstiegspunkt des Clients
        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                ClientGUI gui = new ClientGUI(); // Erstellt GUI
                gui.appendMessage("Willkommen , Bitte verbinden Sie sich mit einem Server.");
            });
        }

        }


