package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import logisticsfideexpress.servicio.ServiceFactory;
import logisticsfideexpress.modelo.MovimientoAlmacen;
import logisticsfideexpress.modelo.Producto;
import logisticsfideexpress.modelo.Proveedor;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmCrudBase;
import logisticsfideexpress.vista.componentes.Tema;

/** RF16 a RF20: productos, proveedores, entradas y salidas de almacen y alerta de stock minimo. */
public class FrmAlmacen extends FrmCrudBase {

    private static final String SIN_PROVEEDOR = "(Sin proveedor)";
    private static final String TODOS = "Todos los productos";
    private static final String BAJO = "BAJO MINIMO";

    private final ServiceFactory gestor;

    // ----- productos
    private final DefaultTableModel modeloProd = modelo("ID", "Codigo", "Nombre", "Unidad", "Stock",
            "Minimo", "Proveedor", "Estado");
    private final JTable tablaProd = new JTable(modeloProd) {
        @Override
        public Component prepareRenderer(TableCellRenderer r, int fila, int col) {
            Component c = super.prepareRenderer(r, fila, col);
            if (!isRowSelected(fila)) {
                c.setForeground(BAJO.equals(getValueAt(fila, 7)) ? Tema.ROJO : Tema.TEXTO);
            }
            return c;
        }
    };
    private final JTextField txtBuscarProd = Estilo.campo();
    private final JTextField txtCodigo = Estilo.campo();
    private final JTextField txtNombre = Estilo.campo();
    private final JTextField txtUnidad = Estilo.campo();
    private final JTextField txtStockIni = Estilo.campo();
    private final JTextField txtStockMin = Estilo.campo();
    private final JComboBox<Object> cmbProveedor = new JComboBox<>();
    private final BotonRedondeado btnRegProd = Estilo.botonPrimario("Registrar producto");
    private final BotonRedondeado btnModProd = Estilo.botonSecundario("Guardar cambios");
    private final BotonRedondeado btnNuevoProd = Estilo.botonSecundario("Nuevo / limpiar");
    private final JLabel lblAlerta = new JLabel(" ");
    private int idProducto = 0;

    // ----- movimientos
    private final DefaultTableModel modeloMov = modelo("ID", "Fecha", "Producto", "Tipo", "Cantidad");
    private final JTable tablaMov = new JTable(modeloMov);
    private final JComboBox<Producto> cmbProductoMov = new JComboBox<>();
    private final JComboBox<Object> cmbFiltroMov = new JComboBox<>();
    private final JTextField txtCantidad = Estilo.campo();
    private final JLabel lblStock = Estilo.suave(" ");
    private final BotonRedondeado btnEntrada = Estilo.botonPrimario("Registrar entrada");
    private final BotonRedondeado btnSalida = Estilo.botonSecundario("Registrar salida");
    private boolean cargando = false;

    // ----- proveedores
    private final DefaultTableModel modeloProv = modelo("ID", "RUC", "Razon social", "Telefono");
    private final JTable tablaProv = new JTable(modeloProv);
    private final JTextField txtBuscarProv = Estilo.campo();
    private final JTextField txtRuc = Estilo.campo();
    private final JTextField txtRazon = Estilo.campo();
    private final JTextField txtTelefono = Estilo.campo();
    private final BotonRedondeado btnRegProv = Estilo.botonPrimario("Registrar proveedor");

    public FrmAlmacen(ServiceFactory gestor) {
        super("Almacen", 1280, 780);
        this.gestor = gestor;
        cmbProveedor.setFont(Tema.NORMAL);
        cmbProductoMov.setFont(Tema.NORMAL);
        cmbFiltroMov.setFont(Tema.NORMAL);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Tema.NEGRITA);
        tabs.addTab("Productos", crearTabProductos());
        tabs.addTab("Entradas y salidas", crearTabMovimientos());
        tabs.addTab("Proveedores", crearTabProveedores());
        getContenido().add(tabs, BorderLayout.CENTER);

        cargarCombos();
        refrescarProductos();
        refrescarMovimientos();
        refrescarProveedores();
        limpiarProducto();
    }

    // =====================================================================
    // Pestana 1: productos
    // =====================================================================
    private JPanel crearTabProductos() {
        Formulario f = new Formulario();
        f.agregar("Codigo", txtCodigo);
        f.agregar("Nombre", txtNombre);
        f.agregar("Unidad (ej. caja, litro, unidad)", txtUnidad);
        f.agregar("Stock inicial (solo al registrar)", txtStockIni);
        f.agregar("Stock minimo", txtStockMin);
        f.agregar("Proveedor", cmbProveedor);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnRegProd);
        botones.add(btnModProd);
        botones.add(btnNuevoProd);

        lblAlerta.setFont(Tema.NEGRITA);
        JPanel barra = new JPanel(new BorderLayout(0, 6));
        barra.setOpaque(false);
        barra.add(barraBusqueda("Buscar (codigo o nombre):", txtBuscarProd), BorderLayout.NORTH);
        barra.add(lblAlerta, BorderLayout.SOUTH);

        JPanel p = armar("Datos del producto", f, botones, barra, tablaProd);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 0, 0, 0));

        btnRegProd.addActionListener(e -> registrarProducto());
        btnModProd.addActionListener(e -> modificarProducto());
        btnNuevoProd.addActionListener(e -> limpiarProducto());
        Estilo.alEscribir(txtBuscarProd, this::refrescarProductos);
        tablaProd.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarProducto();
            }
        });
        return p;
    }

    private void refrescarProductos() {
        try {
            String t = txtBuscarProd.getText().trim();
            List<Producto> lista = t.isEmpty() ? gestor.getAlmacen().listarProductos()
                    : gestor.getAlmacen().listarProductos(t);
            modeloProd.setRowCount(0);
            for (Producto p : lista) {
                modeloProd.addRow(new Object[]{p.getId(), p.getCodigo(), p.getNombre(), p.getUnidad(),
                    p.getStock(), p.getStockMinimo(),
                    p.getProveedor() != null ? p.getProveedor().getRazonSocial() : "-",
                    p.estaBajoMinimo() ? BAJO : "Normal"});
            }
            int bajos = gestor.getAlmacen().productosBajoMinimo().size();
            if (bajos > 0) {
                lblAlerta.setText("ALERTA: " + bajos + " producto(s) por debajo del stock minimo (en rojo).");
                lblAlerta.setForeground(Tema.ROJO);
            } else {
                lblAlerta.setText("Todo el stock esta sobre el minimo.");
                lblAlerta.setForeground(Tema.VERDE);
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void seleccionarProducto() {
        int fila = tablaProd.getSelectedRow();
        if (fila < 0) {
            return;
        }
        try {
            idProducto = (int) modeloProd.getValueAt(fila, 0);
            Producto p = gestor.getAlmacen().buscarProducto(idProducto);
            txtCodigo.setText(p.getCodigo());
            txtNombre.setText(p.getNombre());
            txtUnidad.setText(p.getUnidad());
            txtStockIni.setText(String.valueOf(p.getStock()));
            txtStockMin.setText(String.valueOf(p.getStockMinimo()));
            cmbProveedor.setSelectedItem(SIN_PROVEEDOR);
            if (p.getProveedor() != null) {
                for (int i = 0; i < cmbProveedor.getItemCount(); i++) {
                    Object o = cmbProveedor.getItemAt(i);
                    if (o instanceof Proveedor && ((Proveedor) o).getId() == p.getProveedor().getId()) {
                        cmbProveedor.setSelectedIndex(i);
                    }
                }
            }
            modoEdicionProducto(true);
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void limpiarProducto() {
        idProducto = 0;
        tablaProd.clearSelection();
        for (JTextField t : new JTextField[]{txtCodigo, txtNombre, txtUnidad, txtStockIni, txtStockMin}) {
            t.setText("");
        }
        cmbProveedor.setSelectedItem(SIN_PROVEEDOR);
        modoEdicionProducto(false);
    }

    /** El codigo y el stock no se editan a mano: el stock cambia solo con entradas y salidas. */
    private void modoEdicionProducto(boolean edicion) {
        txtCodigo.setEnabled(!edicion);
        txtStockIni.setEnabled(!edicion);
        btnRegProd.setEnabled(!edicion);
        btnModProd.setEnabled(edicion);
    }

    private int idProveedorElegido() {
        Object o = cmbProveedor.getSelectedItem();
        return o instanceof Proveedor ? ((Proveedor) o).getId() : 0;
    }

    private void registrarProducto() {
        try {
            gestor.getAlmacen().registrarProducto(txtCodigo.getText(), txtNombre.getText(), txtUnidad.getText(),
                    txtStockIni.getText(), txtStockMin.getText(), idProveedorElegido());
            info("Producto registrado correctamente.");
            limpiarProducto();
            cargarCombos();
            refrescarProductos();
            refrescarMovimientos();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void modificarProducto() {
        try {
            gestor.getAlmacen().modificarProducto(idProducto, txtNombre.getText(), txtUnidad.getText(),
                    txtStockMin.getText(), idProveedorElegido());
            info("Producto actualizado.");
            limpiarProducto();
            cargarCombos();
            refrescarProductos();
        } catch (Exception ex) {
            error(ex);
        }
    }

    // =====================================================================
    // Pestana 2: entradas y salidas
    // =====================================================================
    private JPanel crearTabMovimientos() {
        Formulario f = new Formulario();
        f.agregar("Producto", cmbProductoMov);
        f.agregarSolo(lblStock, 0);
        f.agregar("Cantidad", txtCantidad);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnEntrada);
        botones.add(btnSalida);

        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);
        barra.add(Estilo.etiqueta("Historial de:"), BorderLayout.WEST);
        barra.add(cmbFiltroMov, BorderLayout.CENTER);

        JPanel p = armar("Movimiento de stock", f, botones, barra, tablaMov);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 0, 0, 0));

        btnEntrada.addActionListener(e -> mover(true));
        btnSalida.addActionListener(e -> mover(false));
        cmbProductoMov.addActionListener(e -> mostrarStock());
        cmbFiltroMov.addActionListener(e -> {
            if (!cargando) {
                refrescarMovimientos();
            }
        });
        return p;
    }

    private void mostrarStock() {
        Producto p = (Producto) cmbProductoMov.getSelectedItem();
        lblStock.setText(p == null ? " " : "Stock actual: " + p.getStock() + " " + p.getUnidad()
                + "  |  Minimo: " + p.getStockMinimo());
    }

    private void refrescarMovimientos() {
        if (cargando) {
            return;
        }
        try {
            Object filtro = cmbFiltroMov.getSelectedItem();
            List<MovimientoAlmacen> lista = filtro instanceof Producto
                    ? gestor.getAlmacen().listarMovimientos(((Producto) filtro).getId())
                    : gestor.getAlmacen().listarMovimientos();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            modeloMov.setRowCount(0);
            for (MovimientoAlmacen m : lista) {
                modeloMov.addRow(new Object[]{m.getId(), m.getFecha().format(fmt), m.getProducto().getNombre(),
                    m.getTipo(), m.getCantidad()});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void mover(boolean entrada) {
        Producto p = (Producto) cmbProductoMov.getSelectedItem();
        if (p == null) {
            error("Primero registra un producto.");
            return;
        }
        try {
            if (entrada) {
                gestor.getAlmacen().registrarEntrada(p.getId(), txtCantidad.getText());
            } else {
                gestor.getAlmacen().registrarSalida(p.getId(), txtCantidad.getText());
            }
            txtCantidad.setText("");
            cargarCombos();
            refrescarProductos();
            refrescarMovimientos();
            Producto actual = (Producto) cmbProductoMov.getSelectedItem();
            if (actual != null && actual.estaBajoMinimo()) {
                info("Atencion: " + actual.getNombre() + " quedo bajo el stock minimo ("
                        + actual.getStock() + " de " + actual.getStockMinimo() + ").");
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    // =====================================================================
    // Pestana 3: proveedores
    // =====================================================================
    private JPanel crearTabProveedores() {
        Formulario f = new Formulario();
        f.agregar("RUC (11 digitos)", txtRuc);
        f.agregar("Razon social", txtRazon);
        f.agregar("Telefono", txtTelefono);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnRegProv);

        JPanel p = armar("Nuevo proveedor", f, botones,
                barraBusqueda("Buscar (razon social o RUC):", txtBuscarProv), tablaProv);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 0, 0, 0));

        btnRegProv.addActionListener(e -> registrarProveedor());
        Estilo.alEscribir(txtBuscarProv, this::refrescarProveedores);
        return p;
    }

    private void refrescarProveedores() {
        try {
            String t = txtBuscarProv.getText().trim();
            List<Proveedor> lista = t.isEmpty() ? gestor.getAlmacen().listarProveedores()
                    : gestor.getAlmacen().listarProveedores(t);
            modeloProv.setRowCount(0);
            for (Proveedor p : lista) {
                modeloProv.addRow(new Object[]{p.getId(), p.getRuc(), p.getRazonSocial(), p.getTelefono()});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void registrarProveedor() {
        try {
            gestor.getAlmacen().registrarProveedor(txtRuc.getText(), txtRazon.getText(), txtTelefono.getText());
            info("Proveedor registrado correctamente.");
            txtRuc.setText("");
            txtRazon.setText("");
            txtTelefono.setText("");
            cargarCombos();
            refrescarProveedores();
        } catch (Exception ex) {
            error(ex);
        }
    }

    // =====================================================================
    // Combos compartidos
    // =====================================================================
    /** Recarga proveedores y productos en los combos conservando lo que estaba elegido. */
    private void cargarCombos() {
        cargando = true;
        try {
            Object provSel = cmbProveedor.getSelectedItem();
            cmbProveedor.removeAllItems();
            cmbProveedor.addItem(SIN_PROVEEDOR);
            for (Proveedor p : gestor.getAlmacen().listarProveedores()) {
                cmbProveedor.addItem(p);
            }
            if (provSel != null) {
                cmbProveedor.setSelectedItem(provSel);
            }

            Producto movSel = (Producto) cmbProductoMov.getSelectedItem();
            Object filtroSel = cmbFiltroMov.getSelectedItem();
            cmbProductoMov.removeAllItems();
            cmbFiltroMov.removeAllItems();
            cmbFiltroMov.addItem(TODOS);
            for (Producto p : gestor.getAlmacen().listarProductos()) {
                cmbProductoMov.addItem(p);
                cmbFiltroMov.addItem(p);
                if (movSel != null && movSel.getId() == p.getId()) {
                    cmbProductoMov.setSelectedItem(p);
                }
                if (filtroSel instanceof Producto && ((Producto) filtroSel).getId() == p.getId()) {
                    cmbFiltroMov.setSelectedItem(p);
                }
            }
        } catch (Exception ex) {
            error(ex);
        } finally {
            cargando = false;
        }
        mostrarStock();
    }
}
