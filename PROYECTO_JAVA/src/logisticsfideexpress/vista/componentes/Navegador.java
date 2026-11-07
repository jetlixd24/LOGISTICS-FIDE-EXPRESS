package logisticsfideexpress.vista.componentes;

import java.awt.Window;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import logisticsfideexpress.controlador.GestorLogistico;
import logisticsfideexpress.vista.login.FrmLogin;

/**
 * Abre las ventanas de los modulos por nombre y evita duplicarlas.
 * Si la ventana todavia no existe en el proyecto, avisa en vez de fallar.
 */
public final class Navegador {

    private static final Map<String, JFrame> ABIERTAS = new HashMap<>();

    private Navegador() { }

    /** paquete: "administrativa" o "cliente"; clase: por ejemplo "FrmClientes". */
    public static void abrir(GestorLogistico gestor, String paquete, String clase) {
        String clave = paquete + "." + clase;
        JFrame existente = ABIERTAS.get(clave);
        if (existente != null && existente.isDisplayable()) {
            existente.setState(JFrame.NORMAL);
            existente.toFront();
            existente.requestFocus();
            return;
        }
        try {
            Class<?> c = Class.forName("logisticsfideexpress.vista." + paquete + "." + clase);
            JFrame f = (JFrame) c.getConstructor(GestorLogistico.class).newInstance(gestor);
            ABIERTAS.put(clave, f);
            f.setVisible(true);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            JOptionPane.showMessageDialog(null, "El modulo " + clase + " aun no esta disponible.",
                    "En construccion", JOptionPane.INFORMATION_MESSAGE);
        } catch (ReflectiveOperationException e) {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            JOptionPane.showMessageDialog(null, "No se pudo abrir " + clase + ": " + causa.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** RF04: cierra la sesion, cierra todas las ventanas y vuelve a FrmLogin. */
    public static void cerrarSesion(GestorLogistico gestor) {
        gestor.getUsuarios().cerrarSesion();
        ABIERTAS.clear();
        for (Window w : Window.getWindows()) {
            w.dispose();
        }
        new FrmLogin(gestor).setVisible(true);
    }
}
