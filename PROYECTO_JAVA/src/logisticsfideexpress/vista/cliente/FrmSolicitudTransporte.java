package logisticsfideexpress.vista.cliente;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import logisticsfideexpress.controlador.GestorLogistico;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmBase;
import logisticsfideexpress.vista.componentes.PanelTarjeta;

/** RF21: el cliente solicita un servicio de transporte. Queda PENDIENTE hasta que el administrador lo revise. */
public class FrmSolicitudTransporte extends FrmBase {

    private final GestorLogistico gestor;
    private final JTextField txtOrigen = Estilo.campo();
    private final JTextField txtDestino = Estilo.campo();
    private final JTextField txtDescripcion = Estilo.campo();
    private final JTextField txtPeso = Estilo.campo();
    private final JTextField txtVolumen = Estilo.campo();
    private final BotonRedondeado btnEnviar = Estilo.botonPrimario("Enviar solicitud");
    private final BotonRedondeado btnCerrar = Estilo.botonSecundario("Cerrar");

    public FrmSolicitudTransporte(GestorLogistico gestor) {
        super("Solicitar transporte", 520, 640);
        this.gestor = gestor;

        Formulario f = new Formulario();
        f.agregar("Origen (direccion de recojo)", txtOrigen);
        f.agregar("Destino (direccion de entrega)", txtDestino);
        f.agregar("Descripcion de la carga", txtDescripcion);
        f.agregar("Peso aproximado (kg)", txtPeso);
        f.agregar("Volumen en m3 (opcional)", txtVolumen);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        botones.add(btnCerrar);
        botones.add(btnEnviar);
        f.agregarSolo(botones, 18);

        PanelTarjeta tarjeta = new PanelTarjeta();
        tarjeta.setLayout(new BorderLayout(0, 6));
        tarjeta.add(Estilo.suave("Tu solicitud sera revisada por el administrador, que te indicara la tarifa."),
                BorderLayout.NORTH);
        tarjeta.add(f, BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane(tarjeta);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        getContenido().add(sp, BorderLayout.CENTER);

        btnEnviar.addActionListener(e -> enviar());
        btnCerrar.addActionListener(e -> dispose());
    }

    private void enviar() {
        try {
            ServicioTransporte s = gestor.getServicios().solicitar(txtOrigen.getText(), txtDestino.getText(),
                    txtDescripcion.getText(), txtPeso.getText(), txtVolumen.getText());
            info("Solicitud N. " + s.getId() + " registrada. Estado: PENDIENTE de aprobacion.");
            for (JTextField t : new JTextField[]{txtOrigen, txtDestino, txtDescripcion, txtPeso, txtVolumen}) {
                t.setText("");
            }
        } catch (Exception ex) {
            error(ex);
        }
    }
}
