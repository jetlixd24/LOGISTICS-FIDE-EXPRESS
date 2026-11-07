package logisticsfideexpress.vista.cliente;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import logisticsfideexpress.controlador.GestorLogistico;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.modelo.Cliente;
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.modelo.enums.EstadoFactura;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.MenuLateral;
import logisticsfideexpress.vista.componentes.Navegador;
import logisticsfideexpress.vista.componentes.PanelTarjeta;
import logisticsfideexpress.vista.componentes.Tema;

/** RF03, RF04, RF06, RF07: menu del cliente y edicion de sus propios datos. */
public class FrmMenuCliente extends JFrame {

    private static final String PAQ = "cliente";
    private static final String INICIO = "inicio";
    private static final String DATOS = "datos";

    private interface Conteo {
        int obtener() throws Exception;
    }

    private final GestorLogistico gestor;
    private final CardLayout cartas = new CardLayout();
    private final JPanel zona = new JPanel(cartas);

    private final JLabel lblSolicitudes = cifra();
    private final JLabel lblFacturas = cifra();
    private final JLabel lblPagos = cifra();

    private final JTextField txtNombre = Estilo.campoLectura("");
    private final JTextField txtCorreo = Estilo.campoLectura("");
    private final JTextField txtDocumento = Estilo.campoLectura("");
    private final JTextField txtRazon = Estilo.campoLectura("");
    private final JTextField txtTelefono = Estilo.campo();
    private final JTextField txtDireccion = Estilo.campo();
    private final JPasswordField txtClave = Estilo.clave();
    private final BotonRedondeado btnGuardar = Estilo.botonPrimario("Guardar cambios");

    public FrmMenuCliente(GestorLogistico gestor) {
        if (!Sesion.esCliente()) {
            throw new IllegalStateException("Acceso solo para clientes.");
        }
        this.gestor = gestor;
        setTitle("LOGISTICS FIDE EXPRESS - Panel del cliente");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(940, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        zona.setBackground(Tema.FONDO);
        zona.add(crearInicio(), INICIO);
        zona.add(crearDatos(), DATOS);
        add(crearMenu(), BorderLayout.WEST);
        add(zona, BorderLayout.CENTER);

        btnGuardar.addActionListener(e -> guardarDatos());
        actualizarCifras();
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

    // ---------- menu ----------
    private MenuLateral crearMenu() {
        MenuLateral m = new MenuLateral("LOGISTICS", "FIDE EXPRESS  |  Cliente");
        m.seccion("Mi cuenta");
        m.item("Inicio", () -> cartas.show(zona, INICIO));
        m.item("Mis datos", () -> {
            cargarDatos();
            cartas.show(zona, DATOS);
        });
        m.seccion("Transporte");
        m.item("Solicitar transporte", () -> abrir("FrmSolicitudTransporte"));
        m.item("Mis solicitudes", () -> abrir("FrmMisSolicitudes"));
        m.item("Seguimiento de mi carga", () -> abrir("FrmSeguimientoCliente"));
        m.seccion("Facturacion");
        m.item("Mis facturas", () -> abrir("FrmMisFacturas"));
        m.item("Mis pagos", () -> abrir("FrmMisPagos"));

        JLabel nombre = new JLabel(Sesion.getUsuario().getNombre());
        nombre.setFont(Tema.NEGRITA);
        nombre.setForeground(java.awt.Color.WHITE);
        JLabel correo = new JLabel(Sesion.getUsuario().getCorreo());
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

    // ---------- inicio ----------
    private JPanel crearInicio() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel cab = new JPanel();
        cab.setOpaque(false);
        cab.setLayout(new BoxLayout(cab, BoxLayout.Y_AXIS));
        JLabel t = Estilo.titulo("Bienvenido, " + Sesion.getUsuario().getNombre());
        t.setAlignmentX(LEFT_ALIGNMENT);
        JLabel s = Estilo.suave("Solicita transporte y consulta el estado de tus cargas, facturas y pagos.");
        s.setAlignmentX(LEFT_ALIGNMENT);
        cab.add(t);
        cab.add(s);
        cab.add(Box.createVerticalStrut(22));
        p.add(cab, BorderLayout.NORTH);

        JPanel rejilla = new JPanel(new GridLayout(1, 3, 18, 18));
        rejilla.setOpaque(false);
        rejilla.add(tarjeta("Mis solicitudes", lblSolicitudes, "FrmMisSolicitudes"));
        rejilla.add(tarjeta("Facturas con saldo", lblFacturas, "FrmMisFacturas"));
        rejilla.add(tarjeta("Pagos realizados", lblPagos, "FrmMisPagos"));

        JPanel centro = new JPanel(new BorderLayout(0, 18));
        centro.setOpaque(false);
        centro.add(rejilla, BorderLayout.NORTH);

        PanelTarjeta accion = new PanelTarjeta();
        accion.setLayout(new BorderLayout());
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(Estilo.subtitulo("Necesitas enviar una carga?"));
        textos.add(Estilo.suave("Indica origen, destino y datos de la carga; te confirmaremos la solicitud."));
        JPanel boton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        boton.setOpaque(false);
        BotonRedondeado nueva = Estilo.botonPrimario("Solicitar transporte");
        nueva.addActionListener(e -> abrir("FrmSolicitudTransporte"));
        boton.add(nueva);
        accion.add(textos, BorderLayout.CENTER);
        accion.add(boton, BorderLayout.EAST);
        JPanel segunda = new JPanel(new BorderLayout());
        segunda.setOpaque(false);
        segunda.add(accion, BorderLayout.NORTH);
        centro.add(segunda, BorderLayout.CENTER);
        p.add(centro, BorderLayout.CENTER);
        return p;
    }

    private PanelTarjeta tarjeta(String titulo, JLabel cifra, String modulo) {
        PanelTarjeta c = new PanelTarjeta();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        c.setPreferredSize(new Dimension(240, 150));
        JLabel l = Estilo.etiqueta(titulo);
        l.setAlignmentX(LEFT_ALIGNMENT);
        cifra.setAlignmentX(LEFT_ALIGNMENT);
        JLabel ir = new JLabel("Ver detalle  >");
        ir.setFont(Tema.NEGRITA);
        ir.setForeground(Tema.NARANJA);
        ir.setAlignmentX(LEFT_ALIGNMENT);
        c.add(l);
        c.add(Box.createVerticalStrut(6));
        c.add(cifra);
        c.add(Box.createVerticalStrut(10));
        c.add(ir);
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrir(modulo);
            }
        });
        return c;
    }

    // ---------- mis datos (RF06, RF07) ----------
    private JPanel crearDatos() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Tema.FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel cab = new JPanel();
        cab.setOpaque(false);
        cab.setLayout(new BoxLayout(cab, BoxLayout.Y_AXIS));
        JLabel t = Estilo.titulo("Mis datos");
        t.setAlignmentX(LEFT_ALIGNMENT);
        JLabel s = Estilo.suave("Puedes actualizar tu telefono, direccion y contrasena.");
        s.setAlignmentX(LEFT_ALIGNMENT);
        cab.add(t);
        cab.add(s);
        cab.add(Box.createVerticalStrut(16));
        p.add(cab, BorderLayout.NORTH);

        Formulario f = new Formulario();
        f.agregar("Nombre", txtNombre);
        f.agregar("Correo", txtCorreo);
        f.agregar("Documento", txtDocumento);
        f.agregar("Razon social", txtRazon);
        f.agregar("Telefono", txtTelefono);
        f.agregar("Direccion", txtDireccion);
        f.agregar("Nueva contrasena (opcional)", txtClave);
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        fila.setOpaque(false);
        fila.add(btnGuardar);
        f.agregarSolo(fila, 16);

        PanelTarjeta tarjeta = new PanelTarjeta();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.add(f, BorderLayout.CENTER);
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(tarjeta, BorderLayout.CENTER);
        envoltura.setPreferredSize(new Dimension(520, 0));
        JPanel izq = new JPanel(new BorderLayout());
        izq.setOpaque(false);
        izq.add(envoltura, BorderLayout.WEST);
        p.add(izq, BorderLayout.CENTER);
        return p;
    }

    private void cargarDatos() {
        try {
            Cliente c = gestor.getClientes().obtenerMisDatos();
            txtNombre.setText(c.getNombre());
            txtCorreo.setText(c.getCorreo());
            txtDocumento.setText(c.getDocumento());
            txtRazon.setText(c.getRazonSocial());
            txtTelefono.setText(c.getTelefono());
            txtDireccion.setText(c.getDireccion());
            txtClave.setText("");
        } catch (OperacionNoPermitidaException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarDatos() {
        try {
            gestor.getClientes().actualizarMisDatos(txtTelefono.getText(), txtDireccion.getText(),
                    new String(txtClave.getPassword()));
            txtClave.setText("");
            javax.swing.JOptionPane.showMessageDialog(this, "Datos actualizados.", "Informacion",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (ValidacionException | OperacionNoPermitidaException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- cifras ----------
    private void actualizarCifras() {
        poner(lblSolicitudes, () -> gestor.getServicios().misSolicitudes().size());
        poner(lblFacturas, () -> {
            int n = 0;
            for (Factura f : gestor.getFacturacion().misFacturas()) {
                if (f.getEstado() != EstadoFactura.PAGADA) {
                    n++;
                }
            }
            return n;
        });
        poner(lblPagos, () -> gestor.getFacturacion().misPagos().size());
    }

    private void poner(JLabel etiqueta, Conteo conteo) {
        try {
            etiqueta.setText(String.valueOf(conteo.obtener()));
        } catch (Exception ex) {
            etiqueta.setText("-");
        }
    }
}
