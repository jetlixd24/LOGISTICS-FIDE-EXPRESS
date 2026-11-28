package logisticsfideexpress.vista.cliente;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.modelo.Pago;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formato;
import logisticsfideexpress.vista.componentes.FrmBase;
import logisticsfideexpress.vista.componentes.Tema;

/** RF37: el cliente consulta los pagos que ha realizado. */
public class FrmMisPagos extends FrmBase {

    private final ServiceFactory gestor;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"N. pago", "Fecha", "Factura", "Monto", "Metodo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblTotal = new JLabel(" ");
    private final BotonRedondeado btnActualizar = Estilo.botonSecundario("Actualizar");

    public FrmMisPagos(ServiceFactory gestor) {
        super("Mis pagos", 900, 540);
        this.gestor = gestor;
        lblTotal.setFont(Tema.SUBTITULO);
        lblTotal.setForeground(Tema.AZUL);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(lblTotal, BorderLayout.CENTER);
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
            modelo.setRowCount(0);
            double total = 0;
            for (Pago p : gestor.getFacturacion().misPagos()) {
                modelo.addRow(new Object[]{p.getId(), p.getFecha().format(Formato.FECHA),
                    p.getFactura().getNumero(), Formato.moneda(p.getMonto()), p.getMetodo()});
                total += p.getMonto();
            }
            lblTotal.setText("Total pagado: " + Formato.moneda(total));
        } catch (Exception ex) {
            error(ex);
        }
    }
}
