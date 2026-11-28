package logisticsfideexpress.vista.componentes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana base de los modulos de gestion: tarjeta con formulario a la izquierda
 * y tabla con barra de busqueda a la derecha.
 */
public abstract class FrmCrudBase extends FrmBase {

    protected FrmCrudBase(String titulo, int ancho, int alto) {
        super(titulo, ancho, alto);
    }

    /** Modelo de tabla con celdas no editables. */
    protected static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    /** Barra con una etiqueta y un campo de busqueda. */
    protected static JPanel barraBusqueda(String etiqueta, JTextField campo) {
        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);
        barra.add(Estilo.etiqueta(etiqueta), BorderLayout.WEST);
        barra.add(campo, BorderLayout.CENTER);
        return barra;
    }

    /** Arma el panel: tarjeta con formulario y botones a la izquierda, componente libre a la derecha. */
    protected JPanel armar(String subtitulo, JComponent formulario, JComponent botones, JComponent derecha) {
        PanelTarjeta izq = new PanelTarjeta();
        izq.setLayout(new BorderLayout(0, 8));
        izq.setPreferredSize(new Dimension(340, 0));
        izq.add(Estilo.subtitulo(subtitulo), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(formulario);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(14);
        izq.add(sp, BorderLayout.CENTER);
        izq.add(botones, BorderLayout.SOUTH);

        JPanel p = new JPanel(new BorderLayout(18, 0));
        p.setOpaque(false);
        p.add(izq, BorderLayout.WEST);
        p.add(derecha, BorderLayout.CENTER);
        return p;
    }

    /** Igual que armar con la derecha formada por una barra superior y una tabla. */
    protected JPanel armar(String subtitulo, JComponent formulario, JComponent botones,
            JComponent barra, JTable tabla) {
        JPanel der = new JPanel(new BorderLayout(0, 10));
        der.setOpaque(false);
        der.add(barra, BorderLayout.NORTH);
        der.add(Estilo.tabla(tabla), BorderLayout.CENTER);
        return armar(subtitulo, formulario, botones, der);
    }

    protected void montar(String subtitulo, JComponent formulario, JComponent botones,
            JComponent barra, JTable tabla) {
        getContenido().add(armar(subtitulo, formulario, botones, barra, tabla), BorderLayout.CENTER);
    }
}
