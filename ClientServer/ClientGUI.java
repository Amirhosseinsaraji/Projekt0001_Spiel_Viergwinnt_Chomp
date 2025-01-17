package ClientServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientGUI {
    private JFrame frame;               // Hauptfenster
    private JTextArea messageArea;      // Bereich für empfangene Nachrichten
    private JTextField messageField;    // Eingabefeld für Nachrichten
    private JButton sendButton;         // Button zum Senden von Nachrichten
    private JTextField serverField;     // Eingabefeld für Server-IP
    private JTextField portField;       // Eingabefeld für Port
    private JButton connectButton;      // Button zum Verbinden
    private JButton disconnectButton;   // Button zum Trennen
    private JList<String> userList;     // Liste der Benutzer
    private DefaultListModel<String> userListModel; // Modell für die Benutzerliste
    private Client client;
    private boolean connected = false;

    // Referenz zur Client-Logik
    public ClientGUI() {
        // Initialisiere das Fenster
        frame = new JFrame("Client GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Nachrichtenanzeige (TextArea)
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        // Eingabefeld und Senden-Button
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Senden");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Server-Verbindung (IP, Port, Verbinden/Trennen)
        JPanel connectionPanel = new JPanel(new GridLayout(1, 4));
        serverField = new JTextField("localhost");
        portField = new JTextField("8080");
        connectButton = new JButton("Verbinden");
        disconnectButton = new JButton("Trennen");
        disconnectButton.setEnabled(false);
        connectionPanel.add(new JLabel("Server:"));
        connectionPanel.add(serverField);
        connectionPanel.add(new JLabel("Port:"));
        connectionPanel.add(portField);
        connectionPanel.add(connectButton);
        connectionPanel.add(disconnectButton);
        frame.add(connectionPanel, BorderLayout.NORTH);

        // Benutzerliste
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        frame.add(new JScrollPane(userList), BorderLayout.EAST);

        frame.setVisible(true);

        // Initialisiere die Event-Listener
        initializeListeners();
    }

    private void initializeListeners() {
        // Nachricht senden
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Verbindung herstellen
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });

        // Verbindung trennen
        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnectFromServer();
            }
        });
    }

    // Nachricht senden
    private void sendMessage() {
        if (client != null && client.isRunning()) {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                try {
                    client.sendMessage(message);
                    messageField.setText("");
                } catch (IOException e) {
                    appendMessage("Fehler beim Senden der Nachricht: " + e.getMessage());
                }
            }
        }else{
                    appendMessage("Nicht verbunden! Nachricht kann nicht gesendet werden.");
        }
    }

    private void connectToServer() {
        if (connected) {
            appendMessage("Sie sind mit Server verbunden");
            return;
        }
        String host = serverField.getText().trim();
        int port;

        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            appendMessage("Ungültige Portnummer!");
            return;
        }

        appendMessage("Verbindungsversuch zu " + host + ":" + port + " ...");

        // Verbindung in einem separaten Thread herstellen
        new Thread(() -> {
            try {
                client = new Client(this); // Client erstellen und mit der GUI verknüpfen
                client.connect(host, port); // Verbindung herstellen
                SwingUtilities.invokeLater(() -> {
                    connected = true;
                    connectButton.setEnabled(false);
                    disconnectButton.setEnabled(true);
                });
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> appendMessage("Verbindung fehlgeschlagen: " + e.getMessage()));
            }
        }).start();
    }


    // Verbindung zum Server trennen
    private void disconnectFromServer() {
        if (!connected) {
            appendMessage("Sie sind nicht mit dem Server verbunden.");
            return; // Verhindert unnötiges Trennen
        }
        new Thread(() ->{
            if (client != null) {
                client.disconnect();
                SwingUtilities.invokeLater(() -> {
                    connected = false;
                    connectButton.setEnabled(true); // Verbinden-Button aktivieren
                    disconnectButton.setEnabled(false); // Trennen-Button deaktivieren
                });
            }
        }).start();
    }


    // Nachricht zur Nachrichtenanzeige hinzufügen
    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }


    // Benutzerliste aktualisieren
    public void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                userListModel.addElement(user);
            }
        });
    }
    public void setClient(Client client) {
        this.client = client; // Verknüpft die GUI mit der Client-Instanz
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI(); // Erstellt eine Instanz der GUI
            Client client = new Client(gui);
            gui.setClient(client);

        });
    }
}
