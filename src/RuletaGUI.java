import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class RuletaGUI extends JFrame {
    // --- Modelos y Lógica ---
    private final Jugador jugador;
    private final MesaRuleta mesaRuleta;

    // --- Componentes Swing ---
    private final JLabel lblSaldo;
    private final JTextField txtMonto;
    private final JTextField txtApuestaValor;
    private final JComboBox<String> cmbApuestaTipo;
    private final JTextArea txtResultados;
    private final JButton btnGirar;
    private final RuletaPanel ruletaPanel;
    private final Timer timer; // Timer para la animación de giro

    public RuletaGUI() {
        // Inicialización del Modelo
        this.jugador = new Jugador(1000.0);
        this.mesaRuleta = new MesaRuleta();

        // Configuración de la Ventana
        setTitle("Ruleta Arcoíris 2-Pelotas");
        setSize(950, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- Inicialización de Componentes ---
        lblSaldo = new JLabel("Saldo: $" + String.format("%.2f", jugador.getSaldo()));
        txtMonto = new JTextField("100", 5);
        txtApuestaValor = new JTextField("Rojo", 15);
        cmbApuestaTipo = new JComboBox<>(new String[]{
                "Color Cualquiera",
                "Color Unico",
                "Color Mixto",
                "Doble Coincidencia",
                "Numero Especifico",
                "Par / Impar",
                "Color Clasico",
                "Calle o Linea (3 nums)",
                "Docena o Rango (12 nums)"
        });
        txtResultados = new JTextArea(15, 60);
        txtResultados.setEditable(false);
        btnGirar = new JButton("¡APOSTAR Y GIRAR!");

        ruletaPanel = new RuletaPanel(mesaRuleta);

        // --- Panel de Control (Norte) ---
        JPanel pnlControl = new JPanel();
        pnlControl.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        pnlControl.add(lblSaldo);
        pnlControl.add(new JLabel("Tipo:"));
        pnlControl.add(cmbApuestaTipo);
        pnlControl.add(new JLabel("Monto: $"));
        pnlControl.add(txtMonto);
        pnlControl.add(new JLabel("Valor (ej: Rojo, 42, Rojo,Negro):"));
        pnlControl.add(txtApuestaValor);
        pnlControl.add(btnGirar);

        // --- Panel de Ruleta (Centro) ---
        JPanel pnlTablero = new JPanel(new BorderLayout());
        pnlTablero.add(ruletaPanel, BorderLayout.CENTER);

        // --- Panel de Resultados (Sur) ---
        JPanel pnlResultados = new JPanel(new BorderLayout());
        pnlResultados.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pnlResultados.add(new JLabel("Historial y Resultados:"), BorderLayout.NORTH);
        pnlResultados.add(new JScrollPane(txtResultados), BorderLayout.CENTER);

        // --- Añadir Paneles a la Ventana Principal ---
        add(pnlControl, BorderLayout.NORTH);
        add(pnlTablero, BorderLayout.CENTER);
        add(pnlResultados, BorderLayout.SOUTH);

        // Inicialización del Timer de animación (50ms = 20 FPS)
        this.timer = new Timer(50, e -> ruletaPanel.avanzarGiro());

        // Registrar Event Listener
        btnGirar.addActionListener(e -> intentarGiro());

        // Finalizar y mostrar
        setLocationRelativeTo(null);
    }

    /**
     * Inicia el proceso de apuesta, manejo de excepciones y la animación de giro.
     */
    private void intentarGiro() {
        btnGirar.setEnabled(false);

        try {
            // 1. Obtener y validar la entrada
            double monto = Double.parseDouble(txtMonto.getText().trim());
            String tipo = (String) cmbApuestaTipo.getSelectedItem();
            Object valor = obtenerValorApuesta(tipo, txtApuestaValor.getText().trim());

            // 2. Crear y validar la Apuesta
            Apuesta apuesta = new Apuesta(monto, tipo, valor);

            // 3. Descontar del Saldo (Se descuenta antes del giro.)
            jugador.apostar(monto);
            actualizarSaldo();

            // 4. Iniciar la Animación
            ruletaPanel.setResultados(new Casilla[2]);
            ruletaPanel.setIsGirando(true);
            timer.start();

            // 5. Usar un SEGUNDO Timer para detener la animación y calcular resultados
            Timer detencionTimer = new Timer(3000, e -> {
                ((Timer)e.getSource()).stop();
                finalizarGiro(apuesta);
            });
            detencionTimer.setRepeats(false);
            detencionTimer.start();

        } catch (NumberFormatException ex) {
            mostrarError("Error de Entrada", "El monto de apuesta debe ser un número válido.");
            btnGirar.setEnabled(true);
        } catch (MontoApuestaInvalidoException | SaldoInsuficienteException | TipoApuestaInvalidoException ex) {
            mostrarError("Error de Apuesta/Saldo", ex.getMessage());
            btnGirar.setEnabled(true);
        } catch (Exception ex) {
            mostrarError("Error Desconocido", "Ocurrió un error: " + ex.toString());
            btnGirar.setEnabled(true);
        }
    }

    /**
     * Lógica que se ejecuta después de que termina la animación de giro.
     */
    private void finalizarGiro(Apuesta apuesta) {
        timer.stop();
        ruletaPanel.setIsGirando(false);

        try {
            // 1. Cálculo de Resultados
            Casilla[] resultados = mesaRuleta.tirarPelotas();
            double ganancia = mesaRuleta.calcularGanancia(apuesta, resultados);

            // 2. Integración visual y pago
            ruletaPanel.setResultados(resultados);

            String msgResultado = generarMensajeResultado(apuesta, resultados, ganancia);
            txtResultados.append(msgResultado + "\n");

            if (ganancia > apuesta.getMonto()) {
                jugador.cobrar(ganancia);
            }

            actualizarSaldo();
            btnGirar.setEnabled(true);

        } catch (Exception ex) {
            mostrarError("Error de Cálculo", "Error al procesar el resultado: " + ex.getMessage());
            btnGirar.setEnabled(true);
        }
    }

    // --- Métodos Auxiliares ---

    private Object obtenerValorApuesta(String tipo, String textoValor) throws TipoApuestaInvalidoException {
        if (textoValor.isEmpty() && !tipo.equals("Par / Impar")) {
            throw new TipoApuestaInvalidoException("El campo 'Valor' no puede estar vacío para esta apuesta.");
        }

        if (tipo.equals("Numero Especifico")) {
            try {
                return Integer.parseInt(textoValor);
            } catch (NumberFormatException e) {
                throw new TipoApuestaInvalidoException("Para 'Número Específico', el valor debe ser un número entero (1-50).");
            }
        }

        if (tipo.equals("Doble Coincidencia") || tipo.equals("Color Mixto")) {
            String[] partes = textoValor.split(",");
            if (partes.length != 2) {
                throw new TipoApuestaInvalidoException("Para '" + tipo + "', ingresa dos colores separados por coma (ej: Rojo,Negro).");
            }
            return Arrays.asList(partes[0].trim(), partes[1].trim());
        }

        return textoValor;
    }

    private String generarMensajeResultado(Apuesta apuesta, Casilla[] resultados, double gananciaTotal) {
        String msg = "Giro: " + resultados[0] + " y " + resultados[1] + ". ";
        double gananciaNeta = gananciaTotal - apuesta.getMonto();

        if (gananciaNeta > 0) {
            msg += "¡GANASTE! Ganancia Neta: $" + String.format("%.2f", gananciaNeta) + ".";
        } else {
            msg += "Perdiste.";
        }

        if (resultados[0].getColor().equals("Verde") || resultados[1].getColor().equals("Verde")) {
            msg += " **¡SORPRESA VERDE ACTIVADA!**";
        }

        return msg;
    }

    private void actualizarSaldo() {
        lblSaldo.setText("Saldo: $" + String.format("%.2f", jugador.getSaldo()));
    }

    private void mostrarError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }
}