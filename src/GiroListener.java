/**
 * Interfaz para notificar cuando la animación de la ruleta ha terminado.
 */
public interface GiroListener {
    /**
     * Se llama cuando la animación finaliza y se han determinado los resultados.
     * @param resultados Las dos casillas ganadoras.
     */
    void giroTerminado(Casilla[] resultados);
}