package Gui;

import game.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;




public class VierGewinntGUI extends JFrame {
    private final int spalten = 7; // Anzahl der Spalten direkt in der GUI festlegen
    private final int zeilen = 6; // Anzahl der Zeilen direkt festlegen
    private final JPanel spielfeldPanel;
    private final JLabel statusLabel;
    private final JButton[] spaltenButtons;
    private VierGewinnt spiel; // Referenz auf die Spiellogik

    public VierGewinntGUI(VierGewinnt spiel) {
        this.spiel = spiel;
        spiel.setupSpiel(); // Setup nur einmal durchführen
        setTitle("Vier Gewinnt");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());


        // Statusanzeige
        statusLabel = new JLabel("Spieler 1 ist am Zug", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(statusLabel, BorderLayout.NORTH);

        // Spielfeld-Panel
        spielfeldPanel = new JPanel(new GridLayout(zeilen, spalten));
        add(spielfeldPanel, BorderLayout.CENTER);


        // Spaltenauswahl-Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, spalten));
        spaltenButtons = new JButton[spalten];


        add(buttonPanel, BorderLayout.SOUTH);


        zeichneSpielfeld();
        setVisible(true);
    }

    private void zeichneSpielfeld() {
        spielfeldPanel.removeAll();
        String[][] aktuellesSpielfeld = spiel.getSpielfeld();
        List<int[]> gewinnerFelder = spiel.getGewinnKombination();

        for (int i = 0; i < zeilen; i++) {
            for (int j = 0; j < spalten; j++) {
                final int zeile = i;
                final int spalte = j;

                JPanel zelle = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.setColor(Color.WHITE); // Hintergrundfarbe
                        g.fillRect(0, 0, getWidth(), getHeight());

                        // Prüfen, ob es ein Gewinnfeld ist
                        boolean istGewinnFeld = gewinnerFelder.stream().anyMatch(pos -> pos[0] == zeile && pos[1] == spalte);
                        if (istGewinnFeld) {
                            g.setColor(Color.YELLOW); // Gewinnfelder hervorheben
                        } else if (aktuellesSpielfeld[zeile][spalte].equals("X")) {
                            g.setColor(Color.BLUE); // Spieler 1
                        } else if (aktuellesSpielfeld[zeile][spalte].equals("O")) {
                            g.setColor(Color.RED); // Spieler 2 oder Computer
                        } else {
                            g.setColor(Color.LIGHT_GRAY); // Leere Felder
                        }

                        g.fillOval(10, 10, getWidth() - 20, getHeight() - 20); // Kreis zeichnen
                    }
                };

                zelle.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                zelle.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        macheZug(zeile, spalte);
                    }
                });

                spielfeldPanel.add(zelle);
            }
        }

        spielfeldPanel.revalidate();
        spielfeldPanel.repaint();
    }








    private void macheZug(int zeile, int spalte) {
        if (spiel.istSpielBeendet()) {
            return;
        }

        try {
            if (spiel.setzenStein(spalte, spiel.getAktuellerSpieler())) {
                zeichneSpielfeld();

                if (spiel.pruefeSieg(spiel.getAktuellerSpieler())) {
                    spielBeenden(spiel.getAktuellerSpieler().getName() + " hat gewonnen!");
                    return;
                } else if (spiel.istSpielfeldvoll()) {
                    spielBeenden("Unentschieden! Das Spielfeld ist voll.");
                    return;
                }
                // Nächster Spieler
                spiel.naechsterSpieler();
                statusLabel.setText(spiel.getAktuellerSpieler().getName() + " ist am Zug");

                if (spiel.getAktuellerSpieler().getArtDesSpieler().equals("Computer")) {
                    macheComputerZug();
                }

            } else {
                JOptionPane.showMessageDialog(this, "Diese Spalte ist voll!", "Ungültiger Zug", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Ungültiger Zug", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void macheComputerZug() {
        try {
            int spalte = spiel.findeBesteSpalte();  // Der Computer sucht die beste Spalte

            if (spiel.setzenStein(spalte, spiel.getAktuellerSpieler())) {
                zeichneSpielfeld();

                if (spiel.pruefeSieg(spiel.getAktuellerSpieler())) {
                    statusLabel.setText(spiel.getAktuellerSpieler().getName() + " hat gewonnen!");
                    deaktiviereButtons();
                    frageNachNeustart();
                    return;
                } else if (spiel.istSpielfeldvoll()) {
                    statusLabel.setText("Unentschieden! Das Spielfeld ist voll.");
                    deaktiviereButtons();
                    frageNachNeustart();
                    return;
                }

                // Nach dem Computer-Zug zum Menschen wechseln
                spiel.naechsterSpieler();
                statusLabel.setText(spiel.getAktuellerSpieler().getName() + " ist am Zug");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Fehler beim Computer-Zug!", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void frageNachNeustart() {
        int auswahl = JOptionPane.showConfirmDialog(
                this,
                "Möchtest du eine neue Runde spielen?",
                "Spiel beendet",
                JOptionPane.YES_NO_OPTION
        );

        if (auswahl == JOptionPane.YES_OPTION) {
            spiel.reset(); // Spielfeld zurücksetzen
            zeichneSpielfeld(); // GUI aktualisieren
            statusLabel.setText(spiel.getAktuellerSpieler().getName() + " ist am Zug");
        } else {
            System.exit(0); // Spiel beenden
        }
    }


    private void deaktiviereButtons() {
        if (spaltenButtons != null) {
            for (JButton button : spaltenButtons) {
                if (button != null) {
                    button.setEnabled(false); // Button deaktivieren
                }
            }
        }
    }

    private void spielBeenden(String nachricht) {
        statusLabel.setText(nachricht); // Nachricht anzeigen
        deaktiviereButtons();           // Buttons deaktivieren
        frageNachNeustart();            // Neustart oder Spielende
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VierGewinnt spiel = new VierGewinnt(new ArrayList<>());
            new VierGewinntGUI(spiel);
        });


    }
}




