/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

import logisticsfideexpress.dao.interfaz.IVehiculoDAO;
/**
 *
 * @author JETLI
 */
import logisticsfideexpress.dao.interfaz.IConductorDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.Conductor;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.enums.EstadoConductor;

/**
 * Acceso a MySQL para la tabla conductor. El vehículo habitual se resuelve con
 * VehiculoDAO (puede ser NULL).
 */
public class ConductorDAO implements IConductorDAO{

    private static final String SELECT_BASE
            = "SELECT id_conductor, dni, nombre, licencia, categoria, estado, id_vehiculo_habitual "
            + "FROM conductor ";

    private final IVehiculoDAO vehiculos;

    public ConductorDAO(IVehiculoDAO vehiculos) {
        this.vehiculos = vehiculos;
    }

    private Conductor mapear(ResultSet rs) throws SQLException {
        Conductor c = new Conductor(rs.getInt("id_conductor"), rs.getString("dni"),
                rs.getString("nombre"), rs.getString("licencia"), rs.getString("categoria"));
        c.setEstado(EstadoConductor.valueOf(rs.getString("estado")));
        int idVeh = rs.getInt("id_vehiculo_habitual");
        if (!rs.wasNull()) {
            c.setVehiculoHabitual(vehiculos.buscarPorId(idVeh));
        }
        return c;
    }

    private AccesoDatosException duplicado(SQLIntegrityConstraintViolationException e) {
        String m = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (m.contains("dni")) {
            return new AccesoDatosException("El DNI ya esta registrado.", e);
        }
        if (m.contains("licencia")) {
            return new AccesoDatosException("La licencia ya esta registrada.", e);
        }
        if (m.contains("id_vehiculo_habitual")) {
            return new AccesoDatosException("El vehiculo ya esta asociado a otro conductor.", e);
        }
        return new AccesoDatosException("Ya existe un conductor con esos datos.", e);
    }

    @Override
    public void guardar(Conductor c) {
        String sql = "INSERT INTO conductor (dni, nombre, licencia, categoria, estado, id_vehiculo_habitual) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getLicencia());
            ps.setString(4, c.getCategoria());
            ps.setString(5, c.getEstado().name());
            if (c.getVehiculoHabitual() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, c.getVehiculoHabitual().getId());
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw duplicado(e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el conductor. Verifique la conexión con MySQL.", e);
        }
    }

    @Override
    public Conductor buscarPorId(int id) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE id_conductor = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el conductor.", e);
        }
    }

    @Override
    public List<Conductor> listar() {
        List<Conductor> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo listar los conductores. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    @Override
    public void actualizar(Conductor c) {
        String sql = "UPDATE conductor SET dni = ?, nombre = ?, licencia = ?, categoria = ?, estado = ?, "
                + "id_vehiculo_habitual = ? WHERE id_conductor = ?";
        try (Connection cn = ConexionBD.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getDni());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getLicencia());
            ps.setString(4, c.getCategoria());
            ps.setString(5, c.getEstado().name());
            if (c.getVehiculoHabitual() == null) {
                ps.setNull(6, Types.INTEGER);
            } else {
                ps.setInt(6, c.getVehiculoHabitual().getId());
            }
            ps.setInt(7, c.getId());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw duplicado(e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar el conductor.", e);
        }
    }

    @Override
    public void darDeBaja(int id) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "UPDATE conductor SET estado = 'INACTIVO' WHERE id_conductor = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo dar de baja al conductor.", e);
        }
    }

    @Override
    public int siguienteId() {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement("SELECT COALESCE(MAX(id_conductor), 0) + 1 FROM conductor");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener el siguiente id.", e);
        }
    }

    @Override
    public Conductor buscarPorLicencia(String licencia) {
        if (licencia == null) {
            return null;
        }
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE licencia = ?")) {
            ps.setString(1, licencia.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el conductor.", e);
        }
    }

    /** Conductor que ya tiene asociado ese vehículo como habitual (o null). */
    @Override
    public Conductor buscarPorVehiculoHabitual(Vehiculo v) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE id_vehiculo_habitual = ?")) {
            ps.setInt(1, v.getId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el conductor.", e);
        }
    }

    @Override
    public List<Conductor> buscar(String texto) {
        String t = "%" + (texto == null ? "" : texto.trim()) + "%";
        List<Conductor> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE
                        + "WHERE nombre LIKE ? OR dni LIKE ? OR licencia LIKE ? ORDER BY nombre")) {
            ps.setString(1, t);
            ps.setString(2, t);
            ps.setString(3, t);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo buscar conductores.", e);
        }
        return lista;
    }
}