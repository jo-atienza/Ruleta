package modelos;

import enums.TipoApuesta;
import excepciones.MontoApuestaInvalidoException;

public class Apuesta {
    private static final double MIN_APUESTA = 10.0;
    private static final double MAX_APUESTA = 5000.0;

    private final double monto;
    private final TipoApuesta tipo;
    private final Object valor;

    public Apuesta(double monto, TipoApuesta tipo, Object valor) throws MontoApuestaInvalidoException {
        if (monto < MIN_APUESTA || monto > MAX_APUESTA) {
            throw new MontoApuestaInvalidoException(
                    "El monto de la apuesta ($" + monto + ") debe estar entre $" + MIN_APUESTA + " y $" + MAX_APUESTA + "."
            );
        }
        this.monto = monto;
        this.tipo = tipo;
        this.valor = valor;
    }

    public double getMonto() { return monto; }
    public TipoApuesta getTipo() { return tipo; }
    public Object getValor() { return valor; }
}