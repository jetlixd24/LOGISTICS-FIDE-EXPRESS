package logisticsfideexpress.vista.componentes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

/** Fabrica de componentes con el estilo del sistema. */
public final class Estilo {

    private Estilo() { }

    // ---------- botones ----------
    public static BotonRedondeado botonPrimario(String texto) {
        return new BotonRedondeado(texto, Tema.NARANJA, Tema.NARANJA_HOVER);
    }

    public static BotonRedondeado botonSecundario(String texto) {
        return new BotonRedondeado(texto, Tema.AZUL, Tema.AZUL_CLARO);
    }

    public static BotonRedondeado botonPeligro(String texto) {
        return new BotonRedondeado(texto, Tema.ROJO, Tema.ROJO_HOVER);
    }

    // ---------- etiquetas ----------
    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.TITULO);
        l.setForeground(Tema.AZUL);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.SUBTITULO);
        l.setForeground(Tema.AZUL);
        return l;
    }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.NORMAL);
        l.setForeground(Tema.TEXTO);
        return l;
    }

    public static JLabel suave(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.PEQUENA);
        l.setForeground(Tema.TEXTO_SUAVE);
        return l;
    }

    // ---------- campos ----------
    public static JTextField campo() {
        JTextField c = new JTextField();
        estilizar(c);
        return c;
    }

    public static JPasswordField clave() {
        JPasswordField c = new JPasswordField();
        estilizar(c);
        return c;
    }

    /** Campo de solo lectura (gris). */
    public static JTextField campoLectura(String texto) {
        JTextField c = campo();
        c.setText(texto);
        c.setEditable(false);
        c.setBackground(new Color(236, 239, 244));
        return c;
    }

    /** Borde naranja al recibir el foco (FocusListener). */
    public static void estilizar(JTextComponent c) {
        c.setFont(Tema.NORMAL);
        c.setForeground(Tema.TEXTO);
        c.setBorder(borde(Tema.BORDE));
        c.setPreferredSize(new Dimension(220, 38));
        c.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                c.setBorder(borde(Tema.NARANJA));
            }

            @Override
            public void focusLost(FocusEvent e) {
                c.setBorder(borde(Tema.BORDE));
            }
        });
    }

    private static Border borde(Color color) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(4, 10, 4, 10));
    }

    /** Ejecuta la accion cada vez que cambia el texto (DocumentListener): buscador en vivo. */
    public static void alEscribir(JTextComponent campo, Runnable accion) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { accion.run(); }

            @Override
            public void removeUpdate(DocumentEvent e) { accion.run(); }

            @Override
            public void changedUpdate(DocumentEvent e) { accion.run(); }
        });
    }

    // ---------- tablas ----------
    public static JScrollPane tabla(JTable t) {
        t.setFont(Tema.NORMAL);
        t.setForeground(Tema.TEXTO);
        t.setRowHeight(30);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(232, 235, 241));
        t.setSelectionBackground(Tema.SELECCION);
        t.setSelectionForeground(Tema.TEXTO);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setDefaultEditor(Object.class, null); // celdas no editables

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 34));
        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tb, Object v, boolean s, boolean f, int row, int col) {
                super.getTableCellRendererComponent(tb, v, s, f, row, col);
                setBackground(Tema.AZUL);
                setForeground(Color.WHITE);
                setFont(Tema.NEGRITA);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        };
        h.setDefaultRenderer(r);

        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        sp.getViewport().setBackground(Tema.BLANCO);
        return sp;
    }
}
