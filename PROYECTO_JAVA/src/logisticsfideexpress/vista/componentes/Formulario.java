package logisticsfideexpress.vista.componentes;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JPanel;

/** Panel de formulario: cada campo lleva su etiqueta encima y ocupa todo el ancho. */
public class Formulario extends JPanel {

    private int fila = 0;

    public Formulario() {
        setOpaque(false);
        setLayout(new GridBagLayout());
    }

    public void agregar(String etiqueta, JComponent campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = fila++;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(8, 0, 3, 0);
        add(Estilo.etiqueta(etiqueta), g);

        GridBagConstraints g2 = new GridBagConstraints();
        g2.gridx = 0;
        g2.gridy = fila++;
        g2.weightx = 1;
        g2.fill = GridBagConstraints.HORIZONTAL;
        g2.insets = new Insets(0, 0, 2, 0);
        add(campo, g2);
    }

    /** Agrega un componente sin etiqueta (por ejemplo un boton o un mensaje). */
    public void agregarSolo(JComponent componente, int margenSuperior) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = fila++;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(margenSuperior, 0, 0, 0);
        add(componente, g);
    }
}
