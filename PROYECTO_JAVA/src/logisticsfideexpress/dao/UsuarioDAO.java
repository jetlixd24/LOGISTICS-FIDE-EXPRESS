/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import logisticsfideexpress.dao.interfaz.IUsuarioDAO;
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
import logisticsfideexpress.modelo.Administrador;
import logisticsfideexpress.modelo.Cliente;
import logisticsfideexpress.modelo.Transportista;
import logisticsfideexpress.modelo.Usuario;
import logisticsfideexpress.modelo.enums.EstadoUsuario;
import logisticsfideexpress.modelo.enums.TipoUsuario;

/**
 * Acceso a MySQL para usuario + administrador / cliente / transportista
 * (herencia por tablas).
 */
public class UsuarioDAO implements IUsuarioDAO {

    private static final String SELECT_BASE
            = "SELECT u.id_usuario, u.tipo, u.nombre, u.correo, u.clave_hash, u.estado, u.registrado_por, "
            + "a.cargo, "
            + "c.documento, c.razon_social AS c_razon, c.telefono AS c_tel, c.direccion AS c_dir, "
            + "t.ruc, t.razon_social AS t_razon, t.telefono AS t_tel "
            + "FROM usuario u "
            + "LEFT JOIN administrador a ON a.id_usuario = u.id_usuario "
            + "LEFT JOIN cliente c ON c.id_usuario = u.id_usuario "
            + "LEFT JOIN transportista t ON t.id_usuario = u.id_usuario ";

    /** Convierte una fila en Usuario (= deLinea del repositorio viejo). */
    private Usuario mapear(ResultSet rs, boolean resolverRegistrador) throws SQLException {
        int id = rs.getInt("id_usuario");
        TipoUsuario tipo = TipoUsuario.valueOf(rs.getString("tipo"));
        String nombre = rs.getString("nombre");
        String correo = rs.getString("correo");
        String hash = rs.getString("clave_hash");
        Usuario u;
        switch (tipo) {
            case ADMINISTRADOR ->
                u = new Administrador(id, nombre, correo, hash, rs.getString("cargo"));
            case CLIENTE ->
                u = new Cliente(id, nombre, correo, hash, rs.getString("documento"),
                        rs.getString("c_razon"), rs.getString("c_tel"), rs.getString("c_dir"));
            default ->
                u = new Transportista(id, nombre, correo, hash, rs.getString("ruc"),
                        rs.getString("t_razon"), rs.getString("t_tel"));
        }
        u.setEstado(EstadoUsuario.valueOf(rs.getString("estado")));
        int reg = rs.getInt("registrado_por");
        if (!rs.wasNull() && resolverRegistrador) {
            u.setRegistradoPor(buscarPorId(reg, false));
        }
        return u;
    }

    @Override
    public void guardar(Usuario u) {
        String sqlU = "INSERT INTO usuario (tipo, nombre, correo, clave_hash, estado, registrado_por) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(sqlU, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, u.getTipo().name());
                ps.setString(2, u.getNombre());
                ps.setString(3, u.getCorreo());
                ps.setString(4, u.getClaveHash());
                ps.setString(5, u.getEstado().name());
                if (u.getRegistradoPor() == null) {
                    ps.setNull(6, Types.INTEGER);
                } else {
                    ps.setInt(6, u.getRegistradoPor().getId());
                }
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    u.setId(rs.getInt(1));
                }
                insertarDetalle(cn, u);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AccesoDatosException("Ya existe un registro con ese correo o documento.", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el usuario. Verifique la conexión con MySQL.", e);
        }
    }

    private void insertarDetalle(Connection cn, Usuario u) throws SQLException {
        if (u instanceof Administrador a) {
            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO administrador (id_usuario, cargo) VALUES (?, ?)")) {
                ps.setInt(1, a.getId());
                ps.setString(2, a.getCargo());
                ps.executeUpdate();
            }
        } else if (u instanceof Cliente c) {
            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO cliente (id_usuario, documento, razon_social, telefono, direccion) "
                    + "VALUES (?, ?, ?, ?, ?)")) {
                ps.setInt(1, c.getId());
                ps.setString(2, c.getDocumento());
                ps.setString(3, c.getRazonSocial());
                ps.setString(4, c.getTelefono());
                ps.setString(5, c.getDireccion());
                ps.executeUpdate();
            }
        } else if (u instanceof Transportista t) {
            try (PreparedStatement ps = cn.prepareStatement(
                    "INSERT INTO transportista (id_usuario, ruc, razon_social, telefono) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, t.getId());
                ps.setString(2, t.getRuc());
                ps.setString(3, t.getRazonSocial());
                ps.setString(4, t.getTelefono());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public Usuario buscarPorId(int id) {
        return buscarPorId(id, true);
    }

    private Usuario buscarPorId(int id, boolean resolverRegistrador) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE u.id_usuario = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs, resolverRegistrador) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el usuario.", e);
        }
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        if (correo == null) {
            return null;
        }
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE LOWER(u.correo) = ?")) {
            ps.setString(1, correo.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs, true) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el usuario. Verifique la conexión con MySQL.", e);
        }
    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY u.nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs, false));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo listar los usuarios. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    /** Búsqueda por texto (nombre o correo), filtrada en SQL con LIKE. */
    @Override
    public List<Usuario> buscar(String texto) {
        String t = "%" + (texto == null ? "" : texto.trim()) + "%";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        SELECT_BASE + "WHERE u.nombre LIKE ? OR u.correo LIKE ? ORDER BY u.nombre")) {
            ps.setString(1, t);
            ps.setString(2, t);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs, false));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo buscar usuarios.", e);
        }
        return lista;
    }

    @Override
    public void actualizar(Usuario u) {
        String sqlU = "UPDATE usuario SET nombre = ?, correo = ?, clave_hash = ?, estado = ? WHERE id_usuario = ?";
        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try (PreparedStatement ps = cn.prepareStatement(sqlU)) {
                ps.setString(1, u.getNombre());
                ps.setString(2, u.getCorreo());
                ps.setString(3, u.getClaveHash());
                ps.setString(4, u.getEstado().name());
                ps.setInt(5, u.getId());
                ps.executeUpdate();
                actualizarDetalle(cn, u);
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AccesoDatosException("Ya existe un registro con ese correo o documento.", e);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar el usuario.", e);
        }
    }

    private void actualizarDetalle(Connection cn, Usuario u) throws SQLException {
        if (u instanceof Administrador a) {
            try (PreparedStatement ps = cn.prepareStatement(
                    "UPDATE administrador SET cargo = ? WHERE id_usuario = ?")) {
                ps.setString(1, a.getCargo());
                ps.setInt(2, a.getId());
                ps.executeUpdate();
            }
        } else if (u instanceof Cliente c) {
            try (PreparedStatement ps = cn.prepareStatement(
                    "UPDATE cliente SET documento = ?, razon_social = ?, telefono = ?, direccion = ? "
                    + "WHERE id_usuario = ?")) {
                ps.setString(1, c.getDocumento());
                ps.setString(2, c.getRazonSocial());
                ps.setString(3, c.getTelefono());
                ps.setString(4, c.getDireccion());
                ps.setInt(5, c.getId());
                ps.executeUpdate();
            }
        } else if (u instanceof Transportista t) {
            try (PreparedStatement ps = cn.prepareStatement(
                    "UPDATE transportista SET ruc = ?, razon_social = ?, telefono = ? WHERE id_usuario = ?")) {
                ps.setString(1, t.getRuc());
                ps.setString(2, t.getRazonSocial());
                ps.setString(3, t.getTelefono());
                ps.setInt(4, t.getId());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public void darDeBaja(int id) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "UPDATE usuario SET estado = 'INACTIVO' WHERE id_usuario = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo dar de baja al usuario.", e);
        }
    }

    @Override
    public int siguienteId() {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement("SELECT COALESCE(MAX(id_usuario), 0) + 1 FROM usuario");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener el siguiente id.", e);
        }
    }
}