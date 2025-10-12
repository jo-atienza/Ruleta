import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Representa el tablero, la distribución de casillas y la lógica del giro/pago.
 * Implementación con asignación de colores por listas de números fijos.
 */
public class MesaRuleta {
    private final List<Casilla> casillas;
    private final Random random;

    public MesaRuleta() {
        this.casillas = new ArrayList<>();
        this.random = new Random();
        inicializarTablero();
    }

    /**
     * Implementa la distribución por listas de colores para las 50 casillas.
     */
    private void inicializarTablero() {
        this.casillas.clear();

        // --- 1. Definición de la secuencia y números a asignar ---

        // Colores base (R, N, A, B)
        String[] nombresColoresBase = {"Rojo", "Negro", "Azul", "Blanco"};

        // Números fijos Verde
        List<Integer> numerosVerdes = Arrays.asList(25, 50);

        // Lista de números que NO son Verdes (48 números)
        List<Integer> numerosBase = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            if (!numerosVerdes.contains(i)) {
                numerosBase.add(i);
            }
        }

        // Mapa para guardar la asignación final de color por número
        Map<Integer, String> asignacionFinal = new HashMap<>();

        // --- 2. Asignación de colores ---

        // a) Asignar los números Verdes fijos
        asignacionFinal.put(25, "Verde");
        asignacionFinal.put(50, "Verde");

        // b) Asignar los 48 números base secuencialmente
        int colorIndex = 0;
        for (int i = 0; i < numerosBase.size(); i++) {
            int num = numerosBase.get(i);
            // El colorIndex se incrementa en cada iteración para garantizar la secuencia R, N, A, B.
            String color = nombresColoresBase[colorIndex % nombresColoresBase.length];
            asignacionFinal.put(num, color);
            colorIndex++;
        }

        // 3. Construir la lista final de casillas en orden numérico (1 a 50)
        for (int i = 1; i <= 50; i++) {
            this.casillas.add(new Casilla(i, asignacionFinal.get(i)));
        }
    }

    // El resto de los métodos se mantienen igual (getCasillas, tirarPelotas, calcularGanancia)

    /**
     * Getter necesario para que RuletaPanel pueda dibujar.
     */
    public List<Casilla> getCasillas() {
        return casillas;
    }

    /**
     * Simula el lanzamiento de las dos pelotas.
     */
    public Casilla[] tirarPelotas() {
        Casilla[] resultados = new Casilla[2];
        resultados[0] = casillas.get(random.nextInt(casillas.size()));
        resultados[1] = casillas.get(random.nextInt(casillas.size()));
        return resultados;
    }

    /**
     * Calcula la ganancia total (monto apostado + ganancia neta).
     */
    public double calcularGanancia(Apuesta apuesta, Casilla[] resultados) throws TipoApuestaInvalidoException {
        double multiplicadorBase = 0.0;

        final Casilla r1 = resultados[0];
        final Casilla r2 = resultados[1];

        boolean hayVerde = r1.getColor().equals("Verde") || r2.getColor().equals("Verde");

        switch (apuesta.getTipo()) {
            case "Numero Especifico":
                if (apuesta.getValor() instanceof Integer) {
                    int numApostado = (int) apuesta.getValor();
                    if (r1.getNumero() == numApostado || r2.getNumero() == numApostado) {
                        multiplicadorBase = 35.0;
                    }
                } else {
                    throw new TipoApuestaInvalidoException("Apuesta de Número Específico requiere un valor Integer.");
                }
                break;
            case "Par / Impar":
                if (apuesta.getValor() instanceof String) {
                    String condicion = (String) apuesta.getValor();
                    boolean r1Match = (r1.getNumero() % 2 == 0 && condicion.equals("Par")) || (r1.getNumero() % 2 != 0 && condicion.equals("Impar"));
                    boolean r2Match = (r2.getNumero() % 2 == 0 && condicion.equals("Par")) || (r2.getNumero() % 2 != 0 && condicion.equals("Impar"));
                    if (r1Match || r2Match) {
                        multiplicadorBase = 1.0;
                    }
                }
                break;
            case "Color Clasico":
                if (apuesta.getValor() instanceof String) {
                    String colorApostado = (String) apuesta.getValor();
                    if (r1.getColor().equals(colorApostado) || r2.getColor().equals(colorApostado)) {
                        multiplicadorBase = 1.0;
                    }
                } else {
                    throw new TipoApuestaInvalidoException("Apuesta de Color requiere un valor String.");
                }
                break;
            case "Color Cualquiera":
                if (apuesta.getValor() instanceof String) {
                    String colorApostado = (String) apuesta.getValor();
                    if (r1.getColor().equals(colorApostado) || r2.getColor().equals(colorApostado)) {
                        multiplicadorBase = 2.0;
                    }
                }
                break;
            case "Color Unico":
                if (apuesta.getValor() instanceof String) {
                    String colorApostado = (String) apuesta.getValor();
                    if (r1.getColor().equals(colorApostado) && r2.getColor().equals(colorApostado)) {
                        multiplicadorBase = 5.0;
                    }
                }
                break;
            case "Color Mixto":
                if (apuesta.getValor() instanceof List) {
                    List<String> coloresApostados = (List<String>) apuesta.getValor();
                    List<String> coloresResultado = Arrays.asList(r1.getColor(), r2.getColor());

                    if (coloresApostados.size() == 2 && coloresResultado.size() == 2) {
                        Collections.sort(coloresApostados);
                        Collections.sort(coloresResultado);

                        if (coloresApostados.equals(coloresResultado)) {
                            multiplicadorBase = 3.0;
                        }
                    }
                }
                break;
            case "Doble Coincidencia":
                if (apuesta.getValor() instanceof List) {
                    List<String> coloresApostados = (List<String>) apuesta.getValor();
                    if (coloresApostados.size() == 2) {
                        String color1 = coloresApostados.get(0);
                        String color2 = coloresApostados.get(1);

                        if (r1.getColor().equals(color1) && r2.getColor().equals(color2)) {
                            multiplicadorBase = 8.0;
                        }
                    }
                }
                break;
            case "Calle o Linea (3 nums)":
            case "Docena o Rango (12 nums)":
                break;
            default:
                break;
        }

        if (multiplicadorBase > 0.0 && hayVerde) {
            multiplicadorBase *= 2;
        }

        return apuesta.getMonto() * (1.0 + multiplicadorBase);
    }
}