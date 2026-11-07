package logisticsfideexpress.vista.componentes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Ventana base de los modulos: encabezado azul con franja naranja y area de
 * contenido.
 */
public abstract class FrmBase extends JFrame {

    private final JPanel contenido = new JPanel(new BorderLayout());

    protected FrmBase(String titulo, int ancho, int alto) {
        setTitle("LOGISTICS FIDE EXPRESS - " + titulo);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        // Ajusta la ventana a la pantalla (laptops de 768 px de alto)
        java.awt.Rectangle pantalla = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(ancho, pantalla.width), Math.min(alto, pantalla.height));
        setMinimumSize(new Dimension(Math.min(ancho, 700), Math.min(alto, 500)));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Tema.AZUL);
        encabezado.setBorder(new EmptyBorder(14, 24, 12, 24));
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(Tema.TITULO);
        lbl.setForeground(java.awt.Color.WHITE);
        encabezado.add(lbl, BorderLayout.WEST);

        JPanel franja = new JPanel();
        franja.setBackground(Tema.NARANJA);
        franja.setPreferredSize(new Dimension(0, 4));

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(encabezado, BorderLayout.CENTER);
        norte.add(franja, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        contenido.setBackground(Tema.FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        add(contenido, BorderLayout.CENTER);
    }

    protected JPanel getContenido() {
        return contenido;
    }

    protected void info(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    protected void error(Exception ex) {
        error(ex.getMessage());
    }

    protected boolean confirmar(String pregunta) {
        return JOptionPane.showConfirmDialog(this, pregunta, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
