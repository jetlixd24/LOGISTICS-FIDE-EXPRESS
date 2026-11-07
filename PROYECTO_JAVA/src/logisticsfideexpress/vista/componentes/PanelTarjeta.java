package logisticsfideexpress.vista.componentes;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Panel blanco con esquinas redondeadas y borde suave (estilo "tarjeta"). */
public class PanelTarjeta extends JPanel {

    public PanelTarjeta() {
        setOpaque(false);
        setBorder(new EmptyBorder(16, 18, 16, 18));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Tema.BLANCO);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
        g2.setColor(Tema.BORDE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
        g2.dispose();
        super.paintComponent(g);
    }
}
