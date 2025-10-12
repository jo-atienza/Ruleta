/**
 * Representa cada uno de los 50 espacios de la ruleta (número y color).
 */
public class Casilla {
    private final int numero;
    private final String color; // "Rojo", "Negro", "Azul", "Blanco", "Verde"

    public Casilla(int numero, String color) {
        this.numero = numero;
        this.color = color;
    }

    public int getNumero() {
        return numero;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "[" + numero + ", " + color + "]";
    }
}
