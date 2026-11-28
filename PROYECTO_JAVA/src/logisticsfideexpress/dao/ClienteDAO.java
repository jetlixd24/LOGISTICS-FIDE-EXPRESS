/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

import logisticsfideexpress.dao.interfaz.IUsuarioDAO;
/**
 *
 * @author JETLI
 */
import logisticsfideexpress.dao.interfaz.IClienteDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.Cliente;
import logisticsfideexpress.modelo.Usuario;
import logisticsfideexpress.modelo.enums.EstadoUsuario;

/**
 * Vista especializada de UsuarioDAO: guardar/actualizar/baja se delegan
 * (herencia por tablas); las consultas de cliente tienen SQL propio.
 */
public class ClienteDAO implements IClienteDAO {

    private static final String SELECT_BASE
            = "SELECT u.id_usuario, u.nombre, u.correo, u.clave_hash, u.estado, "
            + "c.documento, c.razon_social, c.telefono, c.direccion "
            + "FROM usuario u JOIN cliente c ON c.id_usuario = u.id_usuario ";

    private final IUsuarioDAO usuarios;

    public ClienteDAO(IUsuarioDAO usuarios) {
        this.usuarios = usuarios;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente(rs.getInt("id_usuario"), rs.getString("nombre"),
                rs.getString("correo"), rs.getString("clave_hash"),
                rs.getString("documento"), rs.getString("razon_social"),
                rs.getString("telefono"), rs.getString("direccion"));
        c.setEstado(EstadoUsuario.valueOf(rs.getString("estado")));
        return c;
    }

    @Override
    public void guardar(Cliente c) {
        usuarios.guardar(c);
    }

    @Override
    public Cliente buscarPorId(int id) {
        Usuario u = usuarios.buscarPorId(id);
        return (u instanceof Cliente c) ? c : null;
    }

    @Override
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY c.razon_social");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo listar los clientes. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    @Override
    public void actualizar(Cliente c) {
        usuarios.actualizar(c);
    }

    @Override
    public void darDeBaja(int id) {
        usuarios.darDeBaja(id);
    }

    @Override
    public int siguienteId() {
        return usuarios.siguienteId();
    }

    @Override
    public Cliente buscarPorDocumento(String documento) {
        if (documento == null) {
            return null;
        }
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE c.documento = ?")) {
            ps.setString(1, documento.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el cliente.", e);
        }
    }

    // Búsqueda por texto (nombre, razón social o documento), filtrada en SQL
    @Override
    public List<Cliente> buscar(String texto) {
        String t = "%" + (texto == null ? "" : texto.trim()) + "%";
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE
                        + "WHERE u.nombre LIKE ? OR c.razon_social LIKE ? OR c.documento LIKE ? "
                        + "ORDER BY c.razon_social")) {
            ps.setString(1, t);
            ps.setString(2, t);
            ps.setString(3, t);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo buscar clientes.", e);
        }
        return lista;
    }
}
