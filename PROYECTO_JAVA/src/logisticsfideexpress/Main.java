/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package logisticsfideexpress;

/**
 *
 * @author JETLI
 */
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.vista.login.FrmLogin;

/** Punto de entrada: crea la fachada de servicios (crea el administrador inicial) y abre el login. */
public class Main {

    public static void main(String[] args) {
        // Cualquier error que no capture una ventana llega aquí: mensaje claro y la app sigue abierta.
        Thread.setDefaultUncaughtExceptionHandler((hilo, ex) -> {
            String msg = (ex instanceof AccesoDatosException)
                    ? ex.getMessage()
                    : "Ocurrió un error inesperado: " + ex.getMessage();
            SwingUtilities.invokeLater(()
                    -> JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE));
        });

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // si falla, se usa el estilo por defecto de Swing
        }

        ServiceFactory gestor;
        try {
            gestor = new ServiceFactory(); // conserva aquí tu línea actual si usas getInstance()
        } catch (AccesoDatosException ex) {
            // Sin base de datos no se puede ni abrir el login: aviso claro y salida ordenada.
            JOptionPane.showMessageDialog(null,
                    ex.getMessage() + "\nInicie MySQL y vuelva a abrir la aplicación.",
                    "No se pudo conectar", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(() -> new FrmLogin(gestor).setVisible(true));
    }
}