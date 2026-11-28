package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.modelo.AsignacionViaje;
import logisticsfideexpress.modelo.Conductor;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.enums.EstadoConductor;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.modelo.enums.EstadoVehiculo;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmCrudBase;
import logisticsfideexpress.vista.componentes.Opcion;
import logisticsfideexpress.vista.componentes.Tema;

/** RF24: asignar vehiculo y conductor disponibles a un servicio aprobado. */
@SuppressWarnings("unchecked")
public class FrmAsignaciones extends FrmCrudBase {

    private final ServiceFactory gestor;
    private final DefaultTableModel modelo = modelo("ID", "Servicio", "Cliente", "Ruta", "Vehiculo",
            "Conductor", "Salida", "Llegada", "Estado");
    private final JTable tabla = new JTable(modelo);
    private final JComboBox<Opcion<ServicioTransporte>> cmbServicio = new JComboBox<>();
    private final JComboBox<Opcion<Vehiculo>> cmbVehiculo = new JComboBox<>();
    private final JComboBox<Opcion<Conductor>> cmbConductor = new JComboBox<>();
    private final JTextField txtSalida = Estilo.campo();
    private final JLabel lblInfo = Estilo.suave(" ");
    private final BotonRedondeado btnAsignar = Estilo.botonPrimario("Asignar viaje");
    private final BotonRedondeado btnActualizar = Estilo.botonSecundario("Actualizar listas");
    private boolean cargando = false;

    public FrmAsignaciones(ServiceFactory gestor) {
        super("Asignaciones de viaje", 1320, 720);
        this.gestor = gestor;
        for (JComboBox<?> c : new JComboBox<?>[]{cmbServicio, cmbVehiculo, cmbConductor}) {
            c.setFont(Tema.NORMAL);
        }

        Formulario f = new Formulario();
        f.agregar("Servicio aprobado", cmbServicio);
        f.agregar("Conductor disponible", cmbConductor);
        f.agregar("Vehiculo disponible", cmbVehiculo);
        f.agregar("Fecha de salida AAAA-MM-DD (vacio = hoy)", txtSalida);
        f.agregarSolo(lblInfo, 8);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnAsignar);
        botones.add(btnActualizar);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(Estilo.subtitulo("Viajes asignados"), BorderLayout.WEST);

        montar("Nueva asignacion", f, botones, barra, tabla);

        btnAsignar.addActionListener(e -> asignar());
        btnActualizar.addActionListener(e -> recargar());
        cmbServicio.addActionListener(e -> actualizarInfo());
        cmbVehiculo.addActionListener(e -> actualizarInfo());
        cmbConductor.addActionListener(e -> sugerirVehiculo());
        recargar();
    }

    private void recargar() {
        cargando = true;
        try {
            cmbServicio.removeAllItems();
            for (ServicioTransporte s : gestor.getServicios().listar(EstadoServicio.APROBADO)) {
                cmbServicio.addItem(new Opcion<>(s, "N. " + s.getId() + " - " + s.getCliente().getRazonSocial()
                        + ": " + s.getOrigen() + " -> " + s.getDestino() + " (" + s.getCarga().getPesoKg() + " kg)"));
            }
            cmbVehiculo.removeAllItems();
            for (Vehiculo v : gestor.getVehiculos().listar(EstadoVehiculo.DISPONIBLE)) {
                cmbVehiculo.addItem(new Opcion<>(v, v.getPlaca() + " - " + v.getMarca()
                        + " (" + v.getCapacidadKg() + " kg)"));
            }
            cmbConductor.removeAllItems();
            for (Conductor c : gestor.getConductores().listar()) {
                if (c.getEstado() == EstadoConductor.DISPONIBLE) {
                    cmbConductor.addItem(new Opcion<>(c, c.getNombre() + " (" + c.getLicencia() + ")"));
                }
            }
        } catch (Exception ex) {
            error(ex);
        } finally {
            cargando = false;
        }
        actualizarInfo();
        refrescarTabla();
    }

    /** Al elegir un conductor se propone su vehiculo habitual si esta disponible. */
    private void sugerirVehiculo() {
        if (cargando) {
            return;
        }
        Opcion<Conductor> oc = (Opcion<Conductor>) cmbConductor.getSelectedItem();
        if (oc != null && oc.getValor().getVehiculoHabitual() != null) {
            int idHabitual = oc.getValor().getVehiculoHabitual().getId();
            for (int i = 0; i < cmbVehiculo.getItemCount(); i++) {
                if (cmbVehiculo.getItemAt(i).getValor().getId() == idHabitual) {
                    cmbVehiculo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void actualizarInfo() {
        if (cargando) {
            return;
        }
        Opcion<ServicioTransporte> os = (Opcion<ServicioTransporte>) cmbServicio.getSelectedItem();
        Opcion<Vehiculo> ov = (Opcion<Vehiculo>) cmbVehiculo.getSelectedItem();
        if (os == null) {
            lblInfo.setText("No hay servicios aprobados pendientes de asignar.");
            lblInfo.setForeground(Tema.TEXTO_SUAVE);
            return;
        }
        double peso = os.getValor().getCarga().getPesoKg();
        if (ov == null) {
            lblInfo.setText("Carga: " + peso + " kg. No hay vehiculos disponibles.");
            lblInfo.setForeground(Tema.ROJO);
            return;
        }
        double cap = ov.getValor().getCapacidadKg();
        boolean ok = peso <= cap;
        lblInfo.setText("Carga: " + peso + " kg  |  Capacidad: " + cap + " kg" + (ok ? "" : "  (EXCEDE LA CAPACIDAD)"));
        lblInfo.setForeground(ok ? Tema.VERDE : Tema.ROJO);
    }

    private void refrescarTabla() {
        try {
            List<AsignacionViaje> lista = gestor.getServicios().listarAsignaciones();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            modelo.setRowCount(0);
            for (AsignacionViaje a : lista) {
                ServicioTransporte s = a.getServicio();
                modelo.addRow(new Object[]{a.getId(), "N. " + s.getId(), s.getCliente().getRazonSocial(),
                    s.getOrigen() + " -> " + s.getDestino(),
                    a.getVehiculo().getPlaca(), a.getConductor().getNombre(),
                    a.getFechaSalida() != null ? a.getFechaSalida().format(fmt) : "-",
                    a.getFechaLlegada() != null ? a.getFechaLlegada().format(fmt) : "-", a.getEstado()});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void asignar() {
        Opcion<ServicioTransporte> os = (Opcion<ServicioTransporte>) cmbServicio.getSelectedItem();
        Opcion<Vehiculo> ov = (Opcion<Vehiculo>) cmbVehiculo.getSelectedItem();
        Opcion<Conductor> oc = (Opcion<Conductor>) cmbConductor.getSelectedItem();
        if (os == null || ov == null || oc == null) {
            error("Debe haber un servicio aprobado, un vehiculo disponible y un conductor disponible.");
            return;
        }
        try {
            gestor.getServicios().asignar(os.getValor().getId(), ov.getValor().getId(),
                    oc.getValor().getId(), txtSalida.getText());
            info("Viaje asignado. El servicio paso a estado ASIGNADO.");
            txtSalida.setText("");
            recargar();
        } catch (Exception ex) {
            error(ex);
        }
    }
}
