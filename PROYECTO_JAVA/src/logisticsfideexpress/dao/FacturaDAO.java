/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import logisticsfideexpress.dao.interfaz.IFacturaDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.modelo.enums.EstadoFactura;
import logisticsfideexpress.modelo.enums.TipoFactura;

/**
 * Acceso a MySQL para factura. El tercero (cliente, proveedor o transportista)
 * vive en tres columnas FK y su nombre se obtiene con LEFT JOIN. Las facturas no
 * se dan de baja (histórico contable).
 */
public class FacturaDAO implements IFacturaDAO {

    private static final String SELECT_BASE
            = "SELECT f.id_factura, f.numero, f.tipo, f.fecha, f.id_cliente, f.id_proveedor, "
            + "f.id_transportista, f.total, f.saldo, f.estado, "
            + "COALESCE(c.razon_social, p.razon_social, t.razon_social) AS tercero_nombre "
            + "FROM factura f "
            + "LEFT JOIN cliente c ON c.id_usuario = f.id_cliente "
            + "LEFT JOIN proveedor p ON p.id_proveedor = f.id_proveedor "
            + "LEFT JOIN transportista t ON t.id_usuario = f.id_transportista ";

    private Factura mapear(ResultSet rs) throws SQLException {
        String terceroTipo;
        int terceroId;
        int idCliente = rs.getInt("id_cliente");
        if (!rs.wasNull()) {
            terceroTipo = Factura.TERCERO_CLIENTE;
            terceroId = idCliente;
        } else {
            int idProveedor = rs.getInt("id_proveedor");
            if (!rs.wasNull()) {
                terceroTipo = Factura.TERCERO_PROVEEDOR;
                terceroId = idProveedor;
            } else {
                terceroTipo = Factura.TERCERO_TRANSPORTISTA;
                terceroId = rs.getInt("id_transportista");
            }
        }
        Factura f = new Factura(rs.getInt("id_factura"), rs.getString("numero"),
                TipoFactura.valueOf(rs.getString("tipo")), rs.getDate("fecha").toLocalDate(),
                terceroTipo, terceroId, rs.getString("tercero_nombre"), rs.getDouble("total"));
        f.setSaldo(rs.getDouble("saldo"));
        f.setEstado(EstadoFactura.valueOf(rs.getString("estado")));
        return f;
    }

    private List<Factura> consultar(String condicion, Object... params) {
        List<Factura> lista = new ArrayList<>();
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
            throw new AccesoDatosException("No se pudo consultar las facturas. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    /** Nombre de la columna FK según el tipo de tercero (texto fijo, nunca del usuario). */
    private static String columnaTercero(String terceroTipo) {
        if (Factura.TERCERO_CLIENTE.equals(terceroTipo)) {
            return "f.id_cliente";
        }
        if (Factura.TERCERO_PROVEEDOR.equals(terceroTipo)) {
            return "f.id_proveedor";
        }
        if (Factura.TERCERO_TRANSPORTISTA.equals(terceroTipo)) {
            return "f.id_transportista";
        }
        return null;
    }

    @Override
    public void guardar(Factura f) {
        String sql = "INSERT INTO factura (numero, tipo, fecha, id_cliente, id_proveedor, id_transportista, "
                + "total, saldo, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getNumero());
            ps.setString(2, f.getTipo().name());
            ps.setDate(3, Date.valueOf(f.getFecha()));
            ps.setNull(4, Types.INTEGER);
            ps.setNull(5, Types.INTEGER);
            ps.setNull(6, Types.INTEGER);
            switch (f.getTerceroTipo()) {
                case Factura.TERCERO_CLIENTE ->
                    ps.setInt(4, f.getTerceroId());
                case Factura.TERCERO_PROVEEDOR ->
                    ps.setInt(5, f.getTerceroId());
                default ->
                    ps.setInt(6, f.getTerceroId());
            }
            ps.setDouble(7, f.getTotal());
            ps.setDouble(8, f.getSaldo());
            ps.setString(9, f.getEstado().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    f.setId(rs.getInt(1));
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            String m = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (m.contains("duplicate")) {
                throw new AccesoDatosException("Ya existe una factura con ese numero para ese tercero.", e);
            }
            throw new AccesoDatosException("La factura no cumple las reglas de la base (tercero o importes).", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar la factura. Verifique la conexión con MySQL.", e);
        }
    }

    @Override
    public Factura buscarPorId(int id) {
        List<Factura> r = consultar("WHERE f.id_factura = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Factura> listar() {
        return consultar("ORDER BY f.id_factura");
    }

    /** Solo cambian el saldo y el estado (lo que modifica Factura.registrarPago). */
    @Override
    public void actualizar(Factura f) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "UPDATE factura SET saldo = ?, estado = ? WHERE id_factura = ?")) {
            ps.setDouble(1, f.getSaldo());
            ps.setString(2, f.getEstado().name());
            ps.setInt(3, f.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar la factura.", e);
        }
    }

    @Override
    public void darDeBaja(int id) {
        throw new UnsupportedOperationException("Las facturas no se dan de baja (historico contable).");
    }

    @Override
    public int siguienteId() {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement("SELECT COALESCE(MAX(id_factura), 0) + 1 FROM factura");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener el siguiente id.", e);
        }
    }

    @Override
    public Factura buscarPorNumero(String numero) {
        if (numero == null) {
            return null;
        }
        List<Factura> r = consultar("WHERE f.numero = ? ORDER BY f.id_factura LIMIT 1", numero.trim());
        return r.isEmpty() ? null : r.get(0);
    }

    /** True si ya existe una factura con ese número para ese mismo tercero. */
    @Override
    public boolean existe(String numero, String terceroTipo, int terceroId) {
        String col = columnaTercero(terceroTipo);
        if (numero == null || col == null) {
            return false;
        }
        return !consultar("WHERE f.numero = ? AND " + col + " = ?", numero.trim(), terceroId).isEmpty();
    }

    // Sobrecarga: por tipo (VENTA o COMPRA)
    @Override
    public List<Factura> buscar(TipoFactura tipo) {
        return consultar("WHERE f.tipo = ? ORDER BY f.id_factura", tipo.name());
    }

    // Sobrecarga: por estado
    @Override
    public List<Factura> buscar(EstadoFactura estado) {
        return consultar("WHERE f.estado = ? ORDER BY f.id_factura", estado.name());
    }

    /** Facturas de un tercero (por ejemplo, las facturas de venta de un cliente). */
    @Override
    public List<Factura> buscarPorTercero(String terceroTipo, int terceroId) {
        String col = columnaTercero(terceroTipo);
        if (col == null) {
            return new ArrayList<>();
        }
        return consultar("WHERE " + col + " = ? ORDER BY f.id_factura", terceroId);
    }
}