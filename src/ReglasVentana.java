// Ubicación: src/com/uade/ruleta/ig/ReglasVentana.java

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

// Asume que GestorArchivos.java está creado en el paquete util
// import com.uade.ruleta.logica.util.GestorArchivos;

public class ReglasVentana extends JDialog {

    private boolean continuar = false;
    private final String ARCHIVO_REGLAS = "ReglasRuleta.txt"; // Nombre del archivo

    public ReglasVentana(Frame owner) {
        // JDialog modal: el usuario debe interactuar con ella antes de seguir.
        super(owner, "Menú: Reglas de la Ruleta Arcoíris", true);

        setSize(700, 500);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Si se cierra, no continúa
        setLocationRelativeTo(owner);

        cargarYMostrarReglas();

        JButton btnJugar = new JButton("¡Entendido! Empezar a Jugar");
        btnJugar.addActionListener(e -> {
            continuar = true; // Establece la bandera para que el Main sepa continuar
            dispose();        // Cierra la ventana
        });

        JPanel pnlSur = new JPanel();
        pnlSur.add(btnJugar);

        add(pnlSur, BorderLayout.SOUTH);
        setResizable(false);
    }

    private void cargarYMostrarReglas() {
        JTextArea areaTexto = new JTextArea();
        areaTexto.setEditable(false);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 13));

        try {
            // Llama al GestorArchivos para leer el contenido
            String reglas = GestorArchivos.leerArchivo(ARCHIVO_REGLAS);
            areaTexto.setText(reglas);

        } catch (IOException e) {
            areaTexto.setText("Error al cargar las reglas. Asegúrate que '" +
                    ARCHIVO_REGLAS + "' existe en la ruta principal del proyecto.");
            e.printStackTrace();
        }

        // Agrega el área de texto en un JScrollPane para que sea desplazable
        add(new JScrollPane(areaTexto), BorderLayout.CENTER);
    }

    //Metodo para saber si debe continuar con el juego
    public boolean debeContinuar() {
        return continuar;
    }
}