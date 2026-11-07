package logisticsfideexpress.vista.cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.controlador.GestorLogistico;
import logisticsfideexpress.modelo.SeguimientoCarga;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.FrmBase;
import logisticsfideexpress.vista.componentes.Opcion;
import logisticsfideexpress.vista.componentes.PanelTarjeta;
import logisticsfideexpress.vista.componentes.Tema;

/** RF27: el cliente sigue la ubicacion y el estado de su carga. */
public class FrmSeguimientoCliente extends FrmBase {

    private final GestorLogistico gestor;
    private final JComboBox<Opcion<ServicioTransporte>> cmbServicio = new JComboBox<>();
    private final JLabel lblEstado = new JLabel("-");
    private final JLabel lblRuta = Estilo.etiqueta(" ");
    private final JLabel lblUltima = Estilo.etiqueta(" ");
    private final JLabel lblEntrega = Estilo.etiqueta(" ");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Fecha y hora", "Estado", "Ubicacion", "Observacion"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final BotonRedondeado btnActualizar = Estilo.botonSecundario("Actualizar");
    private boolean cargando = false;

    public FrmSeguimientoCliente(GestorLogistico gestor) {
        super("Seguimiento de mi carga", 1050, 680);
        this.gestor = gestor;
        cmbServicio.setFont(Tema.NORMAL);
        lblEstado.setFont(Tema.TITULO);

        JPanel selector = new JPanel(new BorderLayout(10, 0));
        selector.setOpaque(false);
        selector.add(Estilo.etiqueta("Servicio:"), BorderLayout.WEST);
        selector.add(cmbServicio, BorderLayout.CENTER);
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        der.setOpaque(false);
        der.add(btnActualizar);
        selector.add(der, BorderLayout.EAST);

        PanelTarjeta resumen = new PanelTarjeta();
        resumen.setLayout(new BoxLayout(resumen, BoxLayout.Y_AXIS));
        JLabel t = Estilo.suave("Estado actual");
        for (JLabel l : new JLabel[]{t, lblEstado, lblRuta, lblUltima, lblEntrega}) {
            l.setAlignmentX(LEFT_ALIGNMENT);
            resumen.add(l);
        }

        JPanel arriba = new JPanel(new BorderLayout(0, 12));
        arriba.setOpaque(false);
        arriba.add(selector, BorderLayout.NORTH);
        arriba.add(resumen, BorderLayout.CENTER);

        JPanel c = getContenido();
        c.setLayout(new BorderLayout(0, 14));
        c.add(arriba, BorderLayout.NORTH);
        JPanel abajo = new JPanel(new BorderLayout(0, 8));
        abajo.setOpaque(false);
        abajo.add(Estilo.subtitulo("Historial del recorrido"), BorderLayout.NORTH);
        abajo.add(Estilo.tabla(tabla), BorderLayout.CENTER);
        c.add(abajo, BorderLayout.CENTER);

        cmbServicio.addActionListener(e -> {
            if (!cargando) {
                mostrar();
            }
        });
        btnActualizar.addActionListener(e -> cargarServicios());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                cargarServicios();
            }
        });
        cargarServicios();
    }

    private void cargarServicios() {
        cargando = true;
        try {
            @SuppressWarnings("unchecked")
            Opcion<ServicioTransporte> previo = (Opcion<ServicioTransporte>) cmbServicio.getSelectedItem();
            int idPrevio = previo != null ? previo.getValor().getId() : -1;
            cmbServicio.removeAllItems();
            for (ServicioTransporte s : gestor.getServicios().misSolicitudes()) {
                Opcion<ServicioTransporte> o = new Opcion<>(s, "N. " + s.getId() + " - " + s.getOrigen()
                        + " -> " + s.getDestino() + " (" + s.getCarga().getDescripcion() + ")");
                cmbServicio.addItem(o);
                if (s.getId() == idPrevio) {
                    cmbServicio.setSelectedItem(o);
                }
            }
        } catch (Exception ex) {
            error(ex);
        } finally {
            cargando = false;
        }
        mostrar();
    }

    private void mostrar() {
        modelo.setRowCount(0);
        @SuppressWarnings("unchecked")
        Opcion<ServicioTransporte> o = (Opcion<ServicioTransporte>) cmbServicio.getSelectedItem();
        if (o == null) {
            lblEstado.setText("Sin solicitudes");
            lblEstado.setForeground(Tema.TEXTO_SUAVE);
            lblRuta.setText("Aun no has registrado solicitudes de transporte.");
            lblUltima.setText(" ");
            lblEntrega.setText(" ");
            return;
        }
        try {
            ServicioTransporte s = gestor.getServicios().obtener(o.getValor().getId());
            lblEstado.setText(s.getEstado().toString());
            lblEstado.setForeground(colorEstado(s.getEstado()));
            lblRuta.setText("Ruta: " + s.getOrigen() + " -> " + s.getDestino());
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            SeguimientoCarga ultimo = gestor.getSeguimiento().ultimoAvance(s.getId());
            lblUltima.setText(ultimo == null ? "Sin movimientos registrados."
                    : "Ultima ubicacion: " + ultimo.getUbicacion() + "  (" + ultimo.getFechaHora().format(fmt) + ")");
            if (s.getEstado() == EstadoServicio.ENTREGADO) {
                lblEntrega.setText("Entregado a " + s.getReceptorEntrega()
                        + (s.getFechaEntrega() != null ? " el " + s.getFechaEntrega() : ""));
            } else if (s.getEstado() == EstadoServicio.RECHAZADO) {
                lblEntrega.setText("Motivo del rechazo: " + s.getMotivoRechazo());
            } else {
                lblEntrega.setText(" ");
            }
            for (SeguimientoCarga g : gestor.getSeguimiento().consultar(s.getId())) {
                modelo.addRow(new Object[]{g.getFechaHora().format(fmt), g.getEstado(), g.getUbicacion(),
                    g.getObservacion()});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private Color colorEstado(EstadoServicio e) {
        switch (e) {
            case ENTREGADO:
                return Tema.VERDE;
            case RECHAZADO:
                return Tema.ROJO;
            case PENDIENTE:
                return Tema.TEXTO_SUAVE;
            default:
                return Tema.NARANJA;
        }
    }
}
