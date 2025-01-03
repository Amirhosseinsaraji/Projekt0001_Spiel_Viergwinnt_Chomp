package game;
public abstract class Spielfeld {
    private int zeilen;
    private int spalten;

    public Spielfeld(int zeilen, int spalten) {
        if (zeilen <= 0 || spalten <= 0) {
            throw new IllegalArgumentException("Zeilen und Spalten müssen größer als 0 sein.");
        }
        this.zeilen = zeilen;
        this.spalten = spalten;
    }

    public abstract void zeigeSpielfeld();

    public int getZeilen() {
        return zeilen;
    }


    public int getSpalten() {
        return spalten;
    }
}
