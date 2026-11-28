package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.modelo.CostoViaje;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoGasto;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formato;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmCrudBase;
import logisticsfideexpress.vista.componentes.Opcion;
import logisticsfideexpress.vista.componentes.Tema;

/** RF29 a RF32: registrar gastos de viaje, aprobarlos o rechazarlos y ver el total aprobado por servicio. */
@SuppressWarnings("unchecked")
public class FrmCostos extends FrmCrudBase {

    private static final String TODOS = "Todos";

    private final ServiceFactory gestor;
    private final DefaultTableModel modelo = modelo("ID", "Servicio N.", "Ruta", "Tipo de gasto", "Monto",
            "Fecha", "Estado");
    private final JTable tabla = new JTable(modelo);
    private final JComboBox<Opcion<ServicioTransporte>> cmbServicio = new JComboBox<>();
    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"combustible", "peaje",
        "viaticos", "mantenimiento", "estiba", "otros"});
    private final JTextField txtMonto = Estilo.campo();
    private final JTextField txtFecha = Estilo.campo();
    private final JComboBox<Object> cmbFiltro = new JComboBox<>();
    private final JLabel lblTotal = new JLabel(" ");
    private final BotonRedondeado btnRegistrar = Estilo.botonPrimario("Registrar gasto");
    private final BotonRedondeado btnAprobar = Estilo.botonSecundario("Aprobar gasto seleccionado");
    private final BotonRedondeado btnRechazar = Estilo.botonPeligro("Rechazar gasto seleccionado");
    private boolean cargando = false;

    public FrmCostos(ServiceFactory gestor) {
        super("Costos de viaje", 1320, 760);
        this.gestor = gestor;
        cmbServicio.setFont(Tema.NORMAL);
        cmbTipo.setFont(Tema.NORMAL);
        cmbTipo.setEditable(true);
        cmbFiltro.setFont(Tema.NORMAL);
        lblTotal.setFont(Tema.NEGRITA);
        lblTotal.setForeground(Tema.AZUL);

        Formulario f = new Formulario();
        f.agregar("Servicio (asignado, en ruta o entregado)", cmbServicio);
        f.agregar("Tipo de gasto (puedes escribir otro)", cmbTipo);
        f.agregar("Monto (S/)", txtMonto);
        f.agregar("Fecha AAAA-MM-DD (vacio = hoy)", txtFecha);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnRegistrar);
        botones.add(btnAprobar);
        botones.add(btnRechazar);

        cmbFiltro.addItem(TODOS);
        for (EstadoGasto e : EstadoGasto.values()) {
            cmbFiltro.addItem(e);
        }
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtro.setOpaque(false);
        filtro.add(Estilo.etiqueta("Estado del gasto:"));
        filtro.add(cmbFiltro);
        JPanel barra = new JPanel(new BorderLayout(0, 6));
        barra.setOpaque(false);
        barra.add(filtro, BorderLayout.NORTH);
        barra.add(lblTotal, BorderLayout.SOUTH);

        montar("Nuevo gasto", f, botones, barra, tabla);

        btnRegistrar.addActionListener(e -> registrar());
        btnAprobar.addActionListener(e -> resolver(true));
        btnRechazar.addActionListener(e -> resolver(false));
        cmbFiltro.addActionListener(e -> refrescar());
        cmbServicio.addActionListener(e -> {
            Opcion<ServicioTransporte> o = (Opcion<ServicioTransporte>) cmbServicio.getSelectedItem();
            if (!cargando && o != null) {
                mostrarTotal(o.getValor().getId());
            }
        });
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });
        cargarServicios();
        refrescar();
        botonesResolver(false);
    }

    private void cargarServicios() {
        cargando = true;
        try {
            cmbServicio.removeAllItems();
            for (ServicioTransporte s : gestor.getServicios().listar()) {
                EstadoServicio e = s.getEstado();
                if (e == EstadoServicio.ASIGNADO || e == EstadoServicio.EN_RUTA || e == EstadoServicio.ENTREGADO) {
                    cmbServicio.addItem(new Opcion<>(s, "N. " + s.getId() + " - " + s.getCliente().getRazonSocial()
                            + ": " + s.getOrigen() + " -> " + s.getDestino() + " [" + e + "]"));
                }
            }
        } catch (Exception ex) {
            error(ex);
        } finally {
            cargando = false;
        }
    }

    private void refrescar() {
        try {
            Object filtro = cmbFiltro.getSelectedItem();
            List<CostoViaje> lista = filtro instanceof EstadoGasto ? gestor.getCostos().listar((EstadoGasto) filtro)
                    : gestor.getCostos().listar();
            modelo.setRowCount(0);
            for (CostoViaje c : lista) {
                ServicioTransporte s = c.getServicio();
                modelo.addRow(new Object[]{c.getId(), s.getId(), s.getOrigen() + " -> " + s.getDestino(),
                    c.getTipo(), Formato.moneda(c.getMonto()), c.getFecha().format(Formato.FECHA), c.getEstado()});
            }
            botonesResolver(false);
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void botonesResolver(boolean activos) {
        btnAprobar.setEnabled(activos);
        btnRechazar.setEnabled(activos);
    }

    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            botonesResolver(false);
            return;
        }
        botonesResolver(EstadoGasto.PENDIENTE == modelo.getValueAt(fila, 6));
        mostrarTotal((int) modelo.getValueAt(fila, 1));
    }

    private void mostrarTotal(int idServicio) {
        try {
            lblTotal.setText("Total de gastos aprobados del servicio N. " + idServicio + ": "
                    + Formato.moneda(gestor.getCostos().totalAprobado(idServicio)));
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void registrar() {
        Opcion<ServicioTransporte> o = (Opcion<ServicioTransporte>) cmbServicio.getSelectedItem();
        if (o == null) {
            error("No hay servicios asignados, en ruta o entregados para registrar gastos.");
            return;
        }
        try {
            gestor.getCostos().registrar(o.getValor().getId(), String.valueOf(cmbTipo.getSelectedItem()),
                    txtMonto.getText(), txtFecha.getText());
            info("Gasto registrado. Queda PENDIENTE hasta que lo apruebes.");
            txtMonto.setText("");
            txtFecha.setText("");
            refrescar();
            mostrarTotal(o.getValor().getId());
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void resolver(boolean aprobar) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            error("Selecciona un gasto de la tabla.");
            return;
        }
        int id = (int) modelo.getValueAt(fila, 0);
        int idServicio = (int) modelo.getValueAt(fila, 1);
        if (!confirmar((aprobar ? "Aprobar" : "Rechazar") + " el gasto " + modelo.getValueAt(fila, 3) + " de "
                + modelo.getValueAt(fila, 4) + "?")) {
            return;
        }
        try {
            if (aprobar) {
                gestor.getCostos().aprobar(id);
            } else {
                gestor.getCostos().rechazar(id);
            }
            refrescar();
            mostrarTotal(idServicio);
        } catch (Exception ex) {
            error(ex);
        }
    }
}
