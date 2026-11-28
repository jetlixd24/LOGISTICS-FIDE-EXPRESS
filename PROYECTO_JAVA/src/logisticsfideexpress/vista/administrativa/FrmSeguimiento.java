package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.modelo.SeguimientoCarga;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmCrudBase;
import logisticsfideexpress.vista.componentes.Tema;

/** RF26, RF28: registrar el avance de una carga y confirmar su entrega. */
public class FrmSeguimiento extends FrmCrudBase {

    private static final String EN_CURSO = "En curso";
    private static final String ENTREGADOS = "Entregados";
    private static final String TODOS = "Todos los viajes";

    private final ServiceFactory gestor;
    private final DefaultTableModel modeloServ = modelo("N.", "Cliente", "Ruta", "Carga", "Vehiculo", "Estado");
    private final JTable tablaServ = new JTable(modeloServ);
    private final DefaultTableModel modeloHist = modelo("Fecha y hora", "Estado", "Ubicacion", "Observacion");
    private final JTable tablaHist = new JTable(modeloHist);
    private final JComboBox<String> cmbFiltro = new JComboBox<>(new String[]{EN_CURSO, ENTREGADOS, TODOS});
    private final JTextField txtServicio = Estilo.campoLectura("");
    private final JTextField txtEstado = Estilo.campoLectura("");
    private final JTextField txtUbicacion = Estilo.campo();
    private final JTextField txtObservacion = Estilo.campo();
    private final JTextField txtReceptor = Estilo.campo();
    private final BotonRedondeado btnAvance = Estilo.botonSecundario("Registrar avance (en ruta)");
    private final BotonRedondeado btnEntrega = Estilo.botonPrimario("Confirmar entrega");
    private int idSeleccionado = 0;

    public FrmSeguimiento(ServiceFactory gestor) {
        super("Seguimiento de cargas", 1340, 800);
        this.gestor = gestor;
        cmbFiltro.setFont(Tema.NORMAL);

        Formulario f = new Formulario();
        f.agregar("Servicio", txtServicio);
        f.agregar("Estado actual", txtEstado);
        f.agregar("Ubicacion actual", txtUbicacion);
        f.agregar("Observacion (opcional)", txtObservacion);
        f.agregar("Receptor (solo al confirmar entrega)", txtReceptor);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnAvance);
        botones.add(btnEntrega);

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtro.setOpaque(false);
        filtro.add(Estilo.etiqueta("Mostrar:"));
        filtro.add(cmbFiltro);

        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.setOpaque(false);
        arriba.add(filtro, BorderLayout.NORTH);
        arriba.add(Estilo.tabla(tablaServ), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(0, 8));
        abajo.setOpaque(false);
        abajo.add(Estilo.subtitulo("Historial del servicio seleccionado"), BorderLayout.NORTH);
        abajo.add(Estilo.tabla(tablaHist), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, arriba, abajo);
        split.setOpaque(false);
        split.setBorder(null);
        split.setResizeWeight(0.5);
        split.setDividerSize(8);

        getContenido().add(armar("Registrar movimiento", f, botones, split), BorderLayout.CENTER);

        btnAvance.addActionListener(e -> registrar(EstadoServicio.EN_RUTA));
        btnEntrega.addActionListener(e -> registrar(EstadoServicio.ENTREGADO));
        cmbFiltro.addActionListener(e -> refrescar());
        tablaServ.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });
        limpiar();
        refrescar();
    }

    private void refrescar() {
        try {
            String filtro = (String) cmbFiltro.getSelectedItem();
            List<ServicioTransporte> lista = gestor.getServicios().listar();
            modeloServ.setRowCount(0);
            for (ServicioTransporte s : lista) {
                EstadoServicio e = s.getEstado();
                boolean enCurso = e == EstadoServicio.ASIGNADO || e == EstadoServicio.EN_RUTA;
                boolean mostrar = TODOS.equals(filtro) ? e != EstadoServicio.PENDIENTE && e != EstadoServicio.RECHAZADO
                        && e != EstadoServicio.APROBADO
                        : EN_CURSO.equals(filtro) ? enCurso : e == EstadoServicio.ENTREGADO;
                if (!mostrar) {
                    continue;
                }
                String vehiculo = "-";
                if (!s.getAsignaciones().isEmpty()) {
                    vehiculo = s.getAsignaciones().get(s.getAsignaciones().size() - 1).getVehiculo().getPlaca();
                }
                modeloServ.addRow(new Object[]{s.getId(), s.getCliente().getRazonSocial(),
                    s.getOrigen() + " -> " + s.getDestino(), s.getCarga().getDescripcion(), vehiculo, e});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void seleccionar() {
        int fila = tablaServ.getSelectedRow();
        if (fila < 0) {
            return;
        }
        try {
            idSeleccionado = (int) modeloServ.getValueAt(fila, 0);
            ServicioTransporte s = gestor.getServicios().obtener(idSeleccionado);
            txtServicio.setText("N. " + s.getId() + " - " + s.getCliente().getRazonSocial());
            txtEstado.setText(s.getEstado().toString());
            boolean asignado = s.getEstado() == EstadoServicio.ASIGNADO;
            boolean enRuta = s.getEstado() == EstadoServicio.EN_RUTA;
            btnAvance.setEnabled(asignado || enRuta);
            btnEntrega.setEnabled(enRuta);
            txtUbicacion.setEnabled(asignado || enRuta);
            txtObservacion.setEnabled(asignado || enRuta);
            txtReceptor.setEnabled(enRuta);
            cargarHistorial();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void cargarHistorial() throws Exception {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        modeloHist.setRowCount(0);
        for (SeguimientoCarga g : gestor.getSeguimiento().consultar(idSeleccionado)) {
            modeloHist.addRow(new Object[]{g.getFechaHora().format(fmt), g.getEstado(), g.getUbicacion(),
                g.getObservacion()});
        }
    }

    private void limpiar() {
        idSeleccionado = 0;
        tablaServ.clearSelection();
        modeloHist.setRowCount(0);
        for (JTextField t : new JTextField[]{txtServicio, txtEstado, txtUbicacion, txtObservacion, txtReceptor}) {
            t.setText("");
        }
        txtUbicacion.setEnabled(false);
        txtObservacion.setEnabled(false);
        txtReceptor.setEnabled(false);
        btnAvance.setEnabled(false);
        btnEntrega.setEnabled(false);
    }

    private void registrar(EstadoServicio nuevo) {
        if (nuevo == EstadoServicio.ENTREGADO
                && !confirmar("Confirmar la entrega del servicio N. " + idSeleccionado + "?")) {
            return;
        }
        try {
            gestor.getSeguimiento().registrarAvance(idSeleccionado, nuevo, txtUbicacion.getText(),
                    txtObservacion.getText(), txtReceptor.getText());
            info(nuevo == EstadoServicio.ENTREGADO ? "Entrega confirmada. El vehiculo y el conductor quedaron disponibles."
                    : "Avance registrado.");
            int id = idSeleccionado;
            limpiar();
            refrescar();
            for (int i = 0; i < modeloServ.getRowCount(); i++) {
                if ((int) modeloServ.getValueAt(i, 0) == id) {
                    tablaServ.setRowSelectionInterval(i, i);
                    break;
                }
            }
        } catch (Exception ex) {
            error(ex);
        }
    }
}
