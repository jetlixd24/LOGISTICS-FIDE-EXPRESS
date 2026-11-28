package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.modelo.Proveedor;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.Transportista;
import logisticsfideexpress.modelo.enums.EstadoFactura;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.modelo.enums.TipoFactura;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formato;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmCrudBase;
import logisticsfideexpress.vista.componentes.Opcion;
import logisticsfideexpress.vista.componentes.Tema;

/** RF34 a RF36: emitir facturas de venta de servicios entregados y registrar facturas de compra. */
@SuppressWarnings("unchecked")
public class FrmFacturas extends FrmCrudBase {

    private static final String TODOS = "Todos";

    private final ServiceFactory gestor;
    private final DefaultTableModel modelo = modelo("ID", "Numero", "Tipo", "Fecha", "Tercero", "Total",
            "Saldo", "Estado");
    private final JTable tabla = new JTable(modelo);
    private final JComboBox<Opcion<ServicioTransporte>> cmbVenta = new JComboBox<>();
    private final JComboBox<String> cmbTipoTercero = new JComboBox<>(
            new String[]{Factura.TERCERO_PROVEEDOR, Factura.TERCERO_TRANSPORTISTA});
    private final JComboBox<Opcion<Integer>> cmbTercero = new JComboBox<>();
    private final JTextField txtNumero = Estilo.campo();
    private final JTextField txtTotal = Estilo.campo();
    private final JComboBox<Object> cmbFiltroTipo = new JComboBox<>();
    private final JComboBox<Object> cmbFiltroEstado = new JComboBox<>();
    private final BotonRedondeado btnEmitir = Estilo.botonPrimario("Emitir factura de venta");
    private final BotonRedondeado btnCompra = Estilo.botonSecundario("Registrar factura de compra");

    public FrmFacturas(ServiceFactory gestor) {
        super("Facturas", 1340, 800);
        this.gestor = gestor;
        for (JComboBox<?> c : new JComboBox<?>[]{cmbVenta, cmbTipoTercero, cmbTercero, cmbFiltroTipo, cmbFiltroEstado}) {
            c.setFont(Tema.NORMAL);
        }

        Formulario f = new Formulario();
        f.agregarSolo(Estilo.subtitulo("Venta (al cliente)"), 0);
        f.agregar("Servicio entregado sin facturar", cmbVenta);
        f.agregarSolo(btnEmitir, 8);
        f.agregarSolo(Estilo.subtitulo("Compra (proveedor o transportista)"), 22);
        f.agregar("Tipo de tercero", cmbTipoTercero);
        f.agregar("Tercero", cmbTercero);
        f.agregar("Numero de factura", txtNumero);
        f.agregar("Total (S/)", txtTotal);
        f.agregarSolo(btnCompra, 8);

        JPanel vacio = new JPanel();
        vacio.setOpaque(false);

        cmbFiltroTipo.addItem(TODOS);
        for (TipoFactura t : TipoFactura.values()) {
            cmbFiltroTipo.addItem(t);
        }
        cmbFiltroEstado.addItem(TODOS);
        for (EstadoFactura e : EstadoFactura.values()) {
            cmbFiltroEstado.addItem(e);
        }
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barra.setOpaque(false);
        barra.add(Estilo.etiqueta("Tipo:"));
        barra.add(cmbFiltroTipo);
        barra.add(Estilo.etiqueta("   Estado:"));
        barra.add(cmbFiltroEstado);

        montar("Emision y registro", f, vacio, barra, tabla);

        btnEmitir.addActionListener(e -> emitirVenta());
        btnCompra.addActionListener(e -> registrarCompra());
        cmbTipoTercero.addActionListener(e -> cargarTerceros());
        cmbFiltroTipo.addActionListener(e -> refrescar());
        cmbFiltroEstado.addActionListener(e -> refrescar());
        cargarVentas();
        cargarTerceros();
        refrescar();
    }

    /** Servicios ENTREGADOS cuya factura (F001-######) aun no existe. */
    private void cargarVentas() {
        try {
            Set<String> numeros = new HashSet<>();
            for (Factura f : gestor.getFacturacion().listarFacturas()) {
                numeros.add(f.getNumero());
            }
            cmbVenta.removeAllItems();
            for (ServicioTransporte s : gestor.getServicios().listar(EstadoServicio.ENTREGADO)) {
                if (!numeros.contains(String.format("F001-%06d", s.getId()))) {
                    cmbVenta.addItem(new Opcion<>(s, "N. " + s.getId() + " - " + s.getCliente().getRazonSocial()
                            + " (" + Formato.moneda(s.getTarifa()) + ")"));
                }
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void cargarTerceros() {
        try {
            cmbTercero.removeAllItems();
            if (Factura.TERCERO_PROVEEDOR.equals(cmbTipoTercero.getSelectedItem())) {
                for (Proveedor p : gestor.getAlmacen().listarProveedores()) {
                    cmbTercero.addItem(new Opcion<>(p.getId(), p.getRazonSocial() + " (" + p.getRuc() + ")"));
                }
            } else {
                for (Transportista t : gestor.getTransportistas().listar()) {
                    cmbTercero.addItem(new Opcion<>(t.getId(), t.getRazonSocial() + " (" + t.getRuc() + ")"));
                }
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void refrescar() {
        try {
            List<Factura> lista = gestor.getFacturacion().listarFacturas();
            Object ft = cmbFiltroTipo.getSelectedItem();
            Object fe = cmbFiltroEstado.getSelectedItem();
            modelo.setRowCount(0);
            for (Factura f : lista) {
                if (ft instanceof TipoFactura && f.getTipo() != ft) {
                    continue;
                }
                if (fe instanceof EstadoFactura && f.getEstado() != fe) {
                    continue;
                }
                modelo.addRow(new Object[]{f.getId(), f.getNumero(), f.getTipo(), f.getFecha().format(Formato.FECHA),
                    f.getTerceroNombre(), Formato.moneda(f.getTotal()), Formato.moneda(f.getSaldo()), f.getEstado()});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void emitirVenta() {
        Opcion<ServicioTransporte> o = (Opcion<ServicioTransporte>) cmbVenta.getSelectedItem();
        if (o == null) {
            error("No hay servicios entregados pendientes de facturar.");
            return;
        }
        try {
            Factura f = gestor.getFacturacion().emitirFacturaVenta(o.getValor().getId());
            info("Factura " + f.getNumero() + " emitida por " + Formato.moneda(f.getTotal()) + ".");
            cargarVentas();
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void registrarCompra() {
        Opcion<Integer> o = (Opcion<Integer>) cmbTercero.getSelectedItem();
        if (o == null) {
            error("No hay terceros registrados de ese tipo.");
            return;
        }
        try {
            Factura f = gestor.getFacturacion().registrarFacturaCompra(txtNumero.getText(),
                    (String) cmbTipoTercero.getSelectedItem(), o.getValor(), txtTotal.getText());
            info("Factura de compra " + f.getNumero() + " registrada.");
            txtNumero.setText("");
            txtTotal.setText("");
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }
}
