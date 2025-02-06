package game;
import java.util.Random;
import java.util.Scanner;

public class Chomp extends Spiel {
    private final String[][] spielfeld;
    private final int zeilen;
    private final int spalten;
    private boolean spielBeendet = false;
    private int aktuellerSpielerIndex = 0;
    private Spieler spieler1;
    private Spieler spieler2;

    public Chomp(int zeilen, int spalten) {
        super(null, null);  // Keine Spieler-Liste oder spezielles Spielfeld erforderlich
        this.zeilen = zeilen;
        this.spalten = spalten;
        this.spielfeld = new String[zeilen][spalten];
        initialisiereSpielfeld();
    }
    @Override
    public void spielzug(Spieler spieler) {
        throw new UnsupportedOperationException("Für GUI-Aufrufe muss spielzug(Spieler spieler, int zeile, int spalte) verwendet werden.");
    }

    private void initialisiereSpielfeld() {
        for (int i = 0; i < zeilen; i++) {
            for (int j = 0; j < spalten; j++) {
                spielfeld[i][j] = "O"; // Jede Zelle enthält Schokolade ("O")
            }
        }
        spielfeld[0][0] = "X"; // Das verbotene Feld oben links
    }
    public void start(){
        setupSpiel();
        durchgang();
    }
    private void setupSpiel() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Spielfeld wurde vorbereitet");
        System.out.println("Waehlen Sie den Spielmodus");
        System.out.println("1 : gegen Mensch");
        System.out.println("2 : gegen Computer");
        int modus = scanner.nextInt();

        spieler1 = new Spieler("Spieler 1", "Mensch");
        if (modus == 1) {
            spieler2 = new Spieler("Spieler 2", "Mensch");
        } else {
            spieler2 = new Spieler("Computer", "Computer");
        }
        System.out.println("Spiel startet...");
    }

    public void zeigeSpielfeld() {
        for (int i = 0; i < zeilen; i++) {
            for (int j = 0; j < spalten; j++) {
                System.out.print(spielfeld[i][j] + " ");
            }
            System.out.println();
        }
    }


    public void spielzug(Spieler spieler, int zeile, int spalte) {
        if (istGueltigerZug(zeile, spalte)) {
            brettAnpassen(zeile, spalte); // Spielfeld entsprechend anpassen
            if (zeile == 0 && spalte == 0) {
                spielBeendet = true; // Spiel beendet, wenn oberstes linkes Feld gewählt wird
            }
        } else {
            throw new IllegalArgumentException("Ungültiger Zug: Zeile " + zeile + ", Spalte " + spalte);
        }
    }

    // Zug des Computers
    private void computerZug() {
        Random random = new Random();
        int zeile, spalte;

        do {
            zeile = random.nextInt(zeilen);
            spalte = random.nextInt(spalten);
        } while (!istGueltigerZug(zeile, spalte));

        System.out.println("Computer wählt Zeile " + zeile + " und Spalte " + spalte);
        brettAnpassen(zeile, spalte);

        if (zeile == 0 && spalte == 0) {
            spielBeendet = true; // Computer verliert
        }
    }

    public boolean istGueltigerZug(int zeile, int spalte) {
        return zeile >= 0 && zeile < zeilen && spalte >= 0 && spalte < spalten && !spielfeld[zeile][spalte].equals(" ");
    }

    private void brettAnpassen(int zeile, int spalte) {
        for (int i = zeile; i < zeilen; i++) {
            for (int j = spalte; j < spalten; j++) {
                spielfeld[i][j] = " "; // Feld und alle darunter/weiter rechts gelegenen Felder werden "gegessen"
            }
        }
    }

    public boolean isSpielBeendet() {
        return spielBeendet;
    }

    public String[][] getSpielfeld() {
        return spielfeld;
    }



    @Override
    public void durchgang() {
        while (!spielBeendet) {
            zeigeSpielfeld();
            Spieler aktuellerSpieler = (aktuellerSpielerIndex == 0) ? spieler1 : spieler2;

            if (aktuellerSpieler.getArtDesSpieler().equals("Computer")) {
                computerZug();
            } else {
                spielzug(aktuellerSpieler);
            }

            if (spielBeendet) {
                System.out.println("Spiel beendet. Spieler " + aktuellerSpieler.getName() + " hat verloren!");
            } else {
                aktuellerSpielerIndex = (aktuellerSpielerIndex == 0) ? 1 : 0; // Spieler wechseln
            }
        }
    }
}




