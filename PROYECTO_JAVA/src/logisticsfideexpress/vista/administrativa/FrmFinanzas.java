/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.vista.administrativa;

/**
 *
 * @author JETLI
 */
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.modelo.Pago;
import logisticsfideexpress.modelo.enums.EstadoFactura;
import logisticsfideexpress.modelo.enums.TipoFactura;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formato;
import logisticsfideexpress.vista.componentes.FrmBase;
import logisticsfideexpress.vista.componentes.Tema;

/**
 * RF35-RF37: cuentas por cobrar (clientes), por pagar (proveedores y
 * transportistas) e historial de pagos.
 */
public class FrmFinanzas extends FrmBase {

    private final ServiceFactory gestor;
    private final PanelCuentas porCobrar;
    private final PanelCuentas porPagar;
    private Runnable recargarHistorial;

    public FrmFinanzas(ServiceFactory gestor) {
        super("Finanzas", 1320, 760);
        this.gestor = gestor;
        porCobrar = new PanelCuentas(TipoFactura.VENTA, "Total facturado", "Saldo por cobrar");
        porPagar = new PanelCuentas(TipoFactura.COMPRA, "Total facturado", "Saldo por pagar");

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Por cobrar (clientes)", porCobrar);
        tabs.addTab("Por pagar (proveedores y transportistas)", porPagar);
        tabs.addTab("Historial de pagos", panelHistorial());
        tabs.addChangeListener(e -> {
            porCobrar.refrescar();
            porPagar.refrescar();
            recargarHistorial.run();
        });

        getContenido().setLayout(new BorderLayout());
        getContenido().add(tabs, BorderLayout.CENTER);
    }

    /** Pestaña con todos los pagos (cobros y pagos a terceros). */
    private JPanel panelHistorial() {
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"ID", "Fecha", "Factura", "Tipo", "Tercero", "Monto", "Metodo"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        JTable t = new JTable(m);
        JLabel lbl = Estilo.suave(" ");
        BotonRedondeado btn = Estilo.botonSecundario("Actualizar");

        recargarHistorial = () -> {
            try {
                m.setRowCount(0);
                double suma = 0;
                for (Pago p : gestor.getFacturacion().listarPagos()) {
                    Factura f = p.getFactura();
                    suma += p.getMonto();
                    m.addRow(new Object[]{p.getId(), p.getFecha().format(Formato.FECHA), f.getNumero(),
                        f.getTipo(), f.getTerceroNombre(), Formato.moneda(p.getMonto()), p.getMetodo()});
                }
                lbl.setText(m.getRowCount() + " pago(s)   |   Total pagado: " + Formato.moneda(suma));
            } catch (Exception ex) {
                error(ex);
            }
        };
        btn.addActionListener(e -> recargarHistorial.run());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        barra.setOpaque(false);
        barra.add(btn);

        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        p.add(barra, BorderLayout.NORTH);
        p.add(Estilo.tabla(t), BorderLayout.CENTER);
        p.add(lbl, BorderLayout.SOUTH);
        recargarHistorial.run();
        return p;
    }

    /** Una pestaña: filtro por estado, tabla, totales y botones de pago. */
    private class PanelCuentas extends JPanel {

        private final TipoFactura tipo;
        private final String etTotal;
        private final String etSaldo;
        private final List<Factura> facturas = new ArrayList<>();
        private final DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"ID", "Numero", "Tercero", "Fecha", "Total", "Saldo", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        private final JTable tabla = new JTable(modelo);
        private final JComboBox<String> cmbEstado = new JComboBox<>(
                new String[]{"Con saldo", "Pendientes", "Parciales", "Pagadas", "Todas"});
        private final JLabel lblTotales = Estilo.suave(" ");
        private final BotonRedondeado btnPagar = Estilo.botonPrimario("Registrar pago");
        private final BotonRedondeado btnVer = Estilo.botonSecundario("Ver pagos");

        PanelCuentas(TipoFactura tipo, String etTotal, String etSaldo) {
            this.tipo = tipo;
            this.etTotal = etTotal;
            this.etSaldo = etSaldo;
            setOpaque(false);
            setLayout(new BorderLayout(0, 10));
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

            cmbEstado.setFont(Tema.NORMAL);
            tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JPanel barra = new JPanel(new BorderLayout());
            barra.setOpaque(false);
            JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            izq.setOpaque(false);
            izq.add(Estilo.etiqueta("Mostrar:"));
            izq.add(cmbEstado);
            JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            der.setOpaque(false);
            der.add(btnVer);
            der.add(btnPagar);
            barra.add(izq, BorderLayout.WEST);
            barra.add(der, BorderLayout.EAST);

            add(barra, BorderLayout.NORTH);
            add(Estilo.tabla(tabla), BorderLayout.CENTER);
            add(lblTotales, BorderLayout.SOUTH);

            cmbEstado.addActionListener(e -> refrescar());
            btnPagar.addActionListener(e -> registrarPago());
            btnVer.addActionListener(e -> verPagos());
            refrescar();
        }

        private boolean coincide(Factura f, String filtro) {
            switch (filtro) {
                case "Pendientes":
                    return f.getEstado() == EstadoFactura.PENDIENTE;
                case "Parciales":
                    return f.getEstado() == EstadoFactura.PARCIAL;
                case "Pagadas":
                    return f.getEstado() == EstadoFactura.PAGADA;
                case "Con saldo":
                    return f.getEstado() != EstadoFactura.PAGADA;
                default:
                    return true;
            }
        }

        void refrescar() {
            try {
                String filtro = (String) cmbEstado.getSelectedItem();
                facturas.clear();
                modelo.setRowCount(0);
                double total = 0;
                double saldo = 0;
                for (Factura f : gestor.getFacturacion().listarFacturas(tipo)) {
                    if (!coincide(f, filtro)) {
                        continue;
                    }
                    facturas.add(f);
                    total += f.getTotal();
                    saldo += f.getSaldo();
                    modelo.addRow(new Object[]{f.getId(), f.getNumero(), f.getTerceroNombre(),
                        f.getFecha().format(Formato.FECHA), Formato.moneda(f.getTotal()),
                        Formato.moneda(f.getSaldo()), f.getEstado()});
                }
                lblTotales.setText(facturas.size() + " factura(s)   |   " + etTotal + ": "
                        + Formato.moneda(total) + "   |   " + etSaldo + ": " + Formato.moneda(saldo));
            } catch (Exception ex) {
                error(ex);
            }
        }

        private Factura seleccionada() {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                error("Seleccione una factura de la tabla.");
                return null;
            }
            return facturas.get(fila);
        }

        private void registrarPago() {
            Factura f = seleccionada();
            if (f == null) {
                return;
            }
            if (f.getEstado() == EstadoFactura.PAGADA) {
                error("La factura " + f.getNumero() + " ya esta pagada.");
                return;
            }
            JTextField txtMonto = Estilo.campo();
            txtMonto.setText(String.format(java.util.Locale.US, "%.2f", f.getSaldo()));
            JComboBox<String> cmbMetodo = new JComboBox<>(new String[]{"Transferencia", "Efectivo",
                "Yape", "Plin", "Deposito", "Tarjeta"});
            cmbMetodo.setFont(Tema.NORMAL);

            JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
            form.add(Estilo.etiqueta("Factura " + f.getNumero() + " - saldo " + Formato.moneda(f.getSaldo())));
            form.add(Estilo.etiqueta("Monto a pagar (S/)"));
            form.add(txtMonto);
            form.add(Estilo.etiqueta("Metodo de pago"));
            form.add(cmbMetodo);

            int r = JOptionPane.showConfirmDialog(FrmFinanzas.this, form, "Registrar pago",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                gestor.getFacturacion().registrarPago(f.getId(), txtMonto.getText(),
                        (String) cmbMetodo.getSelectedItem());
                info("Pago registrado correctamente en la factura " + f.getNumero() + ".");
                refrescar();
                if (recargarHistorial != null) {
                    recargarHistorial.run();
                }
            } catch (Exception ex) {
                error(ex);
            }
        }

        private void verPagos() {
            Factura f = seleccionada();
            if (f == null) {
                return;
            }
            try {
                List<Pago> pagos = gestor.getFacturacion().listarPagos(f.getId());
                StringBuilder sb = new StringBuilder();
                if (pagos.isEmpty()) {
                    sb.append("Esta factura aun no tiene pagos.");
                }
                for (Pago p : pagos) {
                    sb.append(p.getFecha().format(Formato.FECHA)).append("   ")
                            .append(Formato.moneda(p.getMonto())).append("   ")
                            .append(p.getMetodo()).append("\n");
                }
                JTextArea area = new JTextArea(sb.toString(), 8, 34);
                area.setEditable(false);
                JOptionPane.showMessageDialog(FrmFinanzas.this, new JScrollPane(area),
                        "Pagos de " + f.getNumero(), JOptionPane.PLAIN_MESSAGE);
            } catch (Exception ex) {
                error(ex);
            }
        }
    }
}