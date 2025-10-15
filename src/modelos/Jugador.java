package modelos;

import excepciones.SaldoInsuficienteException;

public class Jugador {
    private double saldo;

    public Jugador(double saldoInicial) {
        this.saldo = saldoInicial;
    }

    public void apostar(double monto) throws SaldoInsuficienteException {
        if (monto > saldo) {
            // --- INICIO DE LA MODIFICACIÓN ---

            // 1. Calculamos la diferencia que le falta al jugador.
            double diferencia = monto - saldo;

            // 2. Usamos la nueva variable 'diferencia' en el mensaje.
            // Se usa String.format para mostrar el número con dos decimales.
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente. Saldo actual: $" + String.format("%.2f", saldo) + ". Te faltan $" + String.format("%.2f", diferencia) + "."
            );

            // --- FIN DE LA MODIFICACIÓN ---
        }
        this.saldo -= monto;
    }

    public void cobrar(double ganancia) {
        this.saldo += ganancia;
    }

    public double getSaldo() {
        return saldo;
    }
}