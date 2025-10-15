package modelos;

import excepciones.SaldoInsuficienteException;

public class Jugador {
    private double saldo;

    public Jugador(double saldoInicial) {
        this.saldo = saldoInicial;
    }

    public void apostar(double monto) throws SaldoInsuficienteException {
        if (monto > saldo) {
            throw new SaldoInsuficienteException(
                    "Saldo insuficiente. Saldo actual: $" + saldo + ". Necesitas $" + monto + "."
            );
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