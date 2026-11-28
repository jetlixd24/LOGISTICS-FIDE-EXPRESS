/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import logisticsfideexpress.dao.interfaz.IProveedorDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.Proveedor;

/**
 * Acceso a MySQL para la tabla proveedor. Los proveedores no se dan de baja:
 * si ya tienen productos o facturas, se perdería el historial.
 */
public class ProveedorDAO implements IProveedorDAO {

    private static final String SELECT_BASE
            = "SELECT id_proveedor, ruc, razon_social, telefono FROM proveedor ";

    private Proveedor mapear(ResultSet rs) throws SQLException {
        return new Proveedor(rs.getInt("id_proveedor"), rs.getString("ruc"),
                rs.getString("razon_social"), rs.getString("telefono"));
    }

    @Override
    public void guardar(Proveedor p) {
        String sql = "INSERT INTO proveedor (ruc, razon_social, telefono) VALUES (?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getRuc());
            ps.setString(2, p.getRazonSocial());
            ps.setString(3, p.getTelefono());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AccesoDatosException("El RUC del proveedor ya esta registrado.", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el proveedor. Verifique la conexión con MySQL.", e);
        }
    }

    @Override
    public Proveedor buscarPorId(int id) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE id_proveedor = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el proveedor.", e);
        }
    }

    @Override
    public List<Proveedor> listar() {
        List<Proveedor> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY razon_social");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo listar los proveedores. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    @Override
    public void actualizar(Proveedor p) {
        String sql = "UPDATE proveedor SET ruc = ?, razon_social = ?, telefono = ? WHERE id_proveedor = ?";
        try (Connection cn = ConexionBD.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getRuc());
            ps.setString(2, p.getRazonSocial());
            ps.setString(3, p.getTelefono());
            ps.setInt(4, p.getId());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AccesoDatosException("El RUC del proveedor ya esta registrado.", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar el proveedor.", e);
        }
    }

    @Override
    public void darDeBaja(int id) {
        throw new UnsupportedOperationException("Los proveedores no se dan de baja (se conserva el historial).");
    }

    @Override
    public int siguienteId() {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement("SELECT COALESCE(MAX(id_proveedor), 0) + 1 FROM proveedor");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener el siguiente id.", e);
        }
    }

    @Override
    public Proveedor buscarPorRuc(String ruc) {
        if (ruc == null) {
            return null;
        }
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE ruc = ?")) {
            ps.setString(1, ruc.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el proveedor.", e);
        }
    }

    // Búsqueda por texto (razón social o RUC), filtrada en SQL
    @Override
    public List<Proveedor> buscar(String texto) {
        String t = "%" + (texto == null ? "" : texto.trim()) + "%";
        List<Proveedor> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE
                        + "WHERE razon_social LIKE ? OR ruc LIKE ? ORDER BY razon_social")) {
            ps.setString(1, t);
            ps.setString(2, t);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo buscar proveedores.", e);
        }
        return lista;
    }
}