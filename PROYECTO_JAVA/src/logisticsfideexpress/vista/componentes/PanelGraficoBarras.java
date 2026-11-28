/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.vista.componentes;

/**
 *
 * @author JETLI
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.Timer;

/** Gráfico de barras de ingresos vs egresos; las barras "crecen" al abrirse. */
public class PanelGraficoBarras extends JPanel {

    private static final Color COLOR_INGRESOS = new Color(0x2E7D32);
    private static final Color COLOR_EGRESOS = new Color(0xC62828);

    private final String[] etiquetas;
    private final double[] ingresos;
    private final double[] egresos;
    private float progreso = 0f; // 0 -> 1
    private final Timer animacion;

    public PanelGraficoBarras(String[] etiquetas, double[] ingresos, double[] egresos) {
        this.etiquetas = etiquetas;
        this.ingresos = ingresos;
        this.egresos = egresos;
        setBackground(Color.WHITE);
        animacion = new Timer(15, e -> {
            progreso += 0.04f;
            if (progreso >= 1f) {
                progreso = 1f;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        animacion.start();
    }

    @Override
    public void removeNotify() {
        animacion.stop();
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();

        if (etiquetas.length == 0) {
            String msg = "No hay pagos en el periodo seleccionado.";
            g2.setColor(Color.GRAY);
            g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
            g2.dispose();
            return;
        }

        int izq = 80, der = 20, arriba = 50, abajo = 40;
        int ancho = getWidth() - izq - der;
        int alto = getHeight() - arriba - abajo;
        int base = arriba + alto;

        double max = 1;
        for (int i = 0; i < etiquetas.length; i++) {
            max = Math.max(max, Math.max(ingresos[i], egresos[i]));
        }

        // leyenda
        g2.setColor(COLOR_INGRESOS);
        g2.fillRect(izq, 18, 12, 12);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Ingresos (cobros)", izq + 18, 29);
        g2.setColor(COLOR_EGRESOS);
        g2.fillRect(izq + 150, 18, 12, 12);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Egresos (pagos)", izq + 168, 29);

        // líneas guía y valores del eje Y
        for (int k = 0; k <= 4; k++) {
            int y = base - alto * k / 4;
            g2.setColor(new Color(0xE0E0E0));
            g2.drawLine(izq, y, izq + ancho, y);
            String valor = String.format("S/ %,.0f", max * k / 4);
            g2.setColor(Color.GRAY);
            g2.drawString(valor, izq - 8 - fm.stringWidth(valor), y + 4);
        }

        // barras por periodo
        int grupo = ancho / etiquetas.length;
        int barra = Math.max(8, Math.min(60, grupo / 3 - 4));
        int hueco = 6;
        for (int i = 0; i < etiquetas.length; i++) {
            int x0 = izq + i * grupo + (grupo - (2 * barra + hueco)) / 2;
            int hi = (int) (alto * ingresos[i] / max * progreso);
            int he = (int) (alto * egresos[i] / max * progreso);

            g2.setColor(COLOR_INGRESOS);
            g2.fillRoundRect(x0, base - hi, barra, hi, 8, 8);
            g2.setColor(COLOR_EGRESOS);
            g2.fillRoundRect(x0 + barra + hueco, base - he, barra, he, 8, 8);

            g2.setColor(Color.DARK_GRAY);
            if (progreso >= 1f) {
                dibujarValor(g2, fm, ingresos[i], x0, barra, base - hi);
                dibujarValor(g2, fm, egresos[i], x0 + barra + hueco, barra, base - he);
            }
            int centro = x0 + barra + hueco / 2;
            g2.drawString(etiquetas[i], centro - fm.stringWidth(etiquetas[i]) / 2, base + 18);
        }
        g2.setColor(Color.GRAY);
        g2.drawLine(izq, base, izq + ancho, base);
        g2.dispose();
    }

    private void dibujarValor(Graphics2D g2, FontMetrics fm, double valor, int x, int ancho, int yTope) {
        if (valor <= 0) {
            return;
        }
        String t = String.format("%,.0f", valor);
        g2.drawString(t, x + (ancho - fm.stringWidth(t)) / 2, yTope - 4);
    }
}