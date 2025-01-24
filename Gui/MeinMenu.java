package Gui;
import game.*;



import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MeinMenu extends JFrame {
    public MeinMenu() {
        //Einstellung des Fensters
        setTitle("Spielauswahl Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new GridLayout(2, 1));
        setLocationRelativeTo(null);


        //Panel für Chomp-Spiel
        JPanel chompPanel = new JPanel(new BorderLayout());
        ImageIcon chompIcon = new ImageIcon(getClass().getResource("/image/Chomp.jpeg"));
        Image scaledChompImage = chompIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        JLabel chompImageLabel = new JLabel(new ImageIcon(scaledChompImage));
        JLabel chompTextLabel = new JLabel("Chomp", SwingConstants.CENTER);
        chompTextLabel.setFont(new Font("Arial", Font.BOLD, 16));

        chompPanel.add(chompImageLabel, BorderLayout.CENTER);
        chompPanel.add(chompTextLabel, BorderLayout.SOUTH);

        //Event: Klicken auf das Bild starte das Spiel
        chompImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int zeilen = 4;
                int spalten = 5;
                Chomp chomp = new Chomp(zeilen, spalten);
                MeinMenu.this.setVisible(false);
                MeinMenu.this.dispose();
                chomp.start();

            }
        });


        //Panel für Vier Gewinnt-Spiel
        JPanel vierGewinntPanel = new JPanel(new BorderLayout());
        JLabel vierGewinntImageLabel = new JLabel(new ImageIcon(MeinMenu.class.getResource("/image/Viergewinnt.jpeg")));
        JLabel vierGewinntTextLabel = new JLabel("Viergewinnt", SwingConstants.CENTER);
        vierGewinntTextLabel.setFont(new Font("Arial", Font.BOLD, 16));

        vierGewinntPanel.add(vierGewinntImageLabel, BorderLayout.CENTER);
        vierGewinntPanel.add(vierGewinntTextLabel, BorderLayout.SOUTH);

        //Event :Klicken auf das Bild startet das Spiel
        vierGewinntImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                VierGewinnt vierGewinnt = new VierGewinnt(new ArrayList<>());
                MeinMenu.this.setVisible(false);
                MeinMenu.this.dispose();
                new VierGewinntGUI(vierGewinnt);

            }
        });

        //Panel zum Hauptfenster hinzufuegen
        add(chompPanel);
        add(vierGewinntPanel);


        setVisible(true); // Fenster sichtbar machen
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MeinMenu::new); // Starte das Menü
    }
    }

