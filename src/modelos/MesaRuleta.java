package modelos;

import enums.Color;
import excepciones.TipoApuestaInvalidoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MesaRuleta {
    private final List<Casilla> casillas;
    private final Random random;

    public MesaRuleta() {
        this.casillas = new ArrayList<>();
        this.random = new Random();
        inicializarTablero();
    }

    public List<Casilla> getCasillas() {
        return casillas;
    }

    private void inicializarTablero() {
        this.casillas.clear();
        Color[] nombresColoresBase = {Color.ROJO, Color.NEGRO, Color.AZUL, Color.BLANCO};
        List<Integer> numerosVerdes = Arrays.asList(25, 50);
        List<Integer> numerosBase = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            if (!numerosVerdes.contains(i)) numerosBase.add(i);
        }

        Map<Integer, Color> asignacionFinal = new HashMap<>();
        asignacionFinal.put(25, Color.VERDE);
        asignacionFinal.put(50, Color.VERDE);

        int colorIndex = 0;
        for (int num : numerosBase) {
            asignacionFinal.put(num, nombresColoresBase[colorIndex++ % nombresColoresBase.length]);
        }

        for (int i = 1; i <= 50; i++) {
            this.casillas.add(new Casilla(i, asignacionFinal.get(i)));
        }
    }

    public Casilla[] tirarPelotas() {
        Casilla[] resultados = new Casilla[2];
        int idx1 = random.nextInt(casillas.size());
        int idx2;
        do {
            idx2 = random.nextInt(casillas.size());
        } while (idx1 == idx2);
        resultados[0] = casillas.get(idx1);
        resultados[1] = casillas.get(idx2);
        return resultados;
    }

    public double calcularGanancia(Apuesta apuesta, Casilla[] resultados) throws TipoApuestaInvalidoException {
        double multiplicadorPremio = 0.0;
        final Casilla r1 = resultados[0];
        final Casilla r2 = resultados[1];
        boolean hayVerde = r1.getColor() == Color.VERDE || r2.getColor() == Color.VERDE;
        boolean apuestaEraVerde = false;

        switch (apuesta.getTipo()) {
            case NUMERO_ESPECIFICO:
                if (apuesta.getValor() instanceof Integer) {
                    int num = (int) apuesta.getValor();
                    if (r1.getNumero() == num || r2.getNumero() == num) multiplicadorPremio = 35.0;
                }
                break;
            case PAR_IMPAR:
                if (apuesta.getValor() instanceof String) {
                    boolean esPar = "Par".equalsIgnoreCase((String) apuesta.getValor());
                    if (((r1.getNumero() % 2 == 0) == esPar) || ((r2.getNumero() % 2 == 0) == esPar)) {
                        multiplicadorPremio = 1.0;
                    }
                }
                break;
            case COLOR_CLASICO:
                if (apuesta.getValor() instanceof Color) {
                    Color c = (Color) apuesta.getValor();
                    if (c != Color.ROJO && c != Color.NEGRO) throw new TipoApuestaInvalidoException("Color Clásico solo admite ROJO o NEGRO.");
                    if (r1.getColor() == c || r2.getColor() == c) multiplicadorPremio = 1.0;
                }
                break;
            case COLOR_CUALQUIERA:
                if (apuesta.getValor() instanceof Color) {
                    Color c = (Color) apuesta.getValor();
                    if (r1.getColor() == c || r2.getColor() == c) multiplicadorPremio = 2.0;
                    if (c == Color.VERDE) apuestaEraVerde = true;
                }
                break;
            case COLOR_UNICO:
                if (apuesta.getValor() instanceof Color) {
                    Color c = (Color) apuesta.getValor();
                    if (r1.getColor() == c && r2.getColor() == c) multiplicadorPremio = 5.0;
                    if (c == Color.VERDE) apuestaEraVerde = true;
                }
                break;
            case COLOR_MIXTO:
                if (apuesta.getValor() instanceof List) {
                    List<Color> colores = (List<Color>) apuesta.getValor();
                    if (colores.size() == 2) {
                        Color c1 = colores.get(0);
                        Color c2 = colores.get(1);
                        if ((r1.getColor() == c1 && r2.getColor() == c2) || (r1.getColor() == c2 && r2.getColor() == c1)) {
                            multiplicadorPremio = 3.0;
                        }
                    }
                }
                break;
            case DOBLE_COINCIDENCIA:
                if (apuesta.getValor() instanceof List) {
                    List<Object> v = (List<Object>) apuesta.getValor();
                    if (v.size() == 2 && v.get(0) instanceof Integer && v.get(1) instanceof Color) {
                        int num = (Integer) v.get(0);
                        Color c = (Color) v.get(1);
                        if ((r1.getNumero() == num && r1.getColor() == c) || (r2.getNumero() == num && r2.getColor() == c)) {
                            multiplicadorPremio = 8.0;
                        }
                    }
                }
                break;
        }

        if (multiplicadorPremio == 0.0) return 0.0;
        if (hayVerde && !apuestaEraVerde) multiplicadorPremio *= 2;
        return apuesta.getMonto() + (apuesta.getMonto() * multiplicadorPremio);
    }
}