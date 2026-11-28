    /*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
     */
    package logisticsfideexpress.dao;

    /**
     *
     * @author JETLI
     */
import logisticsfideexpress.dao.interfaz.ISeguimientoDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logisticsfideexpress.dao.interfaz.IServicioDAO;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.SeguimientoCarga;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoServicio;

/**
 * Acceso a MySQL para seguimiento_carga. Es histórico (RN09): solo INSERT y
 * consultas; no se actualiza ni se da de baja.
 */
public class SeguimientoDAO implements ISeguimientoDAO {

    private static final String SELECT_BASE
            = "SELECT id_seguimiento, id_servicio, fecha_hora, estado, ubicacion, observacion "
            + "FROM seguimiento_carga ";

    private final IServicioDAO servicios;

    public SeguimientoDAO(IServicioDAO servicios) {
        this.servicios = servicios;
    }

    /** Convierte una fila en SeguimientoCarga; los servicios ya leídos se reutilizan (caché). */
    private SeguimientoCarga mapear(ResultSet rs, Map<Integer, ServicioTransporte> cache) throws SQLException {
        int idServicio = rs.getInt("id_servicio");
        ServicioTransporte s = cache.get(idServicio);
        if (s == null) {
            s = servicios.buscarPorId(idServicio);
            cache.put(idServicio, s);
        }
        if (s == null) {
            return null;
        }
        return new SeguimientoCarga(rs.getInt("id_seguimiento"), s,
                rs.getTimestamp("fecha_hora").toLocalDateTime(),
                EstadoServicio.valueOf(rs.getString("estado")),
                rs.getString("ubicacion"), rs.getString("observacion"));
    }

    private List<SeguimientoCarga> consultar(String condicion, Object... params) {
        List<SeguimientoCarga> lista = new ArrayList<>();
        Map<Integer, ServicioTransporte> cache = new HashMap<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + condicion)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SeguimientoCarga s = mapear(rs, cache);
                    if (s != null) {
                        lista.add(s);
                    }
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el seguimiento. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    /** Guarda el seguimiento y lo agrega también a la lista del servicio (agregación en memoria). */
    @Override
    public void guardar(SeguimientoCarga s) {
        String sql = "INSERT INTO seguimiento_carga (id_servicio, fecha_hora, estado, ubicacion, observacion) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getServicio().getId());
            ps.setTimestamp(2, Timestamp.valueOf(s.getFechaHora()));
            ps.setString(3, s.getEstado().name());
            ps.setString(4, s.getUbicacion());
            ps.setString(5, s.getObservacion() == null ? "" : s.getObservacion());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    s.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el seguimiento. Verifique la conexión con MySQL.", e);
        }
        s.getServicio().agregarSeguimiento(s);
    }

    @Override
    public SeguimientoCarga buscarPorId(int id) {
        List<SeguimientoCarga> r = consultar("WHERE id_seguimiento = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<SeguimientoCarga> listar() {
        return consultar("ORDER BY fecha_hora, id_seguimiento");
    }

    @Override
    public void actualizar(SeguimientoCarga s) {
        throw new UnsupportedOperationException("Los seguimientos son historicos y no se modifican (RN09).");
    }

    @Override
    public void darDeBaja(int id) {
        throw new UnsupportedOperationException("Los seguimientos son historicos y no se dan de baja.");
    }

    @Override
    public int siguienteId() {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "SELECT COALESCE(MAX(id_seguimiento), 0) + 1 FROM seguimiento_carga");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener el siguiente id.", e);
        }
    }

    /** Seguimientos de un servicio, del más antiguo al más reciente. */
    @Override
    public List<SeguimientoCarga> buscarPorServicio(int idServicio) {
        return consultar("WHERE id_servicio = ? ORDER BY fecha_hora, id_seguimiento", idServicio);
    }

    /** Último seguimiento registrado de un servicio (o null). */
    @Override
    public SeguimientoCarga buscarUltimo(int idServicio) {
        List<SeguimientoCarga> r = consultar(
                "WHERE id_servicio = ? ORDER BY fecha_hora DESC, id_seguimiento DESC LIMIT 1", idServicio);
        return r.isEmpty() ? null : r.get(0);
    }
}