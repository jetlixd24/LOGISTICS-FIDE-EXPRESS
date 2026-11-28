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
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formato;
import logisticsfideexpress.vista.componentes.FrmBase;
import logisticsfideexpress.vista.componentes.Tema;

/** RF36: el cliente consulta sus facturas, su saldo y su estado. */
public class FrmMisFacturas extends FrmBase {

    private final ServiceFactory gestor;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Numero", "Fecha", "Total", "Pagado", "Saldo", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel lblSaldo = new JLabel(" ");
    private final BotonRedondeado btnActualizar = Estilo.botonSecundario("Actualizar");

    public FrmMisFacturas(ServiceFactory gestor) {
        super("Mis facturas", 1000, 560);
        this.gestor = gestor;
        lblSaldo.setFont(Tema.SUBTITULO);
        lblSaldo.setForeground(Tema.AZUL);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(lblSaldo, BorderLayout.CENTER);
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
            double saldo = 0;
            for (Factura f : gestor.getFacturacion().misFacturas()) {
                modelo.addRow(new Object[]{f.getNumero(), f.getFecha().format(Formato.FECHA),
                    Formato.moneda(f.getTotal()), Formato.moneda(f.getTotal() - f.getSaldo()),
                    Formato.moneda(f.getSaldo()), f.getEstado()});
                saldo += f.getSaldo();
            }
            lblSaldo.setText("Saldo total pendiente: " + Formato.moneda(saldo));
        } catch (Exception ex) {
            error(ex);
        }
    }
}
