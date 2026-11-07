package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.controlador.GestorLogistico;
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.modelo.Pago;
import logisticsfideexpress.modelo.enums.EstadoFactura;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formato;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmMantenimiento;
import logisticsfideexpress.vista.componentes.Opcion;
import logisticsfideexpress.vista.componentes.Tema;

/** RF37: registrar pagos totales o parciales; el saldo y el estado de la factura se actualizan solos. */
@SuppressWarnings("unchecked")
public class FrmPagos extends FrmMantenimiento {

    private final GestorLogistico gestor;
    private final DefaultTableModel modelo = modelo("ID", "Fecha", "Factura", "Tipo", "Tercero", "Monto", "Metodo");
    private final JTable tabla = new JTable(modelo);
    private final JComboBox<Opcion<Factura>> cmbFactura = new JComboBox<>();
    private final JComboBox<Opcion<Factura>> cmbFiltro = new JComboBox<>();
    private final JComboBox<String> cmbMetodo = new JComboBox<>(new String[]{"Transferencia", "Efectivo",
        "Yape", "Plin", "Deposito", "Tarjeta"});
    private final JTextField txtMonto = Estilo.campo();
    private final JLabel lblInfo = Estilo.suave(" ");
    private final BotonRedondeado btnPagar = Estilo.botonPrimario("Registrar pago");
    private final BotonRedondeado btnSaldo = Estilo.botonSecundario("Pagar saldo total");
    private boolean cargando = false;

    public FrmPagos(GestorLogistico gestor) {
        super("Pagos", 1320, 740);
        this.gestor = gestor;
        for (JComboBox<?> c : new JComboBox<?>[]{cmbFactura, cmbFiltro, cmbMetodo}) {
            c.setFont(Tema.NORMAL);
        }
        cmbMetodo.setEditable(true);

        Formulario f = new Formulario();
        f.agregar("Factura con saldo", cmbFactura);
        f.agregarSolo(lblInfo, 0);
        f.agregar("Monto a pagar (S/)", txtMonto);
        f.agregar("Metodo de pago", cmbMetodo);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnPagar);
        botones.add(btnSaldo);

        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);
        barra.add(Estilo.etiqueta("Historial de pagos de:"), BorderLayout.WEST);
        barra.add(cmbFiltro, BorderLayout.CENTER);

        montar("Nuevo pago", f, botones, barra, tabla);

        btnPagar.addActionListener(e -> pagar());
        btnSaldo.addActionListener(e -> {
            Opcion<Factura> o = (Opcion<Factura>) cmbFactura.getSelectedItem();
            if (o != null) {
                txtMonto.setText(String.format(java.util.Locale.US, "%.2f", o.getValor().getSaldo()));
            }
        });
        cmbFactura.addActionListener(e -> mostrarInfo());
        cmbFiltro.addActionListener(e -> {
            if (!cargando) {
                refrescarTabla();
            }
        });
        recargar();
    }

    private void recargar() {
        cargando = true;
        try {
            Opcion<Factura> selF = (Opcion<Factura>) cmbFactura.getSelectedItem();
            int idSel = selF != null ? selF.getValor().getId() : -1;
            cmbFactura.removeAllItems();
            List<Factura> conSaldo = new ArrayList<>(gestor.getFacturacion().listarFacturas(EstadoFactura.PENDIENTE));
            conSaldo.addAll(gestor.getFacturacion().listarFacturas(EstadoFactura.PARCIAL));
            for (Factura x : conSaldo) {
                Opcion<Factura> o = new Opcion<>(x, x.getNumero() + " - " + x.getTerceroNombre()
                        + " (saldo " + Formato.moneda(x.getSaldo()) + ")");
                cmbFactura.addItem(o);
                if (x.getId() == idSel) {
                    cmbFactura.setSelectedItem(o);
                }
            }
            Opcion<Factura> selFiltro = (Opcion<Factura>) cmbFiltro.getSelectedItem();
            int idFiltro = selFiltro != null && selFiltro.getValor() != null ? selFiltro.getValor().getId() : -1;
            cmbFiltro.removeAllItems();
            cmbFiltro.addItem(new Opcion<>(null, "Todas las facturas"));
            for (Factura x : gestor.getFacturacion().listarFacturas()) {
                Opcion<Factura> o = new Opcion<>(x, x.getNumero() + " - " + x.getTerceroNombre());
                cmbFiltro.addItem(o);
                if (x.getId() == idFiltro) {
                    cmbFiltro.setSelectedItem(o);
                }
            }
        } catch (Exception ex) {
            error(ex);
        } finally {
            cargando = false;
        }
        mostrarInfo();
        refrescarTabla();
    }

    private void mostrarInfo() {
        Opcion<Factura> o = (Opcion<Factura>) cmbFactura.getSelectedItem();
        if (o == null) {
            lblInfo.setText("No hay facturas con saldo pendiente.");
            return;
        }
        Factura f = o.getValor();
        lblInfo.setText(f.getTipo() + "  |  Total " + Formato.moneda(f.getTotal()) + "  |  Saldo "
                + Formato.moneda(f.getSaldo()) + "  |  " + f.getEstado());
    }

    private void refrescarTabla() {
        try {
            Opcion<Factura> filtro = (Opcion<Factura>) cmbFiltro.getSelectedItem();
            List<Pago> lista = filtro != null && filtro.getValor() != null
                    ? gestor.getFacturacion().listarPagos(filtro.getValor().getId())
                    : gestor.getFacturacion().listarPagos();
            modelo.setRowCount(0);
            for (Pago p : lista) {
                Factura f = p.getFactura();
                modelo.addRow(new Object[]{p.getId(), p.getFecha().format(Formato.FECHA), f.getNumero(), f.getTipo(),
                    f.getTerceroNombre(), Formato.moneda(p.getMonto()), p.getMetodo()});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void pagar() {
        Opcion<Factura> o = (Opcion<Factura>) cmbFactura.getSelectedItem();
        if (o == null) {
            error("No hay facturas con saldo pendiente.");
            return;
        }
        Factura f = o.getValor();
        try {
            gestor.getFacturacion().registrarPago(f.getId(), txtMonto.getText(),
                    String.valueOf(cmbMetodo.getSelectedItem()));
            info("Pago registrado. Factura " + f.getNumero() + ": saldo " + Formato.moneda(f.getSaldo())
                    + ", estado " + f.getEstado() + ".");
            txtMonto.setText("");
            recargar();
        } catch (Exception ex) {
            error(ex);
        }
    }
}
