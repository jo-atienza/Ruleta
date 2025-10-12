/**
 * Representa al jugador y gestiona su saldo (lanza SaldoInsuficienteException).
 */
public class Jugador {
    private double saldo;

    public Jugador(double saldoInicial) {
        this.saldo = saldoInicial;
    }

    /**
     * Descuenta el monto de la apuesta.
     */
    public void apostar(double monto) throws SaldoInsuficienteException {
        if (monto > saldo) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente. Saldo actual: $" + saldo + ". Necesitas $" + monto + "."
            );
        }
        this.saldo -= monto;
    }

    /**
     * Añade la ganancia al saldo.
     */
    public void cobrar(double ganancia) {
        this.saldo += ganancia;
    }

    public double getSaldo() {
        return saldo;
    }
}