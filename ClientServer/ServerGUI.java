package ClientServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI {

    private JFrame frame;        // Das Hauptfenster
    private JTextArea logArea;      // Textbereich für Logs (Serveraktivitäten)
    private JList<String> userlist;     // Liste der verbundenen Benutzer
    private DefaultListModel<String> userlistmodel;  // Modell für dynamische Benutzerliste
    private JButton startButton;         // Button zum Starten des Servers
    private JButton stopButton;
    private Server server; // Referenz auf den Server// Button zum Stoppen des Servers

    public ServerGUI() {
        initiaize();        // GUI initialisieren
    }

    private void initiaize() {
        frame = new JFrame("Server GUI");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());  // Layout setzen (BorderLayout)

        JPanel ControlPanel = new JPanel();
        startButton = new JButton("Server starten");
        stopButton = new JButton("Server stopen");
        stopButton.setEnabled(false); //Server stoppen ist anfangs deaktiviert

        ControlPanel.add(startButton); // Buttons zum Panel hinzufügen
        ControlPanel.add(stopButton);

        frame.add(ControlPanel, BorderLayout.SOUTH);

        //Textbereich fuer logs (Mitte)
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea); //Scrollbar hinzufügen
        frame.add(logScrollPane, BorderLayout.CENTER); // In der Mitte

        //Benutzerliste (Rechts)
        userlistmodel = new DefaultListModel<>();
        userlist = new JList<>(userlistmodel);
        JScrollPane userScrollPane = new JScrollPane(userlist);
        frame.add(userScrollPane, BorderLayout.EAST);

        frame.setVisible(true);

        initializelisteners();
    }


    private void initializelisteners() {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startButton.setEnabled(false);
                stopButton.setEnabled(true);

                // Starte den Server in einem separaten Thread
                new Thread(() -> {
                    try {
                        server.startServer(8080); // Server starten
                    } catch (Exception ex) {
                        logMessage("Fehler beim Starten des Servers: " + ex.getMessage());
                    }
                }).start();
            }
        });



        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopButton.setEnabled(false);
                startButton.setEnabled(true);


                // Stoppe den Server
                server.stopServer();
            }
        });
    }
    // Methode zum Hinzufügen von Logs
    public void logMessage(String message) {
        logArea.append(message + "\n");
    }



    // Methode zum Hinzufügen eines Benutzers
    public void addUser(String username) {
        userlistmodel.addElement(username);
    }

    // Methode zum Entfernen eines Benutzers
    public void removeUser(String username) {
        userlistmodel.removeElement(username);
    }


    public void setServer(Server server) {
        // Hier kannst du die Verbindung zwischen GUI und Server herstellen
        // Beispielsweise kannst du den Server speichern, um später darauf zuzugreifen
        this.server = server;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI(); // Erstelle das GUI-Objekt
            Server server = new Server(gui);
            server.setGUI(gui);
            gui.setServer(server);// Verknüpfe GUI mit dem Server
        });
    }


}
