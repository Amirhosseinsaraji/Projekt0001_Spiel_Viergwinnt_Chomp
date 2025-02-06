package game;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Grafische Benutzeroberfläche für das Spiel Chomp.
 * Erstellt ein Spielfeld, das auf Klicks reagiert und den Spielstatus aktualisiert.
 */
public class ChompGUI {
    private JFrame frame; // Hauptfenster
    private JButton[][] buttons; // Spielfeld-Buttons
    private Chomp chomp; // Spiellogik
    private JLabel statusLabel; // Statusanzeige
    private Spieler spieler1;
    private Spieler spieler2;
    private int aktuellerSpielerIndex;

    /**
     * Erstellt die GUI für Chomp mit einem Spielfeld der gegebenen Größe.
     */
    public ChompGUI(int zeilen, int spalten) {
        waehleSpielmodus();
        chomp = new Chomp(zeilen, spalten);
        frame = new JFrame("Chomp - Das Schokoladenspiel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        statusLabel = new JLabel("Spiel beginnt! Spieler 1 ist am Zug.", SwingConstants.CENTER);
        frame.add(statusLabel, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridLayout(zeilen, spalten));
        buttons = new JButton[zeilen][spalten];

        // Spielfeld-Buttons initialisieren
        for (int i = 0; i < zeilen; i++) {
            for (int j = 0; j < spalten; j++) {
                buttons[i][j] = new JButton("O");
                buttons[i][j].setFont(new Font("Arial", Font.PLAIN, 20));
                final int row = i, col = j;
                buttons[i][j].addActionListener(e -> handleMove(row, col));
                panel.add(buttons[i][j]);
            }
        }

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /**
     * Fragt den Spieler, ob er gegen einen Menschen oder den Computer spielen möchte.
     */
    private void waehleSpielmodus() {
        String[] optionen = {"Gegen Mensch", "Gegen Computer"};
        int auswahl = JOptionPane.showOptionDialog(null, "Gegen wen möchten Sie spielen?", "Spielmodus wählen",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionen, optionen[0]);
        spieler1 = new Spieler("Spieler 1", "Mensch");
        spieler2 = (auswahl == 1) ? new Spieler("Computer", "Computer") : new Spieler("Spieler 2", "Mensch");
        aktuellerSpielerIndex = 0;
    }

    /**
     * Behandelt die Spielerbewegung und aktualisiert das Spielfeld.
     */
    private void handleMove(int zeile, int spalte) {
        if (!chomp.isSpielBeendet()) {
            if (chomp.istGueltigerZug(zeile, spalte)) {
                statusLabel.setText("Zug von Spieler: " + getAktuellerSpieler().getName() + " auf Position: " + zeile + ", " + spalte);
                chomp.spielzug(getAktuellerSpieler(), zeile, spalte); // Spielfeld aktualisieren
                updateBoard();
                if (chomp.isSpielBeendet()) {
                    statusLabel.setText("Spiel beendet! " + getAktuellerSpieler().getName() + " hat verloren!");
                    disableButtons();
                } else {
                    aktuellerSpielerIndex = (aktuellerSpielerIndex == 0) ? 1 : 0;
                    statusLabel.setText("Nächster Spieler: " + getAktuellerSpieler().getName());
                    if (spieler2.getArtDesSpieler().equals("Computer") && aktuellerSpielerIndex == 1) {
                        computerZug();
                    }
                }
            } else {
                statusLabel.setText("Ungültiger Zug. Bitte erneut versuchen.");
            }
        } else {
            statusLabel.setText("Das Spiel ist bereits beendet.");
        }
    }

    /**
     * Führt einen Spielzug des Computers aus.
     */
    private void computerZug() {
        Random random = new Random();
        int zeile, spalte;
        do {
            zeile = random.nextInt(buttons.length);
            spalte = random.nextInt(buttons[0].length);
        } while (!chomp.istGueltigerZug(zeile, spalte));

        statusLabel.setText("Computer zieht auf Position: " + zeile + ", " + spalte);
        handleMove(zeile, spalte);
    }

    /**
     * Aktualisiert das Spielfeld nach jedem Spielzug.
     */
    private void updateBoard() {
        String[][] spielfeld = chomp.getSpielfeld(); // Spielfeld sicher abrufen
        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons[i].length; j++) {
                if (spielfeld[i][j].equals(" ")) {
                    buttons[i][j].setEnabled(false);
                    buttons[i][j].setText(" ");
                }
            }
        }
    }

    /**
     * Deaktiviert alle Spielfeld-Buttons nach Spielende.
     */
    private void disableButtons() {
        for (JButton[] buttonRow : buttons) {
            for (JButton button : buttonRow) {
                button.setEnabled(false);
            }
        }
    }

    /**
     * Gibt den aktuellen Spieler zurück.
     */
    private Spieler getAktuellerSpieler() {
        return (aktuellerSpielerIndex == 0) ? spieler1 : spieler2;
    }

    /**
     * Startet die GUI-Anwendung.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChompGUI(5, 5));
    }
}
