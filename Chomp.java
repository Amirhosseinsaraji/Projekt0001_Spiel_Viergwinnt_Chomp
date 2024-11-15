package PraktikumJava;

import java.util.Scanner;

public class Chomp extends Spiel {
    private final String[][] spielfeld;
    private final int zeilen;
    private final int spalten;
    private boolean spielBeendet = false;
    private int aktuellerSpielerIndex = 0;

    public Chomp(int zeilen, int spalten) {
        super(null, null);  // Keine Spieler-Liste oder spezielles Spielfeld erforderlich
        this.zeilen = zeilen;
        this.spalten = spalten;
        this.spielfeld = new String[zeilen][spalten];
        initialisiereSpielfeld();
    }

    private void initialisiereSpielfeld() {
        for (int i = 0; i < zeilen; i++) {
            for (int j = 0; j < spalten; j++) {
                spielfeld[i][j] = "O"; // Jede Zelle enthält Schokolade ("O")
            }
        }
        spielfeld[0][0] = "X"; // Das verbotene Feld oben links
    }

    public void zeigeSpielfeld() {
        for (int i = 0; i < zeilen; i++) {
            for (int j = 0; j < spalten; j++) {
                System.out.print(spielfeld[i][j] + " ");
            }
            System.out.println();
        }
    }

    @Override
    public void spielzug(Spieler spieler) {
        Scanner scanner = new Scanner(System.in);
        int zeile, spalte;

        while (true) {
            System.out.print("Zeile wählen: ");
            zeile = scanner.nextInt();
            System.out.print("Spalte wählen: ");
            spalte = scanner.nextInt();

            if (istGueltigerZug(zeile, spalte)) {
                brettAnpassen(zeile, spalte);
                break;
            } else {
                System.out.println("Ungültiger Zug. Wählen Sie ein Feld innerhalb des Spielfeldes und nicht leer.");
            }
        }

        if (zeile == 0 && spalte == 0) {
            spielBeendet = true;
            System.out.println("Spieler " + spieler.getName() + " hat das verbotene Feld gewählt und verloren!");
        }
    }

    private boolean istGueltigerZug(int zeile, int spalte) {
        return zeile >= 0 && zeile < zeilen && spalte >= 0 && spalte < spalten && !spielfeld[zeile][spalte].equals(" ");
    }

    private void brettAnpassen(int zeile, int spalte) {
        for (int i = zeile; i < zeilen; i++) {
            for (int j = spalte; j < spalten; j++) {
                spielfeld[i][j] = " "; // Feld und alle darunter/weiter rechts gelegenen Felder werden "gegessen"
            }
        }
    }

    @Override
    public void durchgang() {
        Scanner scanner = new Scanner(System.in);
        Spieler spieler1 = new Spieler("Spieler 1", "Mensch");
        Spieler spieler2 = new Spieler("Spieler 2", "Mensch");

        while (!spielBeendet) {
            zeigeSpielfeld();
            Spieler aktuellerSpieler = (aktuellerSpielerIndex == 0) ? spieler1 : spieler2;
            System.out.println("Spieler " + aktuellerSpieler.getName() + " ist am Zug.");
            spielzug(aktuellerSpieler);

            if (!spielBeendet) {
                aktuellerSpielerIndex = (aktuellerSpielerIndex == 0) ? 1 : 0;
            }
        }
        scanner.close();
    }
}




