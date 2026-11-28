/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import logisticsfideexpress.dao.interfaz.IProductoDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logisticsfideexpress.dao.interfaz.IProveedorDAO;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.MovimientoAlmacen;
import logisticsfideexpress.modelo.Producto;
import logisticsfideexpress.modelo.Proveedor;
import logisticsfideexpress.modelo.enums.TipoMovimiento;

/**
 * Acceso a MySQL para producto y movimiento_almacen (relación 1 a muchos).
 * Los productos no se dan de baja y los movimientos son históricos: solo se
 * agregan, nunca se borran.
 */
public class ProductoDAO implements IProductoDAO {

    private static final String SELECT_BASE
            = "SELECT p.id_producto, p.codigo, p.nombre, p.unidad, p.stock, p.stock_minimo, p.id_proveedor, "
            + "pr.ruc, pr.razon_social, pr.telefono "
            + "FROM producto p "
            + "LEFT JOIN proveedor pr ON pr.id_proveedor = p.id_proveedor ";

    // Se conserva el parámetro del constructor para no tocar ServiceFactory; el JOIN ya trae al proveedor.
    private final IProveedorDAO proveedores;

    public ProductoDAO(IProveedorDAO proveedores) {
        this.proveedores = proveedores;
    }

    // ---------------------------------------------------------------- productos
    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto(rs.getInt("id_producto"), rs.getString("codigo"),
                rs.getString("nombre"), rs.getString("unidad"),
                rs.getInt("stock"), rs.getInt("stock_minimo"));
        int idProveedor = rs.getInt("id_proveedor");
        if (!rs.wasNull()) {
            p.setProveedor(new Proveedor(idProveedor, rs.getString("ruc"),
                    rs.getString("razon_social"), rs.getString("telefono")));
        }
        return p;
    }

    private List<Producto> consultar(String condicion, Object... params) {
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + condicion)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar los productos. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    private AccesoDatosException traducir(SQLIntegrityConstraintViolationException e) {
        String m = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (m.contains("duplicate")) {
            return new AccesoDatosException("El codigo del producto ya esta registrado.", e);
        }
        return new AccesoDatosException("El producto no cumple las reglas de la base (stock o proveedor).", e);
    }

    @Override
    public void guardar(Producto p) {
        String sql = "INSERT INTO producto (codigo, nombre, unidad, stock, stock_minimo, id_proveedor) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getUnidad());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getStockMinimo());
            if (p.getProveedor() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, p.getProveedor().getId());
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw traducir(e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el producto. Verifique la conexión con MySQL.", e);
        }
    }

    @Override
    public Producto buscarPorId(int id) {
        List<Producto> r = consultar("WHERE p.id_producto = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Producto> listar() {
        return consultar("ORDER BY p.nombre");
    }

    @Override
    public void actualizar(Producto p) {
        String sql = "UPDATE producto SET codigo = ?, nombre = ?, unidad = ?, stock = ?, stock_minimo = ?, "
                + "id_proveedor = ? WHERE id_producto = ?";
        try (Connection cn = ConexionBD.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getUnidad());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getStockMinimo());
            if (p.getProveedor() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, p.getProveedor().getId());
            }
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw traducir(e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar el producto.", e);
        }
    }

    @Override
    public void darDeBaja(int id) {
        throw new UnsupportedOperationException("Los productos no se dan de baja (se conserva el historial).");
    }

    @Override
    public int siguienteId() {
        return siguiente("SELECT COALESCE(MAX(id_producto), 0) + 1 FROM producto");
    }

    @Override
    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        List<Producto> r = consultar("WHERE p.codigo = ?", codigo.trim());
        return r.isEmpty() ? null : r.get(0);
    }

    // Sobrecarga: por texto (código o nombre), filtrada en SQL
    @Override
    public List<Producto> buscar(String texto) {
        String t = "%" + (texto == null ? "" : texto.trim()) + "%";
        return consultar("WHERE p.codigo LIKE ? OR p.nombre LIKE ? ORDER BY p.nombre", t, t);
    }

    /** Productos cuyo stock llegó o bajó del mínimo. */
    @Override
    public List<Producto> buscarBajoMinimo() {
        return consultar("WHERE p.stock <= p.stock_minimo ORDER BY p.nombre");
    }

    // -------------------------------------------------------------- movimientos
    private List<MovimientoAlmacen> consultarMovimientos(String condicion, Object... params) {
        List<MovimientoAlmacen> lista = new ArrayList<>();
        Map<Integer, Producto> cache = new HashMap<>();
        String sql = "SELECT id_movimiento, id_producto, fecha, tipo, cantidad FROM movimiento_almacen "
                + condicion;
        try (Connection cn = ConexionBD.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idProducto = rs.getInt("id_producto");
                    Producto p = cache.get(idProducto);
                    if (p == null) {
                        p = buscarPorId(idProducto);
                        cache.put(idProducto, p);
                    }
                    if (p == null) {
                        continue;
                    }
                    lista.add(new MovimientoAlmacen(rs.getInt("id_movimiento"), p,
                            rs.getDate("fecha").toLocalDate(),
                            TipoMovimiento.valueOf(rs.getString("tipo")), rs.getInt("cantidad")));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar los movimientos. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    @Override
    public int siguienteIdMovimiento() {
        return siguiente("SELECT COALESCE(MAX(id_movimiento), 0) + 1 FROM movimiento_almacen");
    }

    @Override
    public void guardarMovimiento(MovimientoAlmacen m) {
        String sql = "INSERT INTO movimiento_almacen (id_producto, fecha, tipo, cantidad) VALUES (?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getProducto().getId());
            ps.setDate(2, Date.valueOf(m.getFecha()));
            ps.setString(3, m.getTipo().name());
            ps.setInt(4, m.getCantidad());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    m.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el movimiento. Verifique la conexión con MySQL.", e);
        }
    }

    @Override
    public List<MovimientoAlmacen> listarMovimientos() {
        return consultarMovimientos("ORDER BY id_movimiento");
    }

    // Sobrecarga: movimientos de un producto
    @Override
    public List<MovimientoAlmacen> listarMovimientos(int idProducto) {
        return consultarMovimientos("WHERE id_producto = ? ORDER BY id_movimiento", idProducto);
    }

    private int siguiente(String sql) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener el siguiente id.", e);
        }
    }
}