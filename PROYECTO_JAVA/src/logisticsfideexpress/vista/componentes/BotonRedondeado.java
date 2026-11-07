package logisticsfideexpress.vista.componentes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/** Boton con esquinas redondeadas y efecto hover/pulsado (MouseListener). */
public class BotonRedondeado extends JButton {

    private final Color normal;
    private final Color hover;
    private boolean sobre;
    private boolean presionado;

    public BotonRedondeado(String texto, Color normal, Color hover) {
        super(texto);
        this.normal = normal;
        this.hover = hover;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setFont(Tema.NEGRITA);
        setBorder(new EmptyBorder(9, 22, 9, 22));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                sobre = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                sobre = false;
                presionado = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                presionado = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                presionado = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color c = normal;
        if (!isEnabled()) {
            c = new Color(190, 196, 206);
        } else if (presionado) {
            c = hover.darker();
        } else if (sobre) {
            c = hover;
        }
        g2.setColor(c);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        g2.dispose();
        super.paintComponent(g);
    }
}
