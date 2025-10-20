import java.io.BufferedReader;
import java.io.InputStreamReader; // Nuevo
import java.io.InputStream;      // Nuevo
import java.io.IOException;

public class GestorArchivos {

    // Cambiamos el metodo para usar el ClassLoader
    public static <txt> String leerArchivo(String nombreArchivo) throws IOException {
        StringBuilder contenido = new StringBuilder();

        // 1. Obtener el archivo como un Stream de recursos
        // Esto busca el archivo DENTRO del JAR compilado o en la carpeta src
        InputStream is = GestorArchivos.class.getClassLoader().getResourceAsStream(nombreArchivo);

        if (is == null) {
            // Arroja la excepción si no lo encuentra como recurso
            throw new IOException("Recurso no encontrado: " + nombreArchivo);
        }

        // 2. Usar BufferedReader con el InputStream
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                contenido.append(linea).append("\n");
            }
        }

        return contenido.toString();
    }
}

