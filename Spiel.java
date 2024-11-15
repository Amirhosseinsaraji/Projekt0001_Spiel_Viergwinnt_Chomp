package PraktikumJava;
import java.util.List;

public abstract class Spiel {
    protected List<Spieler> spieler;
    protected Spielfeld spielfeld;

    public Spiel(List<Spieler> spieler, Spielfeld spielfeld) {
        this.spieler = spieler;
        this.spielfeld = spielfeld;
    }
        public abstract void spielzug (Spieler spieler);
        public abstract void durchgang();
    }
