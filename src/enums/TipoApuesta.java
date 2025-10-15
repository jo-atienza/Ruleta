package enums;

public enum TipoApuesta {
    COLOR_CUALQUIERA("Color Cualquiera"),
    COLOR_UNICO("Color Unico"),
    COLOR_MIXTO("Color Mixto"),
    DOBLE_COINCIDENCIA("Doble Coincidencia"),
    NUMERO_ESPECIFICO("Numero Especifico"),
    PAR_IMPAR("Par / Impar"),
    COLOR_CLASICO("Color Clasico");

    private final String displayName;

    TipoApuesta(String displayName) {
        this.displayName = displayName;
    }

    // Esto es lo que se mostrará en el JComboBox de la GUI.
    @Override
    public String toString() {
        return displayName;
    }
}