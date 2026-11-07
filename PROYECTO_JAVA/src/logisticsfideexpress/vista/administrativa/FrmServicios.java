package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.controlador.GestorLogistico;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmMantenimiento;
import logisticsfideexpress.vista.componentes.Tema;

/** RF22, RF23, RF25: revisar solicitudes de los clientes, aprobarlas (con tarifa) o rechazarlas (con motivo). */
public class FrmServicios extends FrmMantenimiento {

    private static final String TODOS = "Todos";

    private final GestorLogistico gestor;
    private final DefaultTableModel modelo = modelo("N.", "Fecha", "Cliente", "Origen", "Destino",
            "Carga", "Peso (kg)", "Estado", "Tarifa");
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtBuscar = Estilo.campo();
    private final JComboBox<Object> cmbFiltro = new JComboBox<>();
    private final JTextField txtCliente = Estilo.campoLectura("");
    private final JTextField txtRuta = Estilo.campoLectura("");
    private final JTextField txtCarga = Estilo.campoLectura("");
    private final JTextField txtEstado = Estilo.campoLectura("");
    private final JTextField txtTarifa = Estilo.campo();
    private final JTextField txtMotivo = Estilo.campo();
    private final BotonRedondeado btnAprobar = Estilo.botonPrimario("Aprobar solicitud");
    private final BotonRedondeado btnRechazar = Estilo.botonPeligro("Rechazar solicitud");
    private int idSeleccionado = 0;

    public FrmServicios(GestorLogistico gestor) {
        super("Solicitudes y servicios", 1320, 780);
        this.gestor = gestor;
        cmbFiltro.setFont(Tema.NORMAL);

        Formulario f = new Formulario();
        f.agregar("Cliente", txtCliente);
        f.agregar("Ruta", txtRuta);
        f.agregar("Carga", txtCarga);
        f.agregar("Estado actual", txtEstado);
        f.agregar("Tarifa a cobrar (para aprobar)", txtTarifa);
        f.agregar("Motivo (para rechazar)", txtMotivo);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnAprobar);
        botones.add(btnRechazar);

        cmbFiltro.addItem(TODOS);
        for (EstadoServicio e : EstadoServicio.values()) {
            cmbFiltro.addItem(e);
        }
        cmbFiltro.setSelectedItem(EstadoServicio.PENDIENTE);

        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(Estilo.etiqueta("Buscar:  "));
        barra.add(izq, BorderLayout.WEST);
        barra.add(txtBuscar, BorderLayout.CENTER);
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        der.setOpaque(false);
        der.add(Estilo.etiqueta("Estado:"));
        der.add(cmbFiltro);
        barra.add(der, BorderLayout.EAST);

        montar("Detalle de la solicitud", f, botones, barra, tabla);

        btnAprobar.addActionListener(e -> aprobar());
        btnRechazar.addActionListener(e -> rechazar());
        cmbFiltro.addActionListener(e -> refrescar());
        Estilo.alEscribir(txtBuscar, this::refrescar);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });
        limpiar();
        refrescar();
    }

    private void refrescar() {
        try {
            String t = txtBuscar.getText().trim();
            List<ServicioTransporte> lista = t.isEmpty() ? gestor.getServicios().listar()
                    : gestor.getServicios().listar(t);
            Object filtro = cmbFiltro.getSelectedItem();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            modelo.setRowCount(0);
            for (ServicioTransporte s : lista) {
                if (filtro instanceof EstadoServicio && s.getEstado() != filtro) {
                    continue;
                }
                modelo.addRow(new Object[]{s.getId(), s.getFechaSolicitud().format(fmt),
                    s.getCliente().getRazonSocial(), s.getOrigen(), s.getDestino(),
                    s.getCarga().getDescripcion(), s.getCarga().getPesoKg(), s.getEstado(),
                    s.getTarifa() > 0 ? String.valueOf(s.getTarifa()) : "-"});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        try {
            idSeleccionado = (int) modelo.getValueAt(fila, 0);
            ServicioTransporte s = gestor.getServicios().obtener(idSeleccionado);
            txtCliente.setText(s.getCliente().getRazonSocial() + " (" + s.getCliente().getNombre() + ")");
            txtRuta.setText(s.getOrigen() + " -> " + s.getDestino());
            txtCarga.setText(s.getCarga().getDescripcion() + " - " + s.getCarga().getPesoKg() + " kg");
            txtEstado.setText(s.getEstado().toString());
            txtTarifa.setText(s.getTarifa() > 0 ? String.valueOf(s.getTarifa()) : "");
            txtMotivo.setText(s.getMotivoRechazo() != null ? s.getMotivoRechazo() : "");
            boolean pendiente = s.getEstado() == EstadoServicio.PENDIENTE;
            txtTarifa.setEnabled(pendiente);
            txtMotivo.setEnabled(pendiente);
            btnAprobar.setEnabled(pendiente);
            btnRechazar.setEnabled(pendiente);
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void limpiar() {
        idSeleccionado = 0;
        tabla.clearSelection();
        for (JTextField t : new JTextField[]{txtCliente, txtRuta, txtCarga, txtEstado, txtTarifa, txtMotivo}) {
            t.setText("");
        }
        txtTarifa.setEnabled(false);
        txtMotivo.setEnabled(false);
        btnAprobar.setEnabled(false);
        btnRechazar.setEnabled(false);
    }

    private void aprobar() {
        if (!confirmar("Aprobar la solicitud N. " + idSeleccionado + " con tarifa " + txtTarifa.getText() + "?")) {
            return;
        }
        try {
            gestor.getServicios().aprobar(idSeleccionado, txtTarifa.getText());
            info("Solicitud aprobada. Ya puedes asignarle vehiculo y conductor en Asignaciones.");
            limpiar();
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void rechazar() {
        if (!confirmar("Rechazar la solicitud N. " + idSeleccionado + "?")) {
            return;
        }
        try {
            gestor.getServicios().rechazar(idSeleccionado, txtMotivo.getText());
            info("Solicitud rechazada.");
            limpiar();
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }
}
