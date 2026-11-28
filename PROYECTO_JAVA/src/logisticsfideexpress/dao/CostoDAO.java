/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import logisticsfideexpress.dao.interfaz.ICostoDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logisticsfideexpress.dao.interfaz.IServicioDAO;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.CostoViaje;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoGasto;

/**
 * Acceso a MySQL para costo_viaje. Baja lógica: el gasto queda RECHAZADO.
 */
public class CostoDAO implements ICostoDAO {

    private static final String SELECT_BASE
            = "SELECT id_costo, id_servicio, tipo, monto, fecha, estado FROM costo_viaje ";

    private final IServicioDAO servicios;

    public CostoDAO(IServicioDAO servicios) {
        this.servicios = servicios;
    }

    /** Convierte una fila en CostoViaje; los servicios ya leídos se reutilizan (caché). */
    private CostoViaje mapear(ResultSet rs, Map<Integer, ServicioTransporte> cache) throws SQLException {
        int idServicio = rs.getInt("id_servicio");
        ServicioTransporte s = cache.get(idServicio);
        if (s == null) {
            s = servicios.buscarPorId(idServicio);
            cache.put(idServicio, s);
        }
        if (s == null) {
            return null;
        }
        CostoViaje c = new CostoViaje(rs.getInt("id_costo"), s, rs.getString("tipo"),
                rs.getDouble("monto"), rs.getDate("fecha").toLocalDate());
        c.setEstado(EstadoGasto.valueOf(rs.getString("estado")));
        return c;
    }

    private List<CostoViaje> consultar(String condicion, Object... params) {
        List<CostoViaje> lista = new ArrayList<>();
        Map<Integer, ServicioTransporte> cache = new HashMap<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + condicion)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CostoViaje c = mapear(rs, cache);
                    if (c != null) {
                        lista.add(c);
                    }
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar los gastos. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    @Override
    public void guardar(CostoViaje c) {
        String sql = "INSERT INTO costo_viaje (id_servicio, tipo, monto, fecha, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getServicio().getId());
            ps.setString(2, c.getTipo());
            ps.setDouble(3, c.getMonto());
            ps.setDate(4, Date.valueOf(c.getFecha()));
            ps.setString(5, c.getEstado().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el gasto. Verifique la conexión con MySQL.", e);
        }
    }

    @Override
    public CostoViaje buscarPorId(int id) {
        List<CostoViaje> r = consultar("WHERE id_costo = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<CostoViaje> listar() {
        return consultar("ORDER BY id_costo");
    }

    /** El modelo solo permite cambiar el estado, que es lo único que se actualiza. */
    @Override
    public void actualizar(CostoViaje c) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "UPDATE costo_viaje SET estado = ? WHERE id_costo = ?")) {
            ps.setString(1, c.getEstado().name());
            ps.setInt(2, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar el gasto.", e);
        }
    }

    /** Baja lógica: el gasto queda RECHAZADO. */
    @Override
    public void darDeBaja(int id) {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "UPDATE costo_viaje SET estado = 'RECHAZADO' WHERE id_costo = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo dar de baja el gasto.", e);
        }
    }

    @Override
    public int siguienteId() {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement("SELECT COALESCE(MAX(id_costo), 0) + 1 FROM costo_viaje");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener el siguiente id.", e);
        }
    }

    @Override
    public List<CostoViaje> buscarPorServicio(int idServicio) {
        return consultar("WHERE id_servicio = ? ORDER BY id_costo", idServicio);
    }

    // Sobrecarga: por estado
    @Override
    public List<CostoViaje> buscar(EstadoGasto estado) {
        return consultar("WHERE estado = ? ORDER BY id_costo", estado.name());
    }

    // Sobrecarga: por servicio y estado
    @Override
    public List<CostoViaje> buscar(int idServicio, EstadoGasto estado) {
        return consultar("WHERE id_servicio = ? AND estado = ? ORDER BY id_costo", idServicio, estado.name());
    }
}