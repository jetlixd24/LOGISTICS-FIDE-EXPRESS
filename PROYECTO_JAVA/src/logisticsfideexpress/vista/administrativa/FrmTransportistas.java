package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.modelo.Transportista;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmCrudBase;

/** RF08, RF09: registrar transportistas y consultar sus datos y vehiculos afiliados. */
public class FrmTransportistas extends FrmCrudBase {

    private final ServiceFactory gestor;
    private final DefaultTableModel modelo = modelo("ID", "Contacto", "RUC", "Razon social",
            "Telefono", "Correo", "Vehiculos afiliados");
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtBuscar = Estilo.campo();
    private final JTextField txtNombre = Estilo.campo();
    private final JTextField txtCorreo = Estilo.campo();
    private final JTextField txtRuc = Estilo.campo();
    private final JTextField txtRazon = Estilo.campo();
    private final JTextField txtTelefono = Estilo.campo();
    private final BotonRedondeado btnRegistrar = Estilo.botonPrimario("Registrar transportista");
    private final BotonRedondeado btnLimpiar = Estilo.botonSecundario("Limpiar");

    public FrmTransportistas(ServiceFactory gestor) {
        super("Gestion de transportistas", 1240, 700);
        this.gestor = gestor;

        Formulario f = new Formulario();
        f.agregar("Nombre de contacto", txtNombre);
        f.agregar("Correo", txtCorreo);
        f.agregar("RUC (11 digitos)", txtRuc);
        f.agregar("Razon social", txtRazon);
        f.agregar("Telefono", txtTelefono);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnRegistrar);
        botones.add(btnLimpiar);

        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);
        barra.add(Estilo.etiqueta("Buscar (razon social o RUC):"), BorderLayout.WEST);
        barra.add(txtBuscar, BorderLayout.CENTER);

        montar("Nuevo transportista", f, botones, barra, tabla);

        btnRegistrar.addActionListener(e -> registrar());
        btnLimpiar.addActionListener(e -> limpiar());
        Estilo.alEscribir(txtBuscar, this::refrescar);
        refrescar();
    }

    private void refrescar() {
        try {
            String t = txtBuscar.getText();
            List<Transportista> lista = t.trim().isEmpty() ? gestor.getTransportistas().listar()
                    : gestor.getTransportistas().listar(t.trim());
            modelo.setRowCount(0);
            for (Transportista x : lista) {
                modelo.addRow(new Object[]{x.getId(), x.getNombre(), x.getRuc(), x.getRazonSocial(),
                    x.getTelefono(), x.getCorreo(), x.getVehiculos().size()});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void limpiar() {
        for (JTextField t : new JTextField[]{txtNombre, txtCorreo, txtRuc, txtRazon, txtTelefono}) {
            t.setText("");
        }
    }

    private void registrar() {
        try {
            gestor.getTransportistas().registrar(txtNombre.getText(), txtCorreo.getText(),
                    txtRuc.getText(), txtRazon.getText(), txtTelefono.getText());
            info("Transportista registrado correctamente.");
            limpiar();
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }
}
