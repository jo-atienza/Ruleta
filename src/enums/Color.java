package enums;

public enum Color {
    ROJO,
    NEGRO,
    AZUL,
    BLANCO,
    VERDE;

    /**
     * Convierte un String a su correspondiente enum Color, ignorando mayúsculas/minúsculas.
     * Esencial para procesar la entrada del usuario desde la GUI.
     * @param texto El nombre del color a convertir.
     * @return El enum Color, o null si no se encuentra.
     */
    public static Color fromString(String texto) {
        if (texto == null) return null;
        for (Color c : Color.values()) {
            if (c.name().equalsIgnoreCase(texto.trim())) {
                return c;
            }
        }
        return null;
    }
}