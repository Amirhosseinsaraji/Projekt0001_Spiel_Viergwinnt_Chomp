package ClientServer;

import java.io.*;
import java.net.*;

class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final Server server;
    public DataInputStream in;
    public DataOutputStream out;
    private String username;


    public ClientHandler(Socket clientSocket, Server server, DataInputStream in, DataOutputStream out) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.in = in;
        this.out = out;
    }



    @Override
    public void run() {
        try {
            // Initialisierung der Streams
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());


            // Anmeldung oder Registrierung
            server.sendMessage(out, "Willkommen! Möchten Sie sich registrieren (1) oder anmelden (2)?");

            String option = server.readMessage(in);


            if ("1".equals(option)) {
                server.handleRegistration(clientSocket, in, out);
            } else if ("2".equals(option)) {
                if (!server.handleLogin(clientSocket, in, out)) {
                    clientSocket.close();
                    return;
                }
            } else {
                server.sendMessage(out, "Ungültige Option. Verbindung wird beendet.");
                clientSocket.close();
                return;
            }

            // Kommunikation nach erfolgreicher Anmeldung
            String clientName = server.getClientName(clientSocket);
            server.sendMessage(out, "Willkommen, " + clientName + "! Sie können jetzt Nachrichten senden.");

            while (!clientSocket.isClosed()) {
                String message = server.readMessage(in);
                if ("exit".equalsIgnoreCase(message)) {
                    server.disconnectClient(clientSocket);
                    break;
                }
                server.broadcast(clientName + ": " + message,"MESSAGE");
            }
        } catch (IOException e) {
            System.out.println("Verbindung zu einem Client verloren: " + e.getMessage());
        } finally {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Fehler beim Schließen des Sockets: " + e.getMessage());
            }
            server.disconnectClient(clientSocket);
        }
    }
    public String getUsername() {
        return this.username; // oder wie der Benutzername gespeichert ist
    }


    // Nachricht an den Client senden
        public void sendMessage(String message) {
            try {
                out.writeUTF("MESSAGE");
                out.writeUTF(message);
            } catch (IOException e) {
                System.err.println("Fehler beim Senden der Nachricht: " + e.getMessage());
            }
        }

        // Methode zum Senden der Benutzerliste an einen Client
        public void sendUserList(String[] users) {
            try {
                out.writeUTF("USER_LIST_UPDATE");
                out.writeInt(users.length);
                for (String user : users) {
                    out.writeUTF(user);
                }
            } catch (IOException e) {
                System.err.println("Fehler beim Senden der Benutzerliste: " + e.getMessage());
            }
        }

    }

