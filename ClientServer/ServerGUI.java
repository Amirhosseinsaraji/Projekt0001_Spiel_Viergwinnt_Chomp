package ClientServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI {
    private JFrame frame;
    private JTextArea logArea;
    private JList<String> userlist;
    private DefaultListModel<String> userlistmodel;
    private JButton startButton;
    private JButton stopButton;

    public ServerGUI() {
        initiaize();
    }

    private void initiaize() {
        frame = new JFrame("Server GUI");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel ControlPanel = new JPanel();
        startButton = new JButton("Server starten");
        stopButton = new JButton("Server stopen");
        stopButton.setEnabled(false); //Server stoppen ist anfangs deaktiviert

        ControlPanel.add(startButton);
        ControlPanel.add(stopButton);

        frame.add(ControlPanel, BorderLayout.NORTH);

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
                logMessage("Server wird gestartet....");
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                // TODO: Logik zum Starten des Servers

            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logMessage("Server wird gestoppt...");
                stopButton.setEnabled(false);
                startButton.setEnabled(true);
                // TODO: Logik zum Stoppen des Servers
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }

}
