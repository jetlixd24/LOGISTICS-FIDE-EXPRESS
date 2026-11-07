package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import logisticsfideexpress.controlador.GestorLogistico;
import logisticsfideexpress.modelo.Usuario;
import logisticsfideexpress.modelo.enums.EstadoFactura;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.MenuLateral;
import logisticsfideexpress.vista.componentes.Navegador;
import logisticsfideexpress.vista.componentes.PanelTarjeta;
import logisticsfideexpress.vista.componentes.Tema;

/** RF03, RF04: menu principal del administrador con acceso a todos los modulos. */
public class FrmMenuAdministrador extends JFrame {

    private static final String PAQ = "administrativa";

    private interface Conteo {
        int obtener() throws Exception;
    }

    private final GestorLogistico gestor;
    private final JLabel lblClientes = cifra();
    private final JLabel lblVehiculos = cifra();
    private final JLabel lblPendientes = cifra();
    private final JLabel lblBajoMinimo = cifra();
    private final JLabel lblFacturas = cifra();

    public FrmMenuAdministrador(GestorLogistico gestor) {
        if (!Sesion.esAdministrador()) {
            throw new IllegalStateException("Acceso solo para administradores.");
        }
        this.gestor = gestor;
        setTitle("LOGISTICS FIDE EXPRESS - Panel administrativo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        java.awt.Rectangle pantalla = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(1180, Math.min(780, pantalla.height));
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(crearMenu(), BorderLayout.WEST);
        add(crearPrincipal(), BorderLayout.CENTER);
        actualizarCifras();

        // Al volver a esta ventana se refrescan los numeros (WindowListener)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                actualizarCifras();
            }
        });
    }

    private static JLabel cifra() {
        JLabel l = new JLabel("0");
        l.setFont(Tema.CIFRA);
        l.setForeground(Tema.AZUL);
        return l;
    }

    // ---------- menu lateral ----------
    private MenuLateral crearMenu() {
        MenuLateral m = new MenuLateral("LOGISTICS", "FIDE EXPRESS  |  Administrador");

        m.seccion("Gestion interna");
        m.item("Usuarios", () -> abrir("FrmUsuarios"));
        m.item("Clientes", () -> abrir("FrmClientes"));
        m.item("Transportistas", () -> abrir("FrmTransportistas"));
        m.item("Conductores", () -> abrir("FrmConductores"));
        m.item("Vehiculos", () -> abrir("FrmVehiculos"));
        m.item("Almacen", () -> abrir("FrmAlmacen"));

        m.seccion("Servicios");
        m.item("Solicitudes y servicios", () -> abrir("FrmServicios"));
        m.item("Asignaciones", () -> abrir("FrmAsignaciones"));
        m.item("Seguimiento de cargas", () -> abrir("FrmSeguimiento"));

        m.seccion("Finanzas y reportes");
        m.item("Costos de viaje", () -> abrir("FrmCostos"));
        m.item("Facturas", () -> abrir("FrmFacturas"));
        m.item("Pagos", () -> abrir("FrmPagos"));
        m.item("Liquidaciones", () -> abrir("FrmLiquidaciones"));
        m.item("Reportes", () -> abrir("FrmReportes"));

        Usuario u = Sesion.getUsuario();
        JLabel nombre = new JLabel(u.getNombre());
        nombre.setFont(Tema.NEGRITA);
        nombre.setForeground(java.awt.Color.WHITE);
        JLabel correo = new JLabel(u.getCorreo());
        correo.setFont(Tema.PEQUENA);
        correo.setForeground(new java.awt.Color(170, 185, 210));
        BotonRedondeado salir = Estilo.botonPeligro("Cerrar sesion");
        salir.setAlignmentX(LEFT_ALIGNMENT);
        salir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        salir.addActionListener(e -> Navegador.cerrarSesion(gestor));

        m.getPie().add(nombre);
        m.getPie().add(correo);
        m.espacioPie(10);
        m.getPie().add(salir);
        return m;
    }

    private void abrir(String clase) {
        Navegador.abrir(gestor, PAQ, clase);
    }

    // ---------- area principal ----------
    private JPanel crearPrincipal() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel cab = new JPanel();
        cab.setOpaque(false);
        cab.setLayout(new BoxLayout(cab, BoxLayout.Y_AXIS));
        JLabel t = Estilo.titulo("Panel administrativo");
        t.setAlignmentX(LEFT_ALIGNMENT);
        JLabel s = Estilo.suave("Bienvenido, " + Sesion.getUsuario().getNombre()
                + ". Haz clic en una tarjeta o usa el menu de la izquierda.");
        s.setAlignmentX(LEFT_ALIGNMENT);
        cab.add(t);
        cab.add(s);
        cab.add(Box.createVerticalStrut(22));
        p.add(cab, BorderLayout.NORTH);

        JPanel rejilla = new JPanel(new GridLayout(2, 3, 18, 18));
        rejilla.setOpaque(false);
        rejilla.add(tarjeta("Clientes registrados", lblClientes, "FrmClientes"));
        rejilla.add(tarjeta("Vehiculos en flota", lblVehiculos, "FrmVehiculos"));
        rejilla.add(tarjeta("Solicitudes pendientes", lblPendientes, "FrmServicios"));
        rejilla.add(tarjeta("Productos bajo el minimo", lblBajoMinimo, "FrmAlmacen"));
        rejilla.add(tarjeta("Facturas pendientes", lblFacturas, "FrmFacturas"));
        rejilla.add(tarjetaSesion());
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(rejilla, BorderLayout.NORTH);
        p.add(envoltura, BorderLayout.CENTER);
        return p;
    }

    private PanelTarjeta tarjeta(String titulo, JLabel cifra, String modulo) {
        PanelTarjeta c = new PanelTarjeta();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        c.setPreferredSize(new Dimension(240, 150));
        JLabel l = Estilo.etiqueta(titulo);
        cifra.setAlignmentX(LEFT_ALIGNMENT);
        l.setAlignmentX(LEFT_ALIGNMENT);
        JLabel ir = new JLabel("Abrir modulo  >");
        ir.setFont(Tema.NEGRITA);
        ir.setForeground(Tema.NARANJA);
        ir.setAlignmentX(LEFT_ALIGNMENT);
        c.add(l);
        c.add(Box.createVerticalStrut(6));
        c.add(cifra);
        c.add(Box.createVerticalGlue());
        c.add(ir);
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrir(modulo);
            }
        });
        return c;
    }

    private PanelTarjeta tarjetaSesion() {
        PanelTarjeta c = new PanelTarjeta();
        c.setPreferredSize(new Dimension(240, 150));
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        Usuario u = Sesion.getUsuario();
        JLabel a = Estilo.etiqueta("Sesion activa");
        JLabel b = Estilo.subtitulo(u.getNombre());
        JLabel d = Estilo.suave(u.getCorreo());
        JLabel e = Estilo.suave("Rol: " + u.getTipo());
        for (JLabel l : new JLabel[]{a, b, d, e}) {
            l.setAlignmentX(LEFT_ALIGNMENT);
        }
        c.add(a);
        c.add(Box.createVerticalStrut(10));
        c.add(b);
        c.add(d);
        c.add(Box.createVerticalStrut(6));
        c.add(e);
        return c;
    }

    // ---------- datos ----------
    private void actualizarCifras() {
        poner(lblClientes, () -> gestor.getClientes().listar().size());
        poner(lblVehiculos, () -> gestor.getVehiculos().listar().size());
        poner(lblPendientes, () -> gestor.getServicios().listar(EstadoServicio.PENDIENTE).size());
        poner(lblBajoMinimo, () -> gestor.getAlmacen().productosBajoMinimo().size());
        poner(lblFacturas, () -> gestor.getFacturacion().listarFacturas(EstadoFactura.PENDIENTE).size());
    }

    private void poner(JLabel etiqueta, Conteo conteo) {
        try {
            etiqueta.setText(String.valueOf(conteo.obtener()));
        } catch (Exception ex) {
            etiqueta.setText("-");
        }
    }
}
