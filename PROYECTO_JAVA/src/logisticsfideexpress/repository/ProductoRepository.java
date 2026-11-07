/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.repository;

/**
 *
 * @author JETLI
 */
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.modelo.MovimientoAlmacen;
import logisticsfideexpress.modelo.Producto;
import logisticsfideexpress.modelo.enums.TipoMovimiento;
import logisticsfideexpress.util.ArchivoUtil;

/**
 * Guarda dos archivos (Producto y MovimientoAlmacen estan relacionados 1 a muchos):
 *  data/productos.txt    -> id;codigo;nombre;unidad;stock;stockMinimo;idProveedor (0 = ninguno)
 *  data/movimientos.txt  -> id;idProducto;fecha;tipo;cantidad
 * Los movimientos son historicos: solo se agregan, nunca se borran.
 */
public class ProductoRepository extends RepositorioArchivo<Producto> {

    private static final String RUTA = "data/productos.txt";
    private static final String RUTA_MOVIMIENTOS = "data/movimientos.txt";

    private final ProveedorRepository proveedores;
    private final List<MovimientoAlmacen> movimientos = new ArrayList<>();

    public ProductoRepository(ProveedorRepository proveedores) {
        super(RUTA);
        this.proveedores = proveedores;
        cargar();
        cargarMovimientos();
    }

    // ---------------------------------------------------------------- productos

    @Override
    protected String aLinea(Producto p) {
        return String.join(ArchivoUtil.SEPARADOR,
                String.valueOf(p.getId()),
                ArchivoUtil.limpiar(p.getCodigo()),
                ArchivoUtil.limpiar(p.getNombre()),
                ArchivoUtil.limpiar(p.getUnidad()),
                String.valueOf(p.getStock()),
                String.valueOf(p.getStockMinimo()),
                String.valueOf(p.getProveedor() == null ? 0 : p.getProveedor().getId()));
    }

    @Override
    protected Producto deLinea(String[] c) {
        Producto p = new Producto(Integer.parseInt(c[0]), c[1], c[2], c[3],
                Integer.parseInt(c[4]), Integer.parseInt(c[5]));
        int idProveedor = Integer.parseInt(c[6]);
        if (idProveedor > 0) {
            p.setProveedor(proveedores.buscarPorId(idProveedor));
        }
        return p;
    }

    @Override
    protected int idDe(Producto p) { return p.getId(); }

    @Override
    protected void fijarId(Producto p, int id) { p.setId(id); }

    @Override
    protected void marcarBaja(Producto p) {
        throw new UnsupportedOperationException("Los productos no se dan de baja (se conserva el historial).");
    }

    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (Producto p : items) {
            if (p.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return p;
            }
        }
        return null;
    }

    // Sobrecarga: por texto (codigo o nombre)
    public List<Producto> buscar(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : items) {
            if (p.getCodigo().toLowerCase().contains(t) || p.getNombre().toLowerCase().contains(t)) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /** Productos cuyo stock llego o bajo del minimo. */
    public List<Producto> buscarBajoMinimo() {
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : items) {
            if (p.estaBajoMinimo()) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    // -------------------------------------------------------------- movimientos

    private void cargarMovimientos() {
        movimientos.clear();
        for (String linea : ArchivoUtil.leerLineas(RUTA_MOVIMIENTOS)) {
            try {
                String[] c = ArchivoUtil.partir(linea);
                Producto p = buscarPorId(Integer.parseInt(c[1]));
                if (p == null) {
                    throw new IllegalStateException("producto inexistente");
                }
                movimientos.add(new MovimientoAlmacen(Integer.parseInt(c[0]), p, LocalDate.parse(c[2]),
                        TipoMovimiento.valueOf(c[3]), Integer.parseInt(c[4])));
            } catch (RuntimeException e) {
                System.err.println("Linea ignorada en " + RUTA_MOVIMIENTOS + ": " + linea + " (" + e.getMessage() + ")");
            }
        }
    }

    private void persistirMovimientos() {
        List<String> lineas = new ArrayList<>();
        for (MovimientoAlmacen m : movimientos) {
            lineas.add(String.join(ArchivoUtil.SEPARADOR,
                    String.valueOf(m.getId()),
                    String.valueOf(m.getProducto().getId()),
                    m.getFecha().toString(),
                    m.getTipo().name(),
                    String.valueOf(m.getCantidad())));
        }
        ArchivoUtil.escribirLineas(RUTA_MOVIMIENTOS, lineas);
    }

    public int siguienteIdMovimiento() {
        int max = 0;
        for (MovimientoAlmacen m : movimientos) {
            max = Math.max(max, m.getId());
        }
        return max + 1;
    }

    public void guardarMovimiento(MovimientoAlmacen m) {
        if (m.getId() <= 0) {
            m.setId(siguienteIdMovimiento());
        }
        movimientos.add(m);
        persistirMovimientos();
    }

    public List<MovimientoAlmacen> listarMovimientos() {
        return new ArrayList<>(movimientos);
    }

    // Sobrecarga: movimientos de un producto
    public List<MovimientoAlmacen> listarMovimientos(int idProducto) {
        List<MovimientoAlmacen> resultado = new ArrayList<>();
        for (MovimientoAlmacen m : movimientos) {
            if (m.getProducto().getId() == idProducto) {
                resultado.add(m);
            }
        }
        return resultado;
    }
}
