import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.util.Collections;

/**
 * Representa el tablero, la distribución de casillas y la lógica del giro/pago.
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
     * Implementa la distribución exacta de las 50 casillas: 2V, 12R, 12N, 12A, 12B.
     * Asignación: Verde en 25 y 50. Los demás se rotan en orden.
     */
    private void inicializarTablero() {
        String[] coloresBase = {"Rojo", "Negro", "Azul", "Blanco"};
        int colorIndex = 0;

        for (int i = 1; i <= 50; i++) {
            String color;

            if (i == 25 || i == 50) {
                color = "Verde";
            } else {
                color = coloresBase[colorIndex % coloresBase.length];
                colorIndex++;
            }
            this.casillas.add(new Casilla(i, color));
        }
    }

    /**
     * Simula el lanzamiento de las dos pelotas.
     * @return Array de las 2 casillas donde cayeron las pelotas.
     */
    public Casilla[] tirarPelotas() {
        Casilla[] resultados = new Casilla[2];
        // Selecciona dos índices aleatorios entre 0 y 49 (50 casillas).
        resultados[0] = casillas.get(random.nextInt(casillas.size()));
        resultados[1] = casillas.get(random.nextInt(casillas.size()));
        return resultados;
    }

    /**
     * Calcula la ganancia basada en la apuesta y el resultado del giro.
     * @param apuesta La apuesta realizada.
     * @param resultados El array de 2 casillas donde cayeron las pelotas.
     * @return Monto total de la ganancia (0 si pierde)
     * Nota: El valor devuelto incluye la apuesta inicial (monto * (1 + multiplicadorBase)).
     */
    public double calcularGanancia(Apuesta apuesta, Casilla[] resultados) throws TipoApuestaInvalidoException {
        double multiplicadorBase = 0.0;

        final Casilla r1 = resultados[0];
        final Casilla r2 = resultados[1];

        // Determinar si aplica la Sorpresa Verde
        boolean hayVerde = r1.getColor().equals("Verde") || r2.getColor().equals("Verde");

        // 1. Determinar el multiplicador base según el tipo de apuesta
        switch (apuesta.getTipo()) {
            case "Numero Especifico": // Pago 35x
                if (apuesta.getValor() instanceof Integer) {
                    int numApostado = (int) apuesta.getValor();
                    if (r1.getNumero() == numApostado || r2.getNumero() == numApostado) {
                        multiplicadorBase = 35.0;
                    }
                } else {
                    throw new TipoApuestaInvalidoException("Apuesta de Número Específico requiere un valor Integer.");
                }
                break;

            case "Par / Impar": // Pago 1x
                if (apuesta.getValor() instanceof String) {
                    String condicion = (String) apuesta.getValor();
                    boolean r1Match = (r1.getNumero() % 2 == 0 && condicion.equals("Par")) || (r1.getNumero() % 2 != 0 && condicion.equals("Impar"));
                    boolean r2Match = (r2.getNumero() % 2 == 0 && condicion.equals("Par")) || (r2.getNumero() % 2 != 0 && condicion.equals("Impar"));
                    if (r1Match || r2Match) {
                        multiplicadorBase = 1.0;
                    }
                }
                break;

            case "Color Clasico": // Pago 1x
                if (apuesta.getValor() instanceof String) {
                    String colorApostado = (String) apuesta.getValor();
                    if (r1.getColor().equals(colorApostado) || r2.getColor().equals(colorApostado)) {
                        multiplicadorBase = 1.0;
                    }
                } else {
                    throw new TipoApuestaInvalidoException("Apuesta de Color requiere un valor String.");
                }
                break;

            case "Color Cualquiera": // Pago 2x
                if (apuesta.getValor() instanceof String) {
                    String colorApostado = (String) apuesta.getValor();
                    if (r1.getColor().equals(colorApostado) || r2.getColor().equals(colorApostado)) {
                        multiplicadorBase = 2.0;
                    }
                }
                break;

            case "Color Unico": // Pago 5x
                if (apuesta.getValor() instanceof String) {
                    String colorApostado = (String) apuesta.getValor();
                    if (r1.getColor().equals(colorApostado) && r2.getColor().equals(colorApostado)) {
                        multiplicadorBase = 5.0;
                    }
                }
                break;

            case "Color Mixto": // Pago 3x
                // Asumimos que el valor es una lista [Color1, Color2]
                if (apuesta.getValor() instanceof List) {
                    List<String> coloresApostados = (List<String>) apuesta.getValor();
                    List<String> coloresResultado = Arrays.asList(r1.getColor(), r2.getColor());

                    if (coloresApostados.size() == 2 && coloresResultado.size() == 2) {
                        // Ordenamos para comparar, ignorando el orden
                        Collections.sort(coloresApostados);
                        Collections.sort(coloresResultado);

                        if (coloresApostados.equals(coloresResultado)) {
                            multiplicadorBase = 3.0;
                        }
                    }
                }
                break;

            case "Doble Coincidencia": // Pago 8x
                // Asumimos que el valor es una lista [Color1, Color2] en ORDEN
                if (apuesta.getValor() instanceof List) {
                    List<String> coloresApostados = (List<String>) apuesta.getValor();
                    if (coloresApostados.size() == 2) {
                        String color1 = coloresApostados.get(0);
                        String color2 = coloresApostados.get(1);

                        // Coincidencia de orden: r1 debe ser Color1, r2 debe ser Color2
                        if (r1.getColor().equals(color1) && r2.getColor().equals(color2)) {
                            multiplicadorBase = 8.0;
                        }
                    }
                }
                break;

            // Faltan casos para Calle/Línea y Docena/Rango, que requieren lógica adicional de números
            case "Calle o Linea (3 nums)":
            case "Docena o Rango (12 nums)":
                // Se dejan en 0.0 por no tener la implementación de rangos numéricos
                break;

            default:
                break;
        }

        // 2. Aplicar la Sorpresa Verde
        if (multiplicadorBase > 0.0 && hayVerde) {
            multiplicadorBase *= 2;
        }

        // Devolvemos el monto total (apuesta inicial + ganancia neta)
        return apuesta.getMonto() * (1.0 + multiplicadorBase);
    }
}