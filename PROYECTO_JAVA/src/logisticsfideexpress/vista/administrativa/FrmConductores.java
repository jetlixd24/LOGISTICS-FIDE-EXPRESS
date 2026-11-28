package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.modelo.Conductor;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.enums.EstadoVehiculo;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmCrudBase;
import logisticsfideexpress.vista.componentes.Tema;

/** RF14, RF15: registrar conductores y asociarles su vehiculo habitual. */
public class FrmConductores extends FrmCrudBase {

    private static final String SIN_VEHICULO = "(Sin vehiculo)";

    private final ServiceFactory gestor;
    private final DefaultTableModel modelo = modelo("ID", "DNI", "Nombre", "Licencia", "Categoria",
            "Estado", "Vehiculo habitual");
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtBuscar = Estilo.campo();
    private final JTextField txtDni = Estilo.campo();
    private final JTextField txtNombre = Estilo.campo();
    private final JTextField txtLicencia = Estilo.campo();
    private final JTextField txtCategoria = Estilo.campo();
    private final JComboBox<Object> cmbVehiculo = new JComboBox<>();
    private final BotonRedondeado btnRegistrar = Estilo.botonPrimario("Registrar conductor");
    private final BotonRedondeado btnAsociar = Estilo.botonSecundario("Asociar vehiculo");
    private final BotonRedondeado btnNuevo = Estilo.botonSecundario("Nuevo / limpiar");
    private int idSeleccionado = 0;

    public FrmConductores(ServiceFactory gestor) {
        super("Gestion de conductores", 1240, 720);
        this.gestor = gestor;
        cmbVehiculo.setFont(Tema.NORMAL);

        Formulario f = new Formulario();
        f.agregar("DNI (8 digitos)", txtDni);
        f.agregar("Nombre completo", txtNombre);
        f.agregar("Licencia", txtLicencia);
        f.agregar("Categoria", txtCategoria);
        f.agregar("Vehiculo habitual", cmbVehiculo);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnRegistrar);
        botones.add(btnAsociar);
        botones.add(btnNuevo);

        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);
        barra.add(Estilo.etiqueta("Buscar (nombre, DNI o licencia):"), BorderLayout.WEST);
        barra.add(txtBuscar, BorderLayout.CENTER);

        montar("Datos del conductor", f, botones, barra, tabla);

        btnRegistrar.addActionListener(e -> registrar());
        btnAsociar.addActionListener(e -> asociar());
        btnNuevo.addActionListener(e -> limpiar());
        Estilo.alEscribir(txtBuscar, this::refrescar);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });
        cargarVehiculos();
        limpiar();
        refrescar();
    }

    /** Solo vehiculos no inactivos (el gestor rechaza los inactivos). */
    private void cargarVehiculos() {
        try {
            cmbVehiculo.removeAllItems();
            cmbVehiculo.addItem(SIN_VEHICULO);
            for (Vehiculo v : gestor.getVehiculos().listar()) {
                if (v.getEstado() != EstadoVehiculo.INACTIVO) {
                    cmbVehiculo.addItem(v);
                }
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private int idVehiculoElegido() {
        Object o = cmbVehiculo.getSelectedItem();
        return o instanceof Vehiculo ? ((Vehiculo) o).getId() : 0;
    }

    private void refrescar() {
        try {
            String t = txtBuscar.getText().trim();
            List<Conductor> lista = t.isEmpty() ? gestor.getConductores().listar()
                    : gestor.getConductores().listar(t);
            modelo.setRowCount(0);
            for (Conductor c : lista) {
                modelo.addRow(new Object[]{c.getId(), c.getDni(), c.getNombre(), c.getLicencia(),
                    c.getCategoria(), c.getEstado(),
                    c.getVehiculoHabitual() != null ? c.getVehiculoHabitual().toString() : "-"});
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
        idSeleccionado = (int) modelo.getValueAt(fila, 0);
        txtDni.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        txtNombre.setText(String.valueOf(modelo.getValueAt(fila, 2)));
        txtLicencia.setText(String.valueOf(modelo.getValueAt(fila, 3)));
        txtCategoria.setText(String.valueOf(modelo.getValueAt(fila, 4)));
        String actual = String.valueOf(modelo.getValueAt(fila, 6));
        cmbVehiculo.setSelectedItem(SIN_VEHICULO);
        for (int i = 0; i < cmbVehiculo.getItemCount(); i++) {
            if (cmbVehiculo.getItemAt(i).toString().equals(actual)) {
                cmbVehiculo.setSelectedIndex(i);
                break;
            }
        }
        modoEdicion(true);
    }

    private void limpiar() {
        idSeleccionado = 0;
        tabla.clearSelection();
        for (JTextField t : new JTextField[]{txtDni, txtNombre, txtLicencia, txtCategoria}) {
            t.setText("");
        }
        cargarVehiculos();
        modoEdicion(false);
    }

    /** Un conductor registrado solo permite cambiar su vehiculo habitual. */
    private void modoEdicion(boolean edicion) {
        txtDni.setEnabled(!edicion);
        txtNombre.setEnabled(!edicion);
        txtLicencia.setEnabled(!edicion);
        txtCategoria.setEnabled(!edicion);
        btnRegistrar.setEnabled(!edicion);
        btnAsociar.setEnabled(edicion);
    }

    private void registrar() {
        try {
            gestor.getConductores().registrar(txtDni.getText(), txtNombre.getText(), txtLicencia.getText(),
                    txtCategoria.getText(), idVehiculoElegido());
            info("Conductor registrado correctamente.");
            limpiar();
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void asociar() {
        int idVehiculo = idVehiculoElegido();
        if (idVehiculo == 0) {
            error("Selecciona un vehiculo para asociar.");
            return;
        }
        try {
            gestor.getConductores().asociarVehiculo(idSeleccionado, idVehiculo);
            info("Vehiculo asociado al conductor.");
            limpiar();
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }
}
