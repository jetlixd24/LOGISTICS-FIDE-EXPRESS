package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.modelo.Cliente;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmCrudBase;

/** RF05, RF06, RF07: registrar, modificar y dar de baja clientes. */
public class FrmClientes extends FrmCrudBase {

    private final ServiceFactory gestor;
    private final DefaultTableModel modelo = modelo("ID", "Nombre", "Documento", "Razon social",
            "Telefono", "Direccion", "Estado");
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtBuscar = Estilo.campo();
    private final JTextField txtNombre = Estilo.campo();
    private final JTextField txtCorreo = Estilo.campo();
    private final JPasswordField txtClave = Estilo.clave();
    private final JTextField txtDocumento = Estilo.campo();
    private final JTextField txtRazon = Estilo.campo();
    private final JTextField txtTelefono = Estilo.campo();
    private final JTextField txtDireccion = Estilo.campo();
    private final BotonRedondeado btnRegistrar = Estilo.botonPrimario("Registrar cliente");
    private final BotonRedondeado btnModificar = Estilo.botonSecundario("Guardar cambios");
    private final BotonRedondeado btnBaja = Estilo.botonPeligro("Dar de baja");
    private final BotonRedondeado btnNuevo = Estilo.botonSecundario("Nuevo / limpiar");
    private int idSeleccionado = 0;

    public FrmClientes(ServiceFactory gestor) {
        super("Gestion de clientes", 1240, 760);
        this.gestor = gestor;

        Formulario f = new Formulario();
        f.agregar("Nombre del contacto", txtNombre);
        f.agregar("Correo", txtCorreo);
        f.agregar("Contrasena (solo al registrar)", txtClave);
        f.agregar("Documento (DNI o RUC)", txtDocumento);
        f.agregar("Razon social", txtRazon);
        f.agregar("Telefono", txtTelefono);
        f.agregar("Direccion", txtDireccion);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnRegistrar);
        botones.add(btnModificar);
        botones.add(btnBaja);
        botones.add(btnNuevo);

        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);
        barra.add(Estilo.etiqueta("Buscar (nombre, razon social o documento):"), BorderLayout.WEST);
        barra.add(txtBuscar, BorderLayout.CENTER);

        montar("Datos del cliente", f, botones, barra, tabla);

        btnRegistrar.addActionListener(e -> registrar());
        btnModificar.addActionListener(e -> modificar());
        btnBaja.addActionListener(e -> darDeBaja());
        btnNuevo.addActionListener(e -> limpiar());
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
            String t = txtBuscar.getText();
            List<Cliente> lista = t.trim().isEmpty() ? gestor.getClientes().listar()
                    : gestor.getClientes().listar(t.trim());
            modelo.setRowCount(0);
            for (Cliente c : lista) {
                modelo.addRow(new Object[]{c.getId(), c.getNombre(), c.getDocumento(), c.getRazonSocial(),
                    c.getTelefono(), c.getDireccion(), c.getEstado()});
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
        txtNombre.setText(String.valueOf(modelo.getValueAt(fila, 1)));
        txtDocumento.setText(String.valueOf(modelo.getValueAt(fila, 2)));
        txtRazon.setText(String.valueOf(modelo.getValueAt(fila, 3)));
        txtTelefono.setText(String.valueOf(modelo.getValueAt(fila, 4)));
        txtDireccion.setText(String.valueOf(modelo.getValueAt(fila, 5)));
        txtCorreo.setText("");
        txtClave.setText("");
        modoEdicion(true);
    }

    private void limpiar() {
        idSeleccionado = 0;
        tabla.clearSelection();
        for (JTextField t : new JTextField[]{txtNombre, txtCorreo, txtClave, txtDocumento, txtRazon,
            txtTelefono, txtDireccion}) {
            t.setText("");
        }
        modoEdicion(false);
    }

    /** En edicion no se cambian el correo ni la clave; en registro no se puede modificar ni dar de baja. */
    private void modoEdicion(boolean edicion) {
        txtCorreo.setEnabled(!edicion);
        txtClave.setEnabled(!edicion);
        btnRegistrar.setEnabled(!edicion);
        btnModificar.setEnabled(edicion);
        btnBaja.setEnabled(edicion);
    }

    private void registrar() {
        try {
            gestor.getClientes().registrar(txtNombre.getText(), txtCorreo.getText(),
                    new String(txtClave.getPassword()), txtDocumento.getText(), txtRazon.getText(),
                    txtTelefono.getText(), txtDireccion.getText());
            info("Cliente registrado correctamente.");
            limpiar();
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void modificar() {
        try {
            gestor.getClientes().modificar(idSeleccionado, txtNombre.getText(), txtDocumento.getText(),
                    txtRazon.getText(), txtTelefono.getText(), txtDireccion.getText());
            info("Datos del cliente actualizados.");
            limpiar();
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void darDeBaja() {
        if (!confirmar("Dar de baja al cliente " + txtNombre.getText() + "?")) {
            return;
        }
        try {
            gestor.getClientes().darDeBaja(idSeleccionado);
            info("Cliente dado de baja.");
            limpiar();
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }
}
