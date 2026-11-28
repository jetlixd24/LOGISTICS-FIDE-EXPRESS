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
import logisticsfideexpress.dao.interfaz.ITransportistaDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.Transportista;
import logisticsfideexpress.modelo.Usuario;
import logisticsfideexpress.modelo.enums.EstadoUsuario;

/**
 * Vista especializada de UsuarioDAO: guardar/actualizar/baja se delegan
 * (herencia por tablas); las consultas de transportista tienen SQL propio.
 */
public class TransportistaDAO implements ITransportistaDAO {

    private static final String SELECT_BASE
            = "SELECT u.id_usuario, u.nombre, u.correo, u.clave_hash, u.estado, "
            + "t.ruc, t.razon_social, t.telefono "
            + "FROM usuario u JOIN transportista t ON t.id_usuario = u.id_usuario ";

    private final IUsuarioDAO usuarios;

    public TransportistaDAO(IUsuarioDAO usuarios) {
        this.usuarios = usuarios;
    }

    private Transportista mapear(ResultSet rs) throws SQLException {
        Transportista t = new Transportista(rs.getInt("id_usuario"), rs.getString("nombre"),
                rs.getString("correo"), rs.getString("clave_hash"),
                rs.getString("ruc"), rs.getString("razon_social"), rs.getString("telefono"));
        t.setEstado(EstadoUsuario.valueOf(rs.getString("estado")));
        return t;
    }

    @Override
    public void guardar(Transportista t) {
        usuarios.guardar(t);
    }

    @Override
    public Transportista buscarPorId(int id) {
        Usuario u = usuarios.buscarPorId(id);
        return (u instanceof Transportista t) ? t : null;
    }

    @Override
    public List<Transportista> listar() {
        List<Transportista> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "ORDER BY t.razon_social");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo listar los transportistas. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    @Override
    public void actualizar(Transportista t) {
        usuarios.actualizar(t);
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
    public Transportista buscarPorRuc(String ruc) {
        if (ruc == null) {
            return null;
        }
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE t.ruc = ?")) {
            ps.setString(1, ruc.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el transportista.", e);
        }
    }

    // Búsqueda por texto (razón social o RUC), filtrada en SQL
    @Override
    public List<Transportista> buscar(String texto) {
        String t = "%" + (texto == null ? "" : texto.trim()) + "%";
        List<Transportista> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE
                        + "WHERE t.razon_social LIKE ? OR t.ruc LIKE ? ORDER BY t.razon_social")) {
            ps.setString(1, t);
            ps.setString(2, t);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo buscar transportistas.", e);
        }
        return lista;
    }
}