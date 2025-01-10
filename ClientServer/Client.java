package ClientServer;

import javax.swing.*;
import java.io.*;
import java.net.*;




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
                if (dataInputStream != null) dataInputStream.close();
                if (dataOutputStream != null) dataOutputStream.close();
                if (clientSocket != null) clientSocket.close();
                gui.appendMessage("Verbindung zum Server beendet.");
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

        private void listenForServerMessages() {
            try {
                while (running) {
                    String message = dataInputStream.readUTF();
                    SwingUtilities.invokeLater(() -> gui.appendMessage("Nachricht vom Server: " + message));
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


        // Einstiegspunkt des Clients
        public static void main(String[] args) {
            new Thread(() -> { // Neuer Thread für den Client
                Client client = new Client(new ClientGUI());
                try {
                    client.connect("localhost", 8080); // Verbindet sich mit dem Server
                }catch (IOException e) {
                    System.out.println("Fehler beim Verbinden mit dem Server: " + e.getMessage());
                }
            }).start();

        }
    }

