/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */

import logisticsfideexpress.dao.interfaz.IVehiculoDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.dao.interfaz.ITransportistaDAO;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.Transportista;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.VehiculoAfiliado;
import logisticsfideexpress.modelo.VehiculoPropio;
import logisticsfideexpress.modelo.enums.EstadoUsuario;
import logisticsfideexpress.modelo.enums.EstadoVehiculo;

/**
 * Acceso a MySQL para la tabla vehiculo (PROPIO y AFILIADO en una sola tabla).
 * El transportista del afiliado se trae con LEFT JOIN, sin consultas extra.
 */
public class VehiculoDAO implements IVehiculoDAO {

    private static final String SELECT_BASE
            = "SELECT v.id_vehiculo, v.tipo, v.placa, v.marca, v.capacidad_kg, v.estado, "
            + "v.kilometraje, v.fecha_compra, v.tarifa_acordada, v.id_transportista, "
            + "u.nombre, u.correo, u.clave_hash, u.estado AS t_estado, "
            + "t.ruc, t.razon_social, t.telefono "
            + "FROM vehiculo v "
            + "LEFT JOIN usuario u ON u.id_usuario = v.id_transportista "
            + "LEFT JOIN transportista t ON t.id_usuario = v.id_transportista ";

    // Se conserva el parámetro para no tocar ServiceFactory; el JOIN ya trae al transportista.
    private final ITransportistaDAO transportistas;

    public VehiculoDAO(ITransportistaDAO transportistas) {
        this.transportistas = transportistas;
    }

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_vehiculo");
        String placa = rs.getString("placa");
        String marca = rs.getString("marca");
        double capacidad = rs.getDouble("capacidad_kg");
        Vehiculo v;
        if ("PROPIO".equals(rs.getString("tipo"))) {
            Date f = rs.getDate("fecha_compra");
            LocalDate fecha = (f == null) ? null : f.toLocalDate();
            v = new VehiculoPropio(id, placa, marca, capacidad, rs.getDouble("kilometraje"), fecha);
        } else {
            Transportista t = new Transportista(rs.getInt("id_transportista"), rs.getString("nombre"),
                    rs.getString("correo"), rs.getString("clave_hash"),
                    rs.getString("ruc"), rs.getString("razon_social"), rs.getString("telefono"));
            t.setEstado(EstadoUsuario.valueOf(rs.getString("t_estado")));
            VehiculoAfiliado a = new VehiculoAfiliado(id, placa, marca, capacidad,
                    rs.getDouble("tarifa_acordada"), t);
            t.agregarVehiculo(a);
            v = a;
        }
        v.setEstado(EstadoVehiculo.valueOf(rs.getString("estado")));
        return v;
    }

    /** Pone los 4 parámetros propios de cada hijo (los que no aplican van NULL). */
    private void fijarEspecificos(PreparedStatement ps, Vehiculo v, int pos) throws SQLException {
        if (v instanceof VehiculoPropio p) {
            ps.setDouble(pos, p.getKilometraje());
            if (p.getFechaCompra() == null) {
                ps.setNull(pos + 1, Types.DATE);
            } else {
                ps.setDate(pos + 1, Date.valueOf(p.getFechaCompra()));
            }
            ps.setNull(pos + 2, Types.DECIMAL);
            ps.setNull(pos + 3, Types.INTEGER);
        } else {
            VehiculoAfiliado a = (VehiculoAfiliado) v;
            ps.setNull(pos, Types.DECIMAL);
            ps.setNull(pos + 1, Types.DATE);
            ps.setDouble(pos + 2, a.getTarifaAcordada());
            ps.setInt(pos + 3, a.getTransportista().getId());
        }
    }

    @Override
    public void guardar(Vehiculo v) {
        String sql = "INSERT INTO vehiculo (tipo, placa, marca, capacidad_kg, estado, "
                + "kilometraje, fecha_compra, tarifa_acordada, id_transportista) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getTipo());
            ps.setString(2, v.getPlaca());
            ps.setString(3, v.getMarca());
            ps.setDouble(4, v.getCapacidadKg());
            ps.setString(5, v.getEstado().name());
            fijarEspecificos(ps, v, 6);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    v.setId(rs.getInt(1));
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AccesoDatosException("La placa ya esta registrada.", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el vehiculo. Verifique la conexión con MySQL.", e);
        }
    }

    @Override
    public Vehiculo buscarPorId(int id) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE v.id_vehiculo = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el vehiculo.", e);
        }
    }

    @Override
    public List<Vehiculo> listar() {
        List<Vehiculo> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY v.placa");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo listar los vehiculos. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    @Override
    public void actualizar(Vehiculo v) {
        String sql = "UPDATE vehiculo SET placa = ?, marca = ?, capacidad_kg = ?, estado = ?, "
                + "kilometraje = ?, fecha_compra = ?, tarifa_acordada = ?, id_transportista = ? "
                + "WHERE id_vehiculo = ?";
        try (Connection cn = ConexionBD.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, v.getPlaca());
            ps.setString(2, v.getMarca());
            ps.setDouble(3, v.getCapacidadKg());
            ps.setString(4, v.getEstado().name());
            fijarEspecificos(ps, v, 5);
            ps.setInt(9, v.getId());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AccesoDatosException("La placa ya esta registrada.", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar el vehiculo.", e);
        }
    }

    @Override
    public void darDeBaja(int id) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "UPDATE vehiculo SET estado = 'INACTIVO' WHERE id_vehiculo = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo dar de baja el vehiculo.", e);
        }
    }

    @Override
    public int siguienteId() {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement("SELECT COALESCE(MAX(id_vehiculo), 0) + 1 FROM vehiculo");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener el siguiente id.", e);
        }
    }

    @Override
    public Vehiculo buscarPorPlaca(String placa) {
        if (placa == null) {
            return null;
        }
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE v.placa = ?")) {
            ps.setString(1, placa.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el vehiculo.", e);
        }
    }

    // Sobrecarga: por texto (placa o marca), filtrada en SQL
    @Override
    public List<Vehiculo> buscar(String texto) {
        String t = "%" + (texto == null ? "" : texto.trim()) + "%";
        List<Vehiculo> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE
                        + "WHERE v.placa LIKE ? OR v.marca LIKE ? ORDER BY v.placa")) {
            ps.setString(1, t);
            ps.setString(2, t);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo buscar vehiculos.", e);
        }
        return lista;
    }

    // Sobrecarga: por estado
    @Override
    public List<Vehiculo> buscar(EstadoVehiculo estado) {
        List<Vehiculo> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE
                        + "WHERE v.estado = ? ORDER BY v.placa")) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo filtrar vehiculos por estado.", e);
        }
        return lista;
    }
}