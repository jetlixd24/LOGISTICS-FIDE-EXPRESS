package logisticsfideexpress.vista.cliente;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.FrmBase;

/** RF25: el cliente consulta el estado de sus solicitudes. */
public class FrmMisSolicitudes extends FrmBase {

    private final ServiceFactory gestor;
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"N.", "Fecha", "Origen", "Destino",
        "Carga", "Peso (kg)", "Estado", "Tarifa", "Detalle"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final BotonRedondeado btnActualizar = Estilo.botonSecundario("Actualizar");

    public FrmMisSolicitudes(ServiceFactory gestor) {
        super("Mis solicitudes", 1180, 600);
        this.gestor = gestor;

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(Estilo.suave("Aqui ves si tu solicitud esta pendiente, aprobada, rechazada, en ruta o entregada."),
                BorderLayout.CENTER);
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        der.setOpaque(false);
        der.add(btnActualizar);
        barra.add(der, BorderLayout.EAST);

        JPanel c = getContenido();
        c.setLayout(new BorderLayout(0, 12));
        c.add(barra, BorderLayout.NORTH);
        c.add(Estilo.tabla(tabla), BorderLayout.CENTER);

        btnActualizar.addActionListener(e -> refrescar());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                refrescar();
            }
        });
        refrescar();
    }

    private void refrescar() {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            modelo.setRowCount(0);
            for (ServicioTransporte s : gestor.getServicios().misSolicitudes()) {
                modelo.addRow(new Object[]{s.getId(), s.getFechaSolicitud().format(fmt), s.getOrigen(),
                    s.getDestino(), s.getCarga().getDescripcion(), s.getCarga().getPesoKg(), s.getEstado(),
                    s.getTarifa() > 0 ? String.valueOf(s.getTarifa()) : "-", detalle(s)});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private String detalle(ServicioTransporte s) {
        if (s.getEstado() == EstadoServicio.RECHAZADO && s.getMotivoRechazo() != null) {
            return "Rechazada: " + s.getMotivoRechazo();
        }
        if (s.getEstado() == EstadoServicio.ENTREGADO && s.getReceptorEntrega() != null) {
            return "Recibido por " + s.getReceptorEntrega()
                    + (s.getFechaEntrega() != null ? " el " + s.getFechaEntrega().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        }
        return "";
    }
}
