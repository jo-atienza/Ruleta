import enums.Color;
import enums.TipoApuesta;
import excepciones.MontoApuestaInvalidoException;
import excepciones.SaldoInsuficienteException;
import excepciones.TipoApuestaInvalidoException;
import modelos.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RuletaGUI extends JFrame implements GiroListener {
    private final JButton btnGirar;
    private final JTextField txtMonto;
    private final JTextField txtApuestaValor;
    private final JComboBox<TipoApuesta> cmbApuestaTipo;
    private final JTextArea txtResultados;
    private final JLabel lblSaldo;
    private final RuletaPanel ruletaPanel;
    private final MesaRuleta mesaRuleta;
    private final Jugador jugador;

    public RuletaGUI() {
        this.jugador = new Jugador(1000.0);
        this.mesaRuleta = new MesaRuleta();
        setTitle("Ruleta Arcoíris 2-Pelotas");
        setSize(1100, 950);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        lblSaldo = new JLabel("Saldo: $" + String.format("%.2f", jugador.getSaldo()));
        txtMonto = new JTextField("100", 5);
        txtApuestaValor = new JTextField("Rojo", 15);
        cmbApuestaTipo = new JComboBox<>(TipoApuesta.values());
        txtResultados = new JTextArea(15, 60);
        txtResultados.setEditable(false);
        btnGirar = new JButton("¡APOSTAR Y GIRAR!");
        ruletaPanel = new RuletaPanel(mesaRuleta);
        ruletaPanel.setGiroListener(this);

        JPanel pnlControl = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlControl.add(lblSaldo);
        pnlControl.add(new JLabel("Tipo:"));
        pnlControl.add(cmbApuestaTipo);
        pnlControl.add(new JLabel("Monto: $"));
        pnlControl.add(txtMonto);
        pnlControl.add(new JLabel("Valor (ej: 42 o Rojo o 15,Azul):"));
        pnlControl.add(txtApuestaValor);
        pnlControl.add(btnGirar);
        JPanel pnlTablero = new JPanel(new BorderLayout());
        pnlTablero.add(ruletaPanel, BorderLayout.CENTER);
        JPanel pnlResultados = new JPanel(new BorderLayout());
        pnlResultados.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        pnlResultados.add(new JLabel("Historial y Resultados:"), BorderLayout.NORTH);
        pnlResultados.add(new JScrollPane(txtResultados), BorderLayout.CENTER);

        add(pnlControl, BorderLayout.NORTH);
        add(pnlTablero, BorderLayout.CENTER);
        add(pnlResultados, BorderLayout.SOUTH);
        btnGirar.addActionListener(e -> procesarApuestaYGirar());
        setLocationRelativeTo(null);
    }

    private void procesarApuestaYGirar() {
        btnGirar.setEnabled(false);
        try {
            double monto = Double.parseDouble(txtMonto.getText().trim());
            TipoApuesta tipo = (TipoApuesta) cmbApuestaTipo.getSelectedItem();
            Object valor = obtenerValorApuesta(tipo, txtApuestaValor.getText().trim());
            Apuesta apuesta = new Apuesta(monto, tipo, valor);
            jugador.apostar(monto);
            actualizarSaldo();
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
        try {
            double monto = Double.parseDouble(txtMonto.getText().trim());
            TipoApuesta tipo = (TipoApuesta) cmbApuestaTipo.getSelectedItem();
            Object valor = obtenerValorApuesta(tipo, txtApuestaValor.getText().trim());
            Apuesta apuesta = new Apuesta(monto, tipo, valor);

            double ganancia = mesaRuleta.calcularGanancia(apuesta, resultados);
            txtResultados.append(generarMensajeResultado(apuesta, resultados, ganancia) + "\n");
            if (ganancia > 0) jugador.cobrar(ganancia);
            actualizarSaldo();
        } catch (Exception ex) {
            mostrarError("Error de Cálculo", "Error al procesar el resultado: " + ex.getMessage());
        } finally {
            btnGirar.setEnabled(true);
        }
    }

    private Object obtenerValorApuesta(TipoApuesta tipo, String textoValor) throws TipoApuestaInvalidoException {
        if (textoValor.isEmpty()) {
            throw new TipoApuestaInvalidoException("El campo 'Valor' no puede estar vacío.");
        }
        switch (tipo) {
            case NUMERO_ESPECIFICO:
                try {
                    return Integer.parseInt(textoValor);
                } catch (NumberFormatException e) {
                    throw new TipoApuestaInvalidoException("Para 'Número Específico', el valor debe ser un número.");
                }
            case COLOR_CLASICO:
            case COLOR_CUALQUIERA:
            case COLOR_UNICO:
                Color color = Color.fromString(textoValor);
                if (color == null) throw new TipoApuestaInvalidoException("Color inválido: '" + textoValor + "'.");
                return color;
            case COLOR_MIXTO:
                String[] partesMixto = textoValor.split(",");
                if (partesMixto.length != 2) throw new TipoApuestaInvalidoException("Para 'Color Mixto', ingresa dos colores separados por coma (ej: Rojo,Negro).");
                List<Color> colores = new ArrayList<>();
                for (String s : partesMixto) {
                    Color c = Color.fromString(s);
                    if (c == null) throw new TipoApuestaInvalidoException("Color inválido en la lista: '" + s + "'.");
                    colores.add(c);
                }
                return colores;
            case DOBLE_COINCIDENCIA:
                String[] partesDoble = textoValor.split(",");
                if (partesDoble.length != 2) throw new TipoApuestaInvalidoException("Para 'Doble Coincidencia', ingresa un número y un color (ej: 42,Rojo).");
                try {
                    int num = Integer.parseInt(partesDoble[0].trim());
                    Color c = Color.fromString(partesDoble[1]);
                    if (c == null) throw new TipoApuestaInvalidoException("Color inválido: '" + partesDoble[1] + "'.");
                    return Arrays.asList(num, c);
                } catch (NumberFormatException e) {
                    throw new TipoApuestaInvalidoException("El primer valor de 'Doble Coincidencia' debe ser un número.");
                }
            case PAR_IMPAR:
                if (!"Par".equalsIgnoreCase(textoValor) && !"Impar".equalsIgnoreCase(textoValor)) {
                    throw new TipoApuestaInvalidoException("Para 'Par / Impar', el valor debe ser 'Par' o 'Impar'.");
                }
                return textoValor;
            default:
                throw new TipoApuestaInvalidoException("Tipo de apuesta no soportado.");
        }
    }

    private String generarMensajeResultado(Apuesta apuesta, Casilla[] resultados, double gananciaTotal) {
        String msg = "Giro: " + resultados[0] + " y " + resultados[1] + ". ";
        if (gananciaTotal > 0) {
            msg += "¡GANASTE! Total Cobrado: $" + String.format("%.2f", gananciaTotal) + ".";
        } else {
            msg += "Perdiste $" + String.format("%.2f", apuesta.getMonto()) + ".";
        }
        if (resultados[0].getColor() == Color.VERDE || resultados[1].getColor() == Color.VERDE) {
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