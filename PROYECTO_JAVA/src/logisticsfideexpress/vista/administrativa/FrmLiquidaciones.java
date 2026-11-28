package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.modelo.DetalleLiquidacion;
import logisticsfideexpress.modelo.Liquidacion;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formato;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmCrudBase;
import logisticsfideexpress.vista.componentes.Opcion;
import logisticsfideexpress.vista.componentes.Tema;

/** RF33, RF39: generar la liquidacion de un servicio entregado y consultar liquidaciones por periodo. */
@SuppressWarnings("unchecked")
public class FrmLiquidaciones extends FrmCrudBase {

    private final ServiceFactory gestor;
    private final DefaultTableModel modelo = modelo("ID", "Fecha", "Servicio N.", "Cliente", "Ruta",
            "Ingreso", "Gastos", "Utilidad");
    private final JTable tabla = new JTable(modelo);
    private final DefaultTableModel modeloDet = modelo("Concepto", "Monto");
    private final JTable tablaDet = new JTable(modeloDet);
    private final JComboBox<Opcion<ServicioTransporte>> cmbServicio = new JComboBox<>();
    private final JTextField txtDesde = Estilo.campo();
    private final JTextField txtHasta = Estilo.campo();
    private final JLabel lblTotal = new JLabel(" ");
    private final JLabel lblResumen = new JLabel(" ");
    private final BotonRedondeado btnGenerar = Estilo.botonPrimario("Generar liquidacion");
    private final BotonRedondeado btnFiltrar = Estilo.botonSecundario("Filtrar por periodo");
    private final BotonRedondeado btnTodas = Estilo.botonSecundario("Ver todas");
    private List<Liquidacion> actuales = new ArrayList<>();

    public FrmLiquidaciones(ServiceFactory gestor) {
        super("Liquidaciones", 1340, 800);
        this.gestor = gestor;
        cmbServicio.setFont(Tema.NORMAL);
        lblTotal.setFont(Tema.NEGRITA);
        lblTotal.setForeground(Tema.AZUL);
        lblResumen.setFont(Tema.NEGRITA);

        Formulario f = new Formulario();
        f.agregar("Servicio entregado sin liquidar", cmbServicio);
        f.agregarSolo(Estilo.subtitulo("Consultar por periodo"), 22);
        f.agregar("Desde AAAA-MM-DD", txtDesde);
        f.agregar("Hasta AAAA-MM-DD", txtHasta);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnGenerar);
        botones.add(btnFiltrar);
        botones.add(btnTodas);

        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.setOpaque(false);
        arriba.add(lblTotal, BorderLayout.NORTH);
        arriba.add(Estilo.tabla(tabla), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(0, 8));
        abajo.setOpaque(false);
        abajo.add(Estilo.subtitulo("Detalle de gastos de la liquidacion seleccionada"), BorderLayout.NORTH);
        abajo.add(Estilo.tabla(tablaDet), BorderLayout.CENTER);
        abajo.add(lblResumen, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, arriba, abajo);
        split.setOpaque(false);
        split.setBorder(null);
        split.setResizeWeight(0.55);
        split.setDividerSize(8);

        getContenido().add(armar("Nueva liquidacion", f, botones, split), BorderLayout.CENTER);

        btnGenerar.addActionListener(e -> generar());
        btnFiltrar.addActionListener(e -> filtrar());
        btnTodas.addActionListener(e -> {
            txtDesde.setText("");
            txtHasta.setText("");
            refrescar(null);
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                detalle();
            }
        });
        cargarServicios();
        refrescar(null);
    }

    /** Servicios ENTREGADOS que todavia no tienen liquidacion. */
    private void cargarServicios() {
        try {
            Set<Integer> liquidados = new HashSet<>();
            for (Liquidacion l : gestor.getLiquidaciones().listar()) {
                liquidados.add(l.getServicio().getId());
            }
            cmbServicio.removeAllItems();
            for (ServicioTransporte s : gestor.getServicios().listar(EstadoServicio.ENTREGADO)) {
                if (!liquidados.contains(s.getId())) {
                    cmbServicio.addItem(new Opcion<>(s, "N. " + s.getId() + " - " + s.getCliente().getRazonSocial()
                            + ": " + s.getOrigen() + " -> " + s.getDestino() + " (tarifa " + Formato.moneda(s.getTarifa()) + ")"));
                }
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    /** Si desde es null se muestran todas las liquidaciones. */
    private void refrescar(String[] periodo) {
        try {
            actuales = periodo == null ? gestor.getLiquidaciones().listar()
                    : gestor.getLiquidaciones().listar(periodo[0], periodo[1]);
            modelo.setRowCount(0);
            modeloDet.setRowCount(0);
            lblResumen.setText(" ");
            double total = 0;
            for (Liquidacion l : actuales) {
                ServicioTransporte s = l.getServicio();
                modelo.addRow(new Object[]{l.getId(), l.getFecha().format(Formato.FECHA), s.getId(),
                    s.getCliente().getRazonSocial(), s.getOrigen() + " -> " + s.getDestino(),
                    Formato.moneda(s.getTarifa()), Formato.moneda(l.getTotalGastos()), Formato.moneda(l.getUtilidad())});
                total += l.getUtilidad();
            }
            lblTotal.setText(actuales.size() + " liquidacion(es)  |  Utilidad total: " + Formato.moneda(total));
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void filtrar() {
        refrescar(new String[]{txtDesde.getText(), txtHasta.getText()});
    }

    private void detalle() {
        int fila = tabla.getSelectedRow();
        modeloDet.setRowCount(0);
        if (fila < 0 || fila >= actuales.size()) {
            lblResumen.setText(" ");
            return;
        }
        Liquidacion l = actuales.get(fila);
        for (DetalleLiquidacion d : l.getDetalles()) {
            modeloDet.addRow(new Object[]{d.getConcepto(), Formato.moneda(d.getMonto())});
        }
        lblResumen.setText("Ingreso " + Formato.moneda(l.getServicio().getTarifa()) + "  -  Gastos "
                + Formato.moneda(l.getTotalGastos()) + "  =  Utilidad " + Formato.moneda(l.getUtilidad()));
        lblResumen.setForeground(l.getUtilidad() >= 0 ? Tema.VERDE : Tema.ROJO);
    }

    private void generar() {
        Opcion<ServicioTransporte> o = (Opcion<ServicioTransporte>) cmbServicio.getSelectedItem();
        if (o == null) {
            error("No hay servicios entregados pendientes de liquidar.");
            return;
        }
        try {
            Liquidacion l = gestor.getLiquidaciones().generar(o.getValor().getId());
            info("Liquidacion generada. Utilidad: " + Formato.moneda(l.getUtilidad()));
            cargarServicios();
            refrescar(null);
        } catch (Exception ex) {
            error(ex);
        }
    }
}
