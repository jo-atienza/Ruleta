public class Main {
    public static void main(String[] args) {
        // Ejecuta la GUI en el Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Mostrar la ventana de reglas
            ReglasVentana reglas = new ReglasVentana(null);
            reglas.setVisible(true);

            // Si el usuario hizo clic en "Jugar", iniciar la ruleta
            if (reglas.debeContinuar()) {
                RuletaGUI frame = new RuletaGUI();
                frame.setVisible(true);
            } else {
                // Si el usuario simplemente cerró la ventana, el programa termina
                System.exit(0);
            }

            RuletaGUI frame = new RuletaGUI();
            frame.setVisible(true);
            
        });
    }
}