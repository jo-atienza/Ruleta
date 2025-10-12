import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

public class RuletaPanel extends JPanel {
    private final MesaRuleta mesa;

    // --- Variables de Estado para la Animación (Reintegradas) ---
    private Casilla[] resultados = new Casilla[2];
    private double anguloRotacion = 0;
    private boolean isGirando = false;
    // -------------------------------------------------------------

    public RuletaPanel(MesaRuleta mesa) {
        this.mesa = mesa;
        setPreferredSize(new Dimension(600, 600)); // Mantengo tu tamaño
        setBackground(new Color(200, 200, 200));
    }

    // --- Métodos de Animación (Requeridos por RuletaGUI) ---

    public void setResultados(Casilla[] res) {
        this.resultados = res;
        if (!isGirando) {
            repaint();
        }
    }

    public void setIsGirando(boolean girando) {
        this.isGirando = girando;
    }

    public void avanzarGiro() {
        if (isGirando) {
            // Lógica de avance de ángulo para la rotación visual
            anguloRotacion += 10;
            if (anguloRotacion >= 360) {
                anguloRotacion -= 360;
            }
            repaint(); // Solicita el repintado
        }
    }

    // --- Lógica de Dibujo (Basada en tu versión simplificada) ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        int centroX = getWidth() / 2;
        int centroY = getHeight() / 2;
        int radioExterior = Math.min(getWidth(), getHeight()) / 2 - 40;
        int radioInterior = (int) (radioExterior * 0.55);

        List<Casilla> casillas = mesa.getCasillas();
        double anguloCasilla = 360.0 / casillas.size();

        // Aplicar ROTACIÓN para la animación de giro
        g2d.rotate(Math.toRadians(anguloRotacion), centroX, centroY);

        // 🔁 Dibujo en sentido horario, corrigiendo desplazamiento
        for (int i = 0; i < casillas.size(); i++) {
            Casilla c = casillas.get(i);

            // Calcula el ángulo de inicio y el color
            double inicioAngulo = 360 - (i * anguloCasilla) - 90 - anguloCasilla;
            Color colorCasilla = getColorFromNombre(c.getColor());
            g2d.setColor(colorCasilla);

            g2d.fill(new Arc2D.Double(
                    centroX - radioExterior, centroY - radioExterior,
                    radioExterior * 2, radioExterior * 2,
                    inicioAngulo, -anguloCasilla, Arc2D.PIE
            ));

            // 🔢 Dibujo del número (sin rotación compleja)
            double anguloTexto = Math.toRadians(inicioAngulo - anguloCasilla / 2);
            double radioTexto = (radioExterior + radioInterior) / 2.0;

            int xTexto = (int) (centroX + Math.cos(anguloTexto) * radioTexto);
            int yTexto = (int) (centroY - Math.sin(anguloTexto) * radioTexto);

            String texto = String.valueOf(c.getNumero());
            g2d.setFont(new Font("Arial", Font.BOLD, 14));

            // Contraste según color de fondo
            if (c.getColor().equalsIgnoreCase("Negro") || c.getColor().equalsIgnoreCase("Azul")) {
                g2d.setColor(Color.WHITE);
            } else {
                g2d.setColor(Color.BLACK);
            }

            // Centramos el texto
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(texto);
            int textHeight = fm.getAscent();

            g2d.drawString(texto, xTexto - textWidth / 2, yTexto + textHeight / 3);
        }

        // Remover la rotación antes de dibujar elementos fijos (índice, centro, pelotas)
        g2d.rotate(-Math.toRadians(anguloRotacion), centroX, centroY);

        // 🟡 Flecha indicadora arriba (ESTÁTICA)
        g2d.setColor(Color.YELLOW);
        Polygon flecha = new Polygon();
        flecha.addPoint(centroX, centroY - radioExterior - 10);
        flecha.addPoint(centroX - 10, centroY - radioExterior + 15);
        flecha.addPoint(centroX + 10, centroY - radioExterior + 15);
        g2d.fill(flecha);

        // 🔘 Círculo interior
        g2d.setColor(getBackground());
        g2d.fill(new Ellipse2D.Double(
                centroX - radioInterior, centroY - radioInterior,
                radioInterior * 2, radioInterior * 2
        ));

        // 🔴⚪ Dibujo de Pelotas y Resultados (Solo cuando NO está girando)
        if (!isGirando && resultados[0] != null) {

            // Lógica de Posicionamiento de Pelotas sobre la zona de caída (Flecha)

            // Índice de la casilla ganadora (para la posición de la pelota)
            int idx1 = getCasillaIndex(resultados[0].getNumero(), casillas);
            int idx2 = getCasillaIndex(resultados[1].getNumero(), casillas);

            // Ángulo de la casilla 1 (se dibuja en el punto de la flecha)
            double anguloCasilla1 = (idx1 * anguloCasilla) + anguloCasilla / 2;

            // La ruleta no está rotando ahora, así que la posición final es 0 grados (arriba)

            String resStr = resultados[0].getNumero() + " | " + resultados[1].getNumero();
            g2d.setFont(new Font("Arial", Font.BOLD, 18));

            // Dibuja las pelotas en el centro (simplificado si no queremos mover la flecha)
            g2d.setColor(Color.WHITE);
            g2d.fillOval(centroX - 25, centroY - 10, 15, 15);
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(centroX + 10, centroY + 5, 15, 15);

            g2d.setColor(Color.BLACK);
            g2d.drawString(resStr, centroX - g2d.getFontMetrics().stringWidth(resStr) / 2, centroY + 5);
        }

        g2d.dispose();
    }

    // --- Métodos Auxiliares ---

    private int getCasillaIndex(int numero, List<Casilla> casillas) {
        for (int i = 0; i < casillas.size(); i++) {
            if (casillas.get(i).getNumero() == numero) {
                return i;
            }
        }
        return -1;
    }

    private Color getColorFromNombre(String nombre) {
        switch (nombre.toLowerCase()) {
            case "rojo":
                return Color.RED;
            case "negro":
                return Color.BLACK;
            case "azul":
                return new Color(0, 0, 180);
            case "blanco":
                return Color.WHITE;
            case "verde":
                return new Color(0, 140, 0); // Verde oscuro
            default:
                return Color.GRAY;
        }
    }
}