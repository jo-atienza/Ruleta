import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

// Implementamos la interfaz para que el panel nos pueda notificar
public class RuletaGUI extends JFrame implements GiroListener {

    // --- ¡ESTAS SON LAS LÍNEAS QUE FALTABAN! ---
    // Componentes de la UI
    private final JButton btnGirar;
    private final JTextField txtMonto;
    private final JTextField txtApuestaValor;
    private final JComboBox<String> cmbApuestaTipo;
    private final JTextArea txtResultados;
    private final JLabel lblSaldo;

    // Lógica del Juego
    private final RuletaPanel ruletaPanel;
    private final MesaRuleta mesaRuleta;
    private final Jugador jugador;
    // ------------------------------------------------

    public RuletaGUI() {
        // --- Inicialización del Modelo ---
        this.jugador = new Jugador(1000.0);
        this.mesaRuleta = new MesaRuleta();

        // --- Configuración de la Ventana ---
        setTitle("Ruleta Arcoíris 2-Pelotas");
        setSize(1100, 950);
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
                "Color Clasico"
        });
        txtResultados = new JTextArea(15, 60);
        txtResultados.setEditable(false);
        btnGirar = new JButton("¡APOSTAR Y GIRAR!");

        ruletaPanel = new RuletaPanel(mesaRuleta);
        // ¡Importante! Registramos la GUI para que el panel le pueda notificar
        ruletaPanel.setGiroListener(this);

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

        // Registrar Event Listener
        btnGirar.addActionListener(e -> procesarApuestaYGirar());

        // Finalizar y mostrar
        setLocationRelativeTo(null);
    }

    private void procesarApuestaYGirar() {
        btnGirar.setEnabled(false);
        try {
            // FASE 1: Validar y cobrar la apuesta
            double monto = Double.parseDouble(txtMonto.getText().trim());
            String tipo = (String) cmbApuestaTipo.getSelectedItem();
            Object valor = obtenerValorApuesta(tipo, txtApuestaValor.getText().trim());

            Apuesta apuesta = new Apuesta(monto, tipo, valor);
            jugador.apostar(monto);
            actualizarSaldo();

            // FASE 2: Simplemente iniciar la animación
            ruletaPanel.iniciarGiro();

        } catch (NumberFormatException ex) {
            mostrarError("Error de Entrada", "El monto de apuesta debe ser un número válido.");
            btnGirar.setEnabled(true);
        } catch (MontoApuestaInvalidoException | SaldoInsuficienteException | TipoApuestaInvalidoException ex) {
            mostrarError("Error de Apuesta/Saldo", ex.getMessage());
            btnGirar.setEnabled(true);
        }
    }

    @Override
    public void giroTerminado(Casilla[] resultados) {
        // La animación terminó, ahora procesamos el resultado
        try {
            double monto = Double.parseDouble(txtMonto.getText().trim());
            String tipo = (String) cmbApuestaTipo.getSelectedItem();
            Object valor = obtenerValorApuesta(tipo, txtApuestaValor.getText().trim());
            Apuesta apuesta = new Apuesta(monto, tipo, valor);

            double ganancia = mesaRuleta.calcularGanancia(apuesta, resultados);
            String msgResultado = generarMensajeResultado(apuesta, resultados, ganancia);
            txtResultados.append(msgResultado + "\n");

            if (ganancia > 0) {
                jugador.cobrar(ganancia);
            }

            actualizarSaldo();

        } catch (Exception ex) {
            mostrarError("Error de Cálculo", "Error al procesar el resultado: " + ex.getMessage());
        } finally {
            // Reactivar el botón para la siguiente ronda
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

        if (gananciaNeta >= 0 && gananciaTotal > 0) {
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