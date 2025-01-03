package game;
public class Spieler {
    private String name;
    private String artDesSpieler;
    public Spieler(String name, String artDesSpieler) {
        setName(name);
        setArtDesSpieler(artDesSpieler);
    }
    public String getName() {
        return name;
    }
    public String getArtDesSpieler() {
        return artDesSpieler;
    }
    public void setName(String name) {
        // Validierung: Name darf nicht null, leer oder nur Leerzeichen sein
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Der Name des Spielers darf nicht leer sein.");
        }
        this.name = name;
    }
    public void setArtDesSpieler(String artDesSpieler) {
        // Validierung: Art des Spielers muss "Mensch" oder "Computer" sein
        if (!artDesSpieler.equals("Mensch") && !artDesSpieler.equals("Computer")) {
            throw new IllegalArgumentException("Die Art des Spielers muss entweder 'Mensch' oder 'Computer' sein.");
        }
        this.artDesSpieler = artDesSpieler;

    }
}
