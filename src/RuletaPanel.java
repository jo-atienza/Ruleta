import enums.Color;
import modelos.Casilla;
import modelos.GiroListener;
import modelos.MesaRuleta;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.List;

public class RuletaPanel extends JPanel {
    private final MesaRuleta mesa;
    private GiroListener listener;
    private double anguloRueda, anguloPelota1, anguloPelota2;
    private double velocidadRueda, velocidadPelota1, velocidadPelota2;
    private boolean pelota1Parada, pelota2Parada, ruedaParada;
    private Timer animationTimer;
    private Casilla[] resultados = null;

    private static final java.awt.Color ROJO_AWT = new java.awt.Color(241, 3, 3);
    private static final java.awt.Color NEGRO_AWT = java.awt.Color.BLACK;
    private static final java.awt.Color AZUL_AWT = new java.awt.Color(4, 4, 252);
    private static final java.awt.Color BLANCO_AWT = java.awt.Color.WHITE;
    private static final java.awt.Color VERDE_AWT = new java.awt.Color(54, 176, 54);

    public RuletaPanel(MesaRuleta mesa) {
        this.mesa = mesa;
        setPreferredSize(new Dimension(850, 850));
        setBackground(new java.awt.Color(200, 200, 200));
    }

    public void setGiroListener(GiroListener listener) {
        this.listener = listener;
    }

    public void iniciarGiro() {
        this.resultados = null;
        pelota1Parada = false;
        pelota2Parada = false;
        ruedaParada = false;

        velocidadRueda = 15.0 + (Math.random() * 5);
        velocidadPelota1 = -22.0 - (Math.random() * 5);
        velocidadPelota2 = -24.0 - (Math.random() * 5);

        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        animationTimer = new Timer(16, e -> actualizarFrame());
        animationTimer.start();
    }

    private void actualizarFrame() {
        anguloRueda = (anguloRueda + velocidadRueda) % 360;
        anguloPelota1 = (anguloPelota1 + velocidadPelota1) % 360;
        anguloPelota2 = (anguloPelota2 + velocidadPelota2) % 360;

        velocidadRueda *= 0.99;
        velocidadPelota1 *= 0.985;
        velocidadPelota2 *= 0.988;

        if (Math.abs(velocidadRueda) < 0.01) { ruedaParada = true; velocidadRueda = 0; }
        if (Math.abs(velocidadPelota1) < 0.01) { pelota1Parada = true; velocidadPelota1 = 0; }
        if (Math.abs(velocidadPelota2) < 0.01) { pelota2Parada = true; velocidadPelota2 = 0; }

        repaint();

        if (ruedaParada && pelota1Parada && pelota2Parada) {
            animationTimer.stop();
            determinarResultadosYNotificar();
        }
    }

    private void determinarResultadosYNotificar() {
        List<Casilla> casillas = mesa.getCasillas();
        double anguloPorCasilla = 360.0 / casillas.size();
        double anguloRelativoPelota1 = (anguloPelota1 - anguloRueda + 360) % 360;
        double anguloRelativoPelota2 = (anguloPelota2 - anguloRueda + 360) % 360;
        int indice1 = (int) (((anguloRelativoPelota1 + 270) % 360) / anguloPorCasilla);
        int indice2 = (int) (((anguloRelativoPelota2 + 270) % 360) / anguloPorCasilla);

        this.resultados = new Casilla[2];
        this.resultados[0] = casillas.get(indice1);
        this.resultados[1] = casillas.get(indice2);

        repaint();

        if (listener != null) {
            listener.giroTerminado(this.resultados);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centroX = getWidth() / 2;
        int centroY = getHeight() / 2;
        int radioExterior = Math.min(getWidth(), getHeight()) / 2 - 10;
        int radioInterior = (int) (radioExterior * 0.30);

        Graphics2D g2dRueda = (Graphics2D) g2d.create();
        g2dRueda.rotate(Math.toRadians(anguloRueda), centroX, centroY);
        List<Casilla> casillas = mesa.getCasillas();
        double anguloCasillaD = 360.0 / casillas.size();

        for (int i = 0; i < casillas.size(); i++) {
            Casilla c = casillas.get(i);
            double inicioAngulo = 360 - (i * anguloCasillaD) - 90 - anguloCasillaD;
            java.awt.Color colorCasilla = getAwtColor(c.getColor());
            g2dRueda.setColor(colorCasilla);
            g2dRueda.fill(new Arc2D.Double(centroX - radioExterior, centroY - radioExterior, radioExterior * 2, radioExterior * 2, inicioAngulo, -anguloCasillaD, Arc2D.PIE));

            double anguloTexto = Math.toRadians(inicioAngulo - anguloCasillaD / 2);
            double radioTexto = (radioExterior * 0.90) + (radioInterior * 0.10);
            int xTexto = (int) (centroX + Math.cos(anguloTexto) * radioTexto);
            int yTexto = (int) (centroY - Math.sin(anguloTexto) * radioTexto);
            String texto = String.valueOf(c.getNumero());
            g2dRueda.setFont(new Font("Arial", Font.BOLD, 18));
            g2dRueda.setColor(c.getColor() == Color.NEGRO || c.getColor() == Color.AZUL ? java.awt.Color.WHITE : java.awt.Color.BLACK);
            FontMetrics fm = g2dRueda.getFontMetrics();
            g2dRueda.drawString(texto, xTexto - fm.stringWidth(texto) / 2, yTexto + fm.getAscent() / 3);
        }
        g2dRueda.dispose();

        g2d.setColor(java.awt.Color.YELLOW);
        Polygon flecha = new Polygon();
        flecha.addPoint(centroX, centroY - radioExterior - 10);
        flecha.addPoint(centroX - 10, centroY - radioExterior + 15);
        flecha.addPoint(centroX + 10, centroY - radioExterior + 15);
        g2d.fill(flecha);
        g2d.setColor(java.awt.Color.DARK_GRAY);
        g2d.fill(new Ellipse2D.Double(centroX - radioInterior, centroY - radioInterior, radioInterior * 2, radioInterior * 2));

        // --- Dibuja las PELOTAS ---
        double p1Rad = Math.toRadians(anguloPelota1);
        double p2Rad = Math.toRadians(anguloPelota2);
        int radioP1 = radioExterior - 18;
        int radioP2 = radioExterior - 20;

        int p1X = (int) (centroX + radioP1 * Math.cos(p1Rad));
        int p1Y = (int) (centroY + radioP1 * Math.sin(p1Rad));
        g2d.setColor(java.awt.Color.YELLOW); //
        g2d.fillOval(p1X - 7, p1Y - 7, 14, 14);

        int p2X = (int) (centroX + radioP2 * Math.cos(p2Rad));
        int p2Y = (int) (centroY + radioP2 * Math.sin(p2Rad));
        g2d.setColor(java.awt.Color.YELLOW); //
        g2d.fillOval(p2X - 7, p2Y - 7, 14, 14);


        if (ruedaParada && resultados != null && resultados[0] != null) {
            String resStr = resultados[0].getNumero() + " | " + resultados[1].getNumero();
            g2d.setColor(java.awt.Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(resStr, centroX - fm.stringWidth(resStr) / 2, centroY + 8);
        }
        g2d.dispose();
    }

    private java.awt.Color getAwtColor(Color colorEnum) {
        switch (colorEnum) {
            case ROJO: return ROJO_AWT;
            case NEGRO: return NEGRO_AWT;
            case AZUL: return AZUL_AWT;
            case BLANCO: return BLANCO_AWT;
            case VERDE: return VERDE_AWT;
            default: return java.awt.Color.GRAY;
        }
    }
}