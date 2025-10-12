public class Main {
    public static void main(String[] args) {
        // Ejecuta la GUI en el Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(() -> {
            RuletaGUI frame = new RuletaGUI();
            frame.setVisible(true);
        });
    }
}