package ClientServer;

import java.io.*;
import java.net.*;

class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final Server server;
    public DataInputStream in;
    public DataOutputStream out;

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
                    server.sendMessage(out, "Verbindung wird beendet.");
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
                server.broadcast(clientName + ": " + message);
            }
        } catch (IOException e) {
            System.out.println("Verbindung zu einem Client verloren: " + e.getMessage());
        } finally {
            server.disconnectClient(clientSocket);
        }
    }
}
