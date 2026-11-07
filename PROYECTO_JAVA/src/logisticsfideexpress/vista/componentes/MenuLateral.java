package logisticsfideexpress.vista.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/** Barra lateral azul con secciones, items con hover (MouseListener) y pie para la sesion. */
public class MenuLateral extends JPanel {

    private final JPanel lista = new JPanel();
    private final JPanel pie = new JPanel();

    public MenuLateral(String linea1, String linea2) {
        setLayout(new BorderLayout());
        setBackground(Tema.AZUL_OSCURO);
        setPreferredSize(new Dimension(250, 0));

        JPanel marca = new JPanel();
        marca.setLayout(new BoxLayout(marca, BoxLayout.Y_AXIS));
        marca.setBackground(Tema.AZUL_OSCURO);
        marca.setBorder(new EmptyBorder(24, 22, 16, 22));
        JLabel a = new JLabel(linea1);
        a.setFont(Tema.TITULO.deriveFont(20f));
        a.setForeground(Color.WHITE);
        JLabel b = new JLabel(linea2);
        b.setFont(Tema.PEQUENA);
        b.setForeground(Tema.NARANJA);
        marca.add(a);
        marca.add(b);
        add(marca, BorderLayout.NORTH);

        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBackground(Tema.AZUL_OSCURO);
        JScrollPane sp = new JScrollPane(lista, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        sp.getViewport().setBackground(Tema.AZUL_OSCURO);
        sp.getVerticalScrollBar().setUnitIncrement(14);
        add(sp, BorderLayout.CENTER);

        pie.setLayout(new BoxLayout(pie, BoxLayout.Y_AXIS));
        pie.setBackground(Tema.AZUL_OSCURO);
        pie.setBorder(new EmptyBorder(12, 18, 18, 18));
        add(pie, BorderLayout.SOUTH);
    }

    public void seccion(String texto) {
        JLabel l = new JLabel(texto.toUpperCase());
        l.setFont(Tema.PEQUENA.deriveFont(java.awt.Font.BOLD, 11f));
        l.setForeground(new Color(140, 160, 190));
        l.setBorder(new EmptyBorder(10, 22, 2, 10));
        l.setAlignmentX(LEFT_ALIGNMENT);
        lista.add(l);
    }

    public void item(String texto, Runnable accion) {
        lista.add(new Item(texto, accion));
    }

    /** Componentes del pie (nombre del usuario, boton cerrar sesion). */
    public JPanel getPie() {
        return pie;
    }

    public void espacioPie(int alto) {
        pie.add(Box.createVerticalStrut(alto));
    }

    /** Elemento del menu: hover con barra naranja y clic que ejecuta la accion. */
    private static class Item extends JPanel {
        private boolean sobre;

        Item(String texto, Runnable accion) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            setPreferredSize(new Dimension(250, 34));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            JLabel l = new JLabel(texto);
            l.setFont(Tema.NORMAL);
            l.setForeground(new Color(220, 228, 240));
            l.setBorder(BorderFactory.createEmptyBorder(0, 26, 0, 10));
            add(l, BorderLayout.CENTER);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    sobre = true;
                    l.setForeground(Color.WHITE);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    sobre = false;
                    l.setForeground(new Color(220, 228, 240));
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    accion.run();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (sobre) {
                g.setColor(Tema.AZUL_CLARO);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Tema.NARANJA);
                g.fillRect(0, 0, 5, getHeight());
            }
            super.paintComponent(g);
        }
    }
}
