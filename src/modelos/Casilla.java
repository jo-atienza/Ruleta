package modelos;

import enums.Color; // Se importa el enum

/**
 * Representa cada uno de los 50 espacios de la ruleta (número y color).
 */
public class Casilla {
    private final int numero;
    private final Color color; // CAMBIO: De String a enum Color

    public Casilla(int numero, Color color) { // CAMBIO: El parámetro ahora es de tipo Color
        this.numero = numero;
        this.color = color;
    }

    public int getNumero() {
        return numero;
    }

    public Color getColor() { // CAMBIO: El retorno ahora es de tipo Color
        return color;
    }

    @Override
    public String toString() {
        // Usamos .name() para obtener el nombre del enum como String
        return "[" + numero + ", " + color.name() + "]";
    }
}