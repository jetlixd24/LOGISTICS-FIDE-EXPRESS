/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package logisticsfideexpress;

/**
 *
 * @author JETLI
 */
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import logisticsfideexpress.controlador.GestorLogistico;
import logisticsfideexpress.vista.login.FrmLogin;

/** Punto de entrada: crea la fachada (carga data/ y crea el administrador inicial) y abre el login. */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            // si falla, se usa el estilo por defecto de Swing
        }
        GestorLogistico gestor = new GestorLogistico();
        SwingUtilities.invokeLater(() -> new FrmLogin(gestor).setVisible(true));
    }
}
