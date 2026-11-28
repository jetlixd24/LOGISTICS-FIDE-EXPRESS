package logisticsfideexpress.vista.login;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.vista.administrativa.FrmMenuAdministrador;
import logisticsfideexpress.vista.cliente.FrmMenuCliente;
import logisticsfideexpress.vista.cliente.FrmRegistroCliente;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.Tema;

/** RF01, RF03: inicio de sesion; abre el menu segun el rol. */
public class FrmLogin extends JFrame {

    /** Ponlo en false para ocultar la ayuda con el usuario de prueba. */
    private static final boolean MOSTRAR_AYUDA = false;

    private final ServiceFactory gestor;
    private final JTextField txtCorreo = Estilo.campo();
    private final JPasswordField txtClave = Estilo.clave();
    private final JLabel lblMensaje = new JLabel(" ");
    private final BotonRedondeado btnIngresar = Estilo.botonPrimario("Ingresar");

    public FrmLogin(ServiceFactory gestor) {
        this.gestor = gestor;
        setTitle("LOGISTICS FIDE EXPRESS - Inicio de sesion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 520);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2));
        add(crearPanelMarca());
        add(crearPanelFormulario());
        configurarEventos();
    }

    // ---------- diseno ----------
    private JPanel crearPanelMarca() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Tema.AZUL, 0, getHeight(), Tema.AZUL_OSCURO));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // "carretera" naranja diagonal, inspirada en el logo
                int w = getWidth();
                int h = getHeight();
                g2.setColor(Tema.NARANJA);
                g2.fillPolygon(new int[]{0, w, w, 0}, new int[]{h - 80, h - 135, h - 108, h - 22}, 4);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillPolygon(new int[]{0, w, w, 0}, new int[]{h - 22, h - 108, h - 92, h - 8}, 4);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(120, 40, 40, 30));

        JLabel t1 = new JLabel("LOGISTICS");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 38));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("FIDE EXPRESS");
        t2.setFont(new Font("Segoe UI", Font.BOLD, 38));
        t2.setForeground(Tema.NARANJA);
        JLabel t3 = new JLabel("Operador logistico a nivel nacional");
        t3.setFont(Tema.NORMAL);
        t3.setForeground(new Color(201, 211, 227));

        p.add(t1);
        p.add(t2);
        p.add(javax.swing.Box.createVerticalStrut(14));
        p.add(t3);
        return p;
    }

    private JPanel crearPanelFormulario() {
        JPanel p = new JPanel();
        p.setBackground(Tema.FONDO);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(80, 50, 30, 50));

        JLabel h = Estilo.titulo("Iniciar sesion");
        h.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub = Estilo.suave("Ingresa con tu correo y contrasena");
        sub.setAlignmentX(LEFT_ALIGNMENT);
        p.add(h);
        p.add(sub);
        p.add(javax.swing.Box.createVerticalStrut(14));

        Formulario f = new Formulario();
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.agregar("Correo", txtCorreo);
        f.agregar("Contrasena", txtClave);
        p.add(f);

        JCheckBox chk = new JCheckBox("Mostrar contrasena");
        chk.setOpaque(false);
        chk.setFont(Tema.PEQUENA);
        chk.setForeground(Tema.TEXTO_SUAVE);
        chk.setAlignmentX(LEFT_ALIGNMENT);
        chk.addActionListener(e -> txtClave.setEchoChar(chk.isSelected() ? (char) 0 : '\u2022'));
        p.add(chk);

        lblMensaje.setFont(Tema.PEQUENA);
        lblMensaje.setForeground(Tema.ROJO);
        lblMensaje.setAlignmentX(LEFT_ALIGNMENT);
        lblMensaje.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        p.add(lblMensaje);

        btnIngresar.setAlignmentX(LEFT_ALIGNMENT);
        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.add(btnIngresar);
        p.add(javax.swing.Box.createVerticalStrut(14));

        JLabel enlace = new JLabel("Crear cuenta de cliente");
        enlace.setFont(Tema.NEGRITA);
        enlace.setForeground(Tema.AZUL);
        enlace.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        enlace.setAlignmentX(LEFT_ALIGNMENT);
        enlace.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                enlace.setForeground(Tema.NARANJA);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                enlace.setForeground(Tema.AZUL);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                new FrmRegistroCliente(gestor).setVisible(true);
            }
        });
        p.add(enlace);

        if (MOSTRAR_AYUDA) {
            p.add(javax.swing.Box.createVerticalStrut(18));
            JLabel ayuda = Estilo.suave("Prueba: admin@fide.com / admin123");
            ayuda.setAlignmentX(LEFT_ALIGNMENT);
            p.add(ayuda);
        }
        return p;
    }

    // ---------- eventos ----------
    private void configurarEventos() {
        btnIngresar.addActionListener(e -> ingresar());
        // Enter en cualquiera de los dos campos = Ingresar
        KeyAdapter enter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ingresar();
                }
            }
        };
        txtCorreo.addKeyListener(enter);
        txtClave.addKeyListener(enter);
    }

    private void ingresar() {
        try {
            gestor.getUsuarios().login(txtCorreo.getText(), new String(txtClave.getPassword()));
            dispose();
            if (Sesion.esAdministrador()) {
                new FrmMenuAdministrador(gestor).setVisible(true);
            } else {
                new FrmMenuCliente(gestor).setVisible(true);
            }
        } catch (ValidacionException | OperacionNoPermitidaException ex) {
            lblMensaje.setText(ex.getMessage());
            txtClave.setText("");
        }
    }
}
