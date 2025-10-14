package modelos;

import enums.Color;
import excepciones.TipoApuestaInvalidoException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import enums.Color;

/*
 * Representa el tablero, la distribución de casillas y la lógica del giro/pago.
 * Implementación con asignación de colores por listas de números fijos.
 */

public class MesaRuleta {
    // Atributos
    private final List<Casilla> casillas;
    private final Random random;

    // Constructor:
    public MesaRuleta() {
        this.casillas = new ArrayList<>();
        this.random = new Random();
        inicializarTablero();
    }

    // Getters & Setters:
    public List<Casilla> getCasillas() {
        return casillas;
    }

    // Métodos de la clase:

    // Implementa la distribución para las 50 casillas.
    private void inicializarTablero() {
        this.casillas.clear();

        Color[] nombresColoresBase = {Color.ROJO, Color.NEGRO, Color.AZUL, Color.BLANCO};

        // Verdes:
        List<Integer> numerosVerdes = Arrays.asList(25, 50);

        // Otros:
        List<Integer> numerosBase = new ArrayList<>();

        for (int casilla = 1; casilla <= 50; casilla++) {
            if (!numerosVerdes.contains(casilla)) {
                numerosBase.add(casilla);
            }
        }

        // Mapa con la asignación final de color por número
        Map<Integer, Color> asignacionFinal = new HashMap<>();

        // Asignación de colores:

        // Asignar los números Verdes fijos
        asignacionFinal.put(25, Color.VERDE);
        asignacionFinal.put(50,  Color.VERDE);

        // Asignar los 48 números base secuencialmente
        int colorIndex = 0;
        for (int i = 0; i < numerosBase.size(); i++) { // TODO poner otro nombre?
            int num = numerosBase.get(i);
            // El colorIndex se incrementa en cada iteración para garantizar la secuencia R, N, A, B.
            Color color = nombresColoresBase[colorIndex % nombresColoresBase.length];
            asignacionFinal.put(num, color);
            colorIndex++;
        }

        // Construir la lista final de casillas en orden numérico (1 a 50)
        for (int i = 1; i <= 50; i++) { //TODO poner otro nombre
            this.casillas.add(new Casilla(i, asignacionFinal.get(i)));
        }
    }

    // Simula el lanzamiento de las dos pelotas.
    public Casilla[] tirarPelotas() {
        Casilla[] resultados = new Casilla[2];
        resultados[0] = casillas.get(random.nextInt(casillas.size()));
        resultados[1] = casillas.get(random.nextInt(casillas.size()));
        return resultados;
    }

    // Calcula la ganancia total (monto apostado + ganancia neta).
    public double calcularGanancia(Apuesta apuesta, Casilla[] resultados) throws TipoApuestaInvalidoException {
        double multiplicadorBase = 0.0;

        final Casilla r1 = resultados[0];
        final Casilla r2 = resultados[1];

        boolean hayVerde = r1.getColor().equals(Color.VERDE) || r2.getColor().equals(Color.VERDE);

        switch (apuesta.getTipo()) {
            case SORPRESA_VERDE:
                if (hayVerde) {
                    multiplicadorBase = 1.0; // Modificar y completar.
                }
                break;

            case NUMERO:
                if (apuesta.getValor() instanceof Integer) {
                    int numApostado = (int) apuesta.getValor();
                    if (r1.getNumero() == numApostado || r2.getNumero() == numApostado) {
                        multiplicadorBase = 35.0;
                    }
                } else {
                    throw new TipoApuestaInvalidoException("Apuesta de Número Específico requiere un valor Integer.");
                }
                break;

            case PARIDAD:
                if (apuesta.getValor() instanceof String) {
                    String condicion = (String) apuesta.getValor();
                    boolean r1Match = (r1.getNumero() % 2 == 0 && condicion.equals("Par")) || (r1.getNumero() % 2 != 0 && condicion.equals("Impar"));
                    boolean r2Match = (r2.getNumero() % 2 == 0 && condicion.equals("Par")) || (r2.getNumero() % 2 != 0 && condicion.equals("Impar"));
                    if (r1Match || r2Match) {
                        multiplicadorBase = 1.0;
                    }
                }
                break;

            case COLOR_CLASICO:
                if (apuesta.getValor() instanceof Color) {
                    Color colorApostado = (Color) apuesta.getValor();
                    if (r1.getColor().equals(colorApostado) || r2.getColor().equals(colorApostado)) {
                        multiplicadorBase = 1.0;
                    }
                } else {
                    throw new TipoApuestaInvalidoException("Apuesta de Color requiere un valor String.");
                }
                break;

            case COLOR_CUALQUIERA:
                if (apuesta.getValor() instanceof String) {
                    Color colorApostado = (Color) apuesta.getValor();
                    if (r1.getColor().equals(colorApostado) || r2.getColor().equals(colorApostado)) {
                        multiplicadorBase = 2.0;
                    }
                }
                break;

            case COLOR_UNICO:
                if (apuesta.getValor() instanceof String) {
                    String colorApostado = (String) apuesta.getValor();
                    if (r1.getColor().equals(colorApostado) && r2.getColor().equals(colorApostado)) {
                        multiplicadorBase = 5.0;
                    }
                }
                break;

            case COLOR_MIXTO:
                if (apuesta.getValor() instanceof List) {
                    List<Color> coloresApostados = (List<Color>) apuesta.getValor();
                    List<Color> coloresResultado = Arrays.asList(r1.getColor(), r2.getColor());

                    if (coloresApostados.size() == 2 && coloresResultado.size() == 2) {
                        Collections.sort(coloresApostados);
                        Collections.sort(coloresResultado);

                        if (coloresApostados.equals(coloresResultado)) {
                            multiplicadorBase = 3.0;
                        }
                    }
                }
                break;

            case DOBLE_COINCIDENCIA:
                if (apuesta.getValor() instanceof List) {
                    List<Color> coloresApostados = (List<Color>) apuesta.getValor();
                    if (coloresApostados.size() == 2) {
                        Color color1 = coloresApostados.get(0);
                        Color color2 = coloresApostados.get(1);

                        if (r1.getColor().equals(color1) && r2.getColor().equals(color2)) {
                            multiplicadorBase = 8.0;
                        }
                    }
                }
                break;

            case CALLE:
                // TODO Completar
                break;

            case RANGO:
                // TODO Completar
                break;

            default:
                // TODO Completar
                break;
        }

        if (multiplicadorBase > 0.0 && hayVerde) {
            multiplicadorBase *= 2;
        }

        return apuesta.getMonto() * (1.0 + multiplicadorBase);
    }
}
