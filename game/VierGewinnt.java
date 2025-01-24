package game;
import util.Protokollierbar;

import java.util.*;
import javax.swing.JOptionPane;



public class VierGewinnt extends Spiel implements Protokollierbar {
    private Stack<int[]> spielzuege = new Stack<>(); //LIFO, Historie der Zuege haben , die Zuege zuruecknehmen
    private String[][] spielfeld;
    private final int zeilen = 6;
    private final int spalten = 7;


    public VierGewinnt(List<Spieler> Spieler) {
        super(new ArrayList<>(), null);
        this.spielfeld = new String[zeilen][spalten];
        initialiesiereSpielfeld();
    }
    public void setupSpiel(){
        // Gegnerauswahl über JOptionPane
        String[] options = {"1: Computer", "2: Mensch"};
        String gegnerTyp = (String) JOptionPane.showInputDialog(
                null,
                "Wählen Sie Ihren Gegner:",
                "Gegnerauswahl",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        spieler.add(new Spieler("Mensch", "Mensch")); // Spieler 1 ist der Mensch


        if (gegnerTyp != null && gegnerTyp.startsWith("1")) {
            spieler.add(new Spieler("Computer", "Computer"));
        } else if (gegnerTyp != null && gegnerTyp.startsWith("2")) {
            spieler.add(new Spieler("Mensch 2", "Mensch"));
        } else {
            zeigeStatusInGui("Ungueltige Auswahl.Der Gegner wird als Computer gesetzt.");
            spieler.add(new Spieler("Computer", "Computer"));
        }
    }
    public void start() {
        zeigeStatusInGui("Spiel startet...");
        setupSpiel();
        durchgang();
    }

    //Methode initialisierung des Spielfeldes
    private void initialiesiereSpielfeld() {
        for (int i = 0; i < zeilen; i++) {
            for (int j = 0; j < spalten; j++) {
                this.spielfeld[i][j] = "-";// Füllt das Spielfeld mit leeren Zeichen
            }
        }
    }

    //Methode zum Hinzufuegen eines Spielzuges im Stack
    @Override
    public void spielzugHinzufuegen(int zeile, int spalte, Spieler spieler) {
        spielzuege.push(new int[]{zeile, spalte});
    }

    //Methode zum Entfernen des letzten Spielzuges aus dem Stack
    @Override
    public void spielzugEntfernen() {
        if (!spielzuege.empty()) {
            int[] letzterzug = spielzuege.pop();
            int zeile = letzterzug[0];
            int spalte = letzterzug[1];
            spielfeld[zeile][spalte] = "-";
        }
    }





    @Override
    public void spielzug(Spieler spieler) {
        Scanner scanner = new Scanner(System.in);
        int spalte;
        if (spieler.getArtDesSpieler().equals("Computer")) {
            spalte = findeBesteSpalte();
            System.out.println("Computer setzt in Spalte " + spalte);
        } else {
            // Menschlicher Spieler wählt eine Spalte
            while (true) {
                try {
                    System.out.print("Spieler " + spieler.getName() + ", wählen Sie eine Spalte (zwischen 0 und " + (spalten - 1) + "): ");
                    spalte = scanner.nextInt();
                    // Überprüfen, ob die Spalte im gültigen Bereich liegt, aber das ist nicht notwendig, weil wir in GUI das durchfuehren möchten
                    if (spalte >= 0 && spalte < spalten && istSpalteVerfuegbar(spalte)) {
                        break; // Gültige und verfügbare Spalte gefunden
                    } else {
                        System.out.println("Spalte " + spalte + " ist voll. Wählen Sie eine andere Spalte.");
                    }
                } catch (InputMismatchException e){
                    System.out.println("Ungültige Spaltennummer. Bitte eine Zahl zwischen 0 und " + (spalten - 1) + " eingeben.");
                    scanner.next();
                }
            }
        }

        // Stein setzen, nachdem eine gültige und verfügbare Spalte ausgewählt wurde
        if (setzenStein(spalte, spieler)) {
            spielzugHinzufuegen(findZeileInSpalte(spalte), spalte, spieler);
        }
    }



    @Override
    public void durchgang() {
        boolean spielBeendet = false;
        int aktuellerSpielerIndex = 0;

        while (!spielBeendet) {
            Spieler aktuellerSpieler = spieler.get(aktuellerSpielerIndex);
            zeigeSpielfeld();
            spielzug(aktuellerSpieler);
            if (pruefeSieg(aktuellerSpieler)) {
                zeigeSpielfeld();
                System.out.println("Spieler" + " " + aktuellerSpieler.getName()+ "" + "hat gewonnen");
                spielBeendet = true; // Spiel beenden, wenn ein Spieler gewinnt
            } else if (istSpielfeldvoll()) {
                zeigeSpielfeld();
                System.out.println("Untentschieden! Das Spielfeld ist voll");
                spielBeendet = true;
            } else {
                // Wechsel zum nächsten Spieler (zwischen Spieler 0 und Spieler 1)
                if (aktuellerSpielerIndex == 0) {
                    aktuellerSpielerIndex = 1;
                } else {
                    aktuellerSpielerIndex = 0;
                }
            }
        }
    }





    public boolean istSpielfeldvoll() {
        for(int i = 0 ;i < spalten; i++){
            if (spielfeld[0][i].equals("-")){
                return false;
            }
        }
        return true;
    }

    public void naechsterSpieler() {
        aktuellerSpielerIndex = (aktuellerSpielerIndex + 1) % spieler.size();
    }



    private int findZeileInSpalte(int spalte) {
        // Durchläuft die Zeilen von unten nach oben
        for (int i = zeilen - 1; i >= 0; i--) {
            if (spielfeld[i][spalte].equals("-")) { // Überprüft, ob das Feld leer ist
                return i; // Gibt die Zeile zurück, in der der Stein platziert werden kann
            }
        }
        // Wenn die gesamte Spalte voll ist, gibt sie -1 zurück
        return -1;
    }

    private boolean setzenStein(int spalte, Spieler spieler) {
        for(int i = zeilen - 1; i >= 0; i--){
            if(spielfeld[i][spalte].equals("-")){

                spielfeld[i][spalte]= spieler.getName(); // Setspielfeld[i][spalte] = zt den Namen des Spielers
                return true;
            }

        }
        return false;
    }

    private boolean istSpalteVerfuegbar(int spalte){
        return spielfeld[0][spalte].equals("-");
    }

    private int findeBesteSpalte(){

        // versuche ,eine Gewinnspalte fuer den Computer zu finden
        for(int i = 0; i < spalten; i++ ){
            if (istSpalteVerfuegbar(i) && pruefeGewinnmoeglichkeit(i, spieler.get(1).getName())) {
                return i; // Wähle diese Spalte, um zu gewinnen
            }
        }
        // versuche den Gegner zu blockieren
        String gegnerName = spieler.get(0).getName();
        for (int i = 0; i < spalten; i++) {
            if (istSpalteVerfuegbar(i) && pruefeGewinnmoeglichkeit(i, gegnerName)) {
                return i; // Wähle diese Spalte, um den Gegner zu blockieren
            }
        }
        // waehle eine zufaellige verfuegbare Spalte , wenn keine Gewinn oder Blockierspalte gefunden wird
        Random random = new Random();
        int spalte ;
        do {
            spalte = random.nextInt(spalten);
        }while (!istSpalteVerfuegbar(spalte));
        return  spalte;



    }

    private boolean pruefeGewinnmoeglichkeit(int spalte , String name) {
        int zeile = findZeileInSpalte(spalte);
        if (zeile == -1){
            return false;
        }// Wenn die Spalte voll ist, gibt es keine Gewinnmöglichkeit

        // Temporär den Namen in der gefundenen Zeile und Spalte setzen, um zu testen
        spielfeld[zeile][spalte] = name;

        // Überprüfen, ob das Setzen des Steins zum Gewinn führt.
        boolean gewinnt =pruefeSieg(new Spieler("Computer", "Computer"));

        // Entferne den Namen wieder, um das Spielfeld zurückzusetzen
        spielfeld[zeile][spalte] = "-";

        return gewinnt;
    }

    public boolean pruefeSieg(Spieler spieler) {
        String symbol = spieler.getName();

        //Horizontale Gewinnueberpruefung
        for (int i = 0; i < zeilen; i++){
            for(int j = 0; j < spalten - 3; j++ ){
                if (spielfeld[i][j].equals(symbol) && spielfeld[i][j+1].equals(symbol) &&
                        spielfeld[i][j+2].equals(symbol) && spielfeld[i][j+3].equals(symbol)){
                    return true;
                }
            }
        }

        // Vertikale Gewinnüberprüfung
        for (int i = 0; i < zeilen - 3; i++) {
            for (int j = 0; j < spalten; j++) {
                if (spielfeld[i][j].equals(symbol) && spielfeld[i + 1][j].equals(symbol) &&
                        spielfeld[i + 2][j].equals(symbol) && spielfeld[i + 3][j].equals(symbol)) {
                    return true;
                }
            }
        }
        // Diagonale Gewinnüberprüfung (von oben links nach unten rechts)
        for (int i = 0; i < zeilen - 3; i++) {
            for (int j = 0; j < spalten - 3; j++) {
                if (spielfeld[i][j].equals(symbol) && spielfeld[i + 1][j + 1].equals(symbol) &&
                        spielfeld[i + 2][j + 2].equals(symbol) && spielfeld[i + 3][j + 3].equals(symbol)) {
                    return true;
                }
            }
        }
        // Diagonale Gewinnüberprüfung (von unten links nach oben rechts)
        for (int i = 3; i < zeilen; i++) {
            for (int j = 0; j < spalten - 3; j++) {
                if (spielfeld[i][j].equals(symbol) && spielfeld[i - 1][j + 1].equals(symbol) &&
                        spielfeld[i - 2][j + 2].equals(symbol) && spielfeld[i - 3][j + 3].equals(symbol)) {
                    return true;
                }
            }
        }
        return false; // kein Sieg
    }
    public String[][] getSpielfeld() {
        return spielfeld;
    }

    private int aktuellerSpielerIndex = 0; // Beispiel: Index des aktuellen Spielers

    public Spieler getAktuellerSpieler() {
        return spieler.get(aktuellerSpielerIndex);
    }



    private void zeigeSpielfeld() {
        // Konsolenausgabe für Debugging
        for (int i = 0; i < zeilen; i++) {
            for (int j = 0; j < spalten; j++) {
                System.out.print(spielfeld[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();

    }


    private void zeigeStatusInGui(String nachricht) {
        // Beispiel: GUI-Komponente für Status verwenden
        JOptionPane.showMessageDialog(null, nachricht, "Spielstatus", JOptionPane.INFORMATION_MESSAGE);
    }

}




