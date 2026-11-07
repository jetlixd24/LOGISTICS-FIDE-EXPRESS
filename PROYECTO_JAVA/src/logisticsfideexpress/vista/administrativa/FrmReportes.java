package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.controlador.GestorLogistico;
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.modelo.Producto;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.modelo.enums.EstadoVehiculo;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formato;
import logisticsfideexpress.vista.componentes.FrmBase;
import logisticsfideexpress.vista.componentes.PanelTarjeta;
import logisticsfideexpress.vista.componentes.Tema;

/** RF38 a RF40: reportes de servicios, flota, almacen, gastos, utilidad y cuentas por cobrar y pagar. */
public class FrmReportes extends FrmBase {

    private final GestorLogistico gestor;
    private final JLabel lblGastos = valor();
    private final JLabel lblUtilidad = valor();
    private final JLabel lblCobrar = valor();
    private final JLabel lblPagar = valor();
    private final JLabel lblPeriodo = Estilo.suave(" ");
    private final JTextField txtDesde = Estilo.campo();
    private final JTextField txtHasta = Estilo.campo();
    private final DefaultTableModel mServicios = tabla("Estado", "Cantidad");
    private final DefaultTableModel mFlota = tabla("Estado", "Vehiculos");
    private final DefaultTableModel mProductos = tabla("Codigo", "Producto", "Stock", "Minimo", "Proveedor");
    private final DefaultTableModel mFacturas = tabla("Numero", "Tipo", "Tercero", "Total", "Saldo", "Estado");
    private final JTextArea txtResumen = new JTextArea();

    public FrmReportes(GestorLogistico gestor) {
        super("Reportes", 1180, 800);
        this.gestor = gestor;

        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 14, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(tarjeta("Gastos aprobados", lblGastos));
        tarjetas.add(tarjeta("Utilidad liquidada", lblUtilidad));
        tarjetas.add(tarjeta("Cuentas por cobrar", lblCobrar));
        tarjetas.add(tarjeta("Cuentas por pagar", lblPagar));

        BotonRedondeado btnAplicar = Estilo.botonPrimario("Aplicar periodo");
        BotonRedondeado btnQuitar = Estilo.botonSecundario("Quitar periodo");
        JPanel periodo = new JPanel(new GridLayout(1, 0, 10, 0));
        periodo.setOpaque(false);
        periodo.add(Estilo.etiqueta("Desde AAAA-MM-DD"));
        periodo.add(txtDesde);
        periodo.add(Estilo.etiqueta("Hasta AAAA-MM-DD"));
        periodo.add(txtHasta);
        periodo.add(btnAplicar);
        periodo.add(btnQuitar);

        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        tarjetas.setAlignmentX(LEFT_ALIGNMENT);
        periodo.setAlignmentX(LEFT_ALIGNMENT);
        lblPeriodo.setAlignmentX(LEFT_ALIGNMENT);
        norte.add(tarjetas);
        norte.add(javax.swing.Box.createVerticalStrut(14));
        norte.add(periodo);
        norte.add(javax.swing.Box.createVerticalStrut(4));
        norte.add(lblPeriodo);
        norte.add(javax.swing.Box.createVerticalStrut(10));

        txtResumen.setEditable(false);
        txtResumen.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        txtResumen.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Tema.NEGRITA);
        tabs.addTab("Servicios por estado", Estilo.tabla(new JTable(mServicios)));
        tabs.addTab("Flota por estado", Estilo.tabla(new JTable(mFlota)));
        tabs.addTab("Productos bajo minimo", Estilo.tabla(new JTable(mProductos)));
        tabs.addTab("Facturas con saldo", Estilo.tabla(new JTable(mFacturas)));
        tabs.addTab("Resumen general", new JScrollPane(txtResumen));

        getContenido().setLayout(new BorderLayout(0, 0));
        getContenido().add(norte, BorderLayout.NORTH);
        getContenido().add(tabs, BorderLayout.CENTER);

        btnAplicar.addActionListener(e -> refrescar());
        btnQuitar.addActionListener(e -> {
            txtDesde.setText("");
            txtHasta.setText("");
            refrescar();
        });
        refrescar();
    }

    private static JLabel valor() {
        JLabel l = new JLabel("-");
        l.setFont(Tema.CIFRA.deriveFont(24f));
        l.setForeground(Tema.AZUL);
        return l;
    }

    private static DefaultTableModel tabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private PanelTarjeta tarjeta(String titulo, JLabel valor) {
        PanelTarjeta c = new PanelTarjeta();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setPreferredSize(new Dimension(200, 96));
        JLabel t = Estilo.etiqueta(titulo);
        t.setAlignmentX(LEFT_ALIGNMENT);
        valor.setAlignmentX(LEFT_ALIGNMENT);
        c.add(t);
        c.add(javax.swing.Box.createVerticalStrut(6));
        c.add(valor);
        return c;
    }

    private void refrescar() {
        try {
            String d = txtDesde.getText().trim();
            String h = txtHasta.getText().trim();
            boolean conPeriodo = !d.isEmpty() || !h.isEmpty();

            Map<EstadoServicio, Integer> servicios = conPeriodo
                    ? gestor.getReportes().serviciosPorEstado(d, h) : gestor.getReportes().serviciosPorEstado();
            double utilidad = conPeriodo
                    ? gestor.getReportes().utilidadTotal(d, h) : gestor.getReportes().utilidadTotal();
            lblPeriodo.setText(conPeriodo ? "Periodo aplicado a: servicios por estado y utilidad ("
                    + d + " a " + h + ")." : "Mostrando todos los datos (sin periodo).");

            lblGastos.setText(Formato.moneda(gestor.getReportes().totalGastosAprobados()));
            lblUtilidad.setText(Formato.moneda(utilidad));
            lblCobrar.setText(Formato.moneda(gestor.getReportes().cuentasPorCobrar()));
            lblPagar.setText(Formato.moneda(gestor.getReportes().cuentasPorPagar()));

            mServicios.setRowCount(0);
            for (Map.Entry<EstadoServicio, Integer> e : servicios.entrySet()) {
                mServicios.addRow(new Object[]{e.getKey(), e.getValue()});
            }
            mFlota.setRowCount(0);
            for (Map.Entry<EstadoVehiculo, Integer> e : gestor.getReportes().flotaPorEstado().entrySet()) {
                mFlota.addRow(new Object[]{e.getKey(), e.getValue()});
            }
            mProductos.setRowCount(0);
            for (Producto p : gestor.getReportes().productosBajoMinimo()) {
                mProductos.addRow(new Object[]{p.getCodigo(), p.getNombre(), p.getStock(), p.getStockMinimo(),
                    p.getProveedor() != null ? p.getProveedor().getRazonSocial() : "-"});
            }
            mFacturas.setRowCount(0);
            for (Factura f : gestor.getReportes().facturasConSaldo()) {
                mFacturas.addRow(new Object[]{f.getNumero(), f.getTipo(), f.getTerceroNombre(),
                    Formato.moneda(f.getTotal()), Formato.moneda(f.getSaldo()), f.getEstado()});
            }
            txtResumen.setText(gestor.getReportes().resumenGeneral());
            txtResumen.setCaretPosition(0);
        } catch (Exception ex) {
            error(ex);
        }
    }
}
