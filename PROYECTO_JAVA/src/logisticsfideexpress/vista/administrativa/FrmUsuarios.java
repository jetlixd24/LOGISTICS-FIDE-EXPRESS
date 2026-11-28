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
import logisticsfideexpress.modelo.Usuario;
import logisticsfideexpress.modelo.enums.EstadoUsuario;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmCrudBase;

/** RF02, RF03: usuarios del sistema. Crea administradores y activa o desactiva cuentas. */
public class FrmUsuarios extends FrmCrudBase {

    private final ServiceFactory gestor;
    private final DefaultTableModel modelo = modelo("ID", "Nombre", "Correo", "Rol", "Estado");
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtBuscar = Estilo.campo();
    private final JTextField txtNombre = Estilo.campo();
    private final JTextField txtCorreo = Estilo.campo();
    private final JPasswordField txtClave = Estilo.clave();
    private final JTextField txtCargo = Estilo.campo();
    private final BotonRedondeado btnCrear = Estilo.botonPrimario("Crear administrador");
    private final BotonRedondeado btnActivar = Estilo.botonSecundario("Activar usuario");
    private final BotonRedondeado btnDesactivar = Estilo.botonPeligro("Desactivar usuario");

    public FrmUsuarios(ServiceFactory gestor) {
        super("Usuarios del sistema", 1180, 700);
        this.gestor = gestor;

        Formulario f = new Formulario();
        f.agregar("Nombre", txtNombre);
        f.agregar("Correo", txtCorreo);
        f.agregar("Contrasena (minimo 6 caracteres)", txtClave);
        f.agregar("Cargo", txtCargo);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnCrear);
        botones.add(btnActivar);
        botones.add(btnDesactivar);

        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);
        barra.add(Estilo.etiqueta("Buscar:"), BorderLayout.WEST);
        barra.add(txtBuscar, BorderLayout.CENTER);

        montar("Nuevo administrador", f, botones, barra, tabla);

        btnCrear.addActionListener(e -> crear());
        btnActivar.addActionListener(e -> cambiarEstado(EstadoUsuario.ACTIVO));
        btnDesactivar.addActionListener(e -> cambiarEstado(EstadoUsuario.INACTIVO));
        Estilo.alEscribir(txtBuscar, this::refrescar);
        refrescar();
    }

    private void refrescar() {
        try {
            String t = txtBuscar.getText();
            List<Usuario> lista = t.trim().isEmpty() ? gestor.getUsuarios().listar()
                    : gestor.getUsuarios().listar(t.trim());
            modelo.setRowCount(0);
            for (Usuario u : lista) {
                modelo.addRow(new Object[]{u.getId(), u.getNombre(), u.getCorreo(), u.getTipo(), u.getEstado()});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void crear() {
        try {
            gestor.getUsuarios().crearAdministrador(txtNombre.getText(), txtCorreo.getText(),
                    new String(txtClave.getPassword()), txtCargo.getText());
            info("Administrador creado correctamente.");
            txtNombre.setText("");
            txtCorreo.setText("");
            txtClave.setText("");
            txtCargo.setText("");
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void cambiarEstado(EstadoUsuario nuevo) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            error("Selecciona un usuario de la tabla.");
            return;
        }
        int id = (int) modelo.getValueAt(fila, 0);
        String nombre = String.valueOf(modelo.getValueAt(fila, 1));
        if (!confirmar("Cambiar el estado de " + nombre + " a " + nuevo + "?")) {
            return;
        }
        try {
            gestor.getUsuarios().cambiarEstado(id, nuevo);
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }
}
