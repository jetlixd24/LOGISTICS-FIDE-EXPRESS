package logisticsfideexpress.vista.cliente;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.excepciones.RegistroDuplicadoException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.FrmBase;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.PanelTarjeta;

/** RF02 (CU21): el cliente crea su propia cuenta. */
public class FrmRegistroCliente extends FrmBase {

    private final ServiceFactory gestor;
    private final JTextField txtNombre = Estilo.campo();
    private final JTextField txtCorreo = Estilo.campo();
    private final JPasswordField txtClave = Estilo.clave();
    private final JPasswordField txtClave2 = Estilo.clave();
    private final JTextField txtDocumento = Estilo.campo();
    private final JTextField txtRazon = Estilo.campo();
    private final JTextField txtTelefono = Estilo.campo();
    private final JTextField txtDireccion = Estilo.campo();
    private final BotonRedondeado btnCrear = Estilo.botonPrimario("Crear cuenta");
    private final BotonRedondeado btnCancelar = Estilo.botonSecundario("Cancelar");

    public FrmRegistroCliente(ServiceFactory gestor) {
        super("Registro de cliente", 520, 740);
        this.gestor = gestor;

        Formulario f = new Formulario();
        f.agregar("Nombre del contacto", txtNombre);
        f.agregar("Correo", txtCorreo);
        f.agregar("Contrasena (minimo 6 caracteres)", txtClave);
        f.agregar("Repetir contrasena", txtClave2);
        f.agregar("Documento (DNI o RUC)", txtDocumento);
        f.agregar("Razon social", txtRazon);
        f.agregar("Telefono", txtTelefono);
        f.agregar("Direccion", txtDireccion);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnCancelar);
        botones.add(btnCrear);
        f.agregarSolo(botones, 16);

        PanelTarjeta tarjeta = new PanelTarjeta();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.add(f, BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane(tarjeta);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        getContenido().add(sp, BorderLayout.CENTER);

        btnCrear.addActionListener(e -> crear());
        btnCancelar.addActionListener(e -> dispose());
    }

    private void crear() {
        String c1 = new String(txtClave.getPassword());
        String c2 = new String(txtClave2.getPassword());
        if (!c1.equals(c2)) {
            error("Las contrasenas no coinciden.");
            return;
        }
        try {
            gestor.getUsuarios().registrarCliente(txtNombre.getText(), txtCorreo.getText(), c1,
                    txtDocumento.getText(), txtRazon.getText(), txtTelefono.getText(), txtDireccion.getText());
            info("Cuenta creada. Ya puedes iniciar sesion.");
            dispose();
        } catch (ValidacionException | RegistroDuplicadoException ex) {
            error(ex);
        }
    }
}
