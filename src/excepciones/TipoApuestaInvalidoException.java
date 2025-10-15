package excepciones;

public class TipoApuestaInvalidoException extends Exception {
    public TipoApuestaInvalidoException(String mensaje) {
        super(mensaje);
    }
}