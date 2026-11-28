/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import logisticsfideexpress.dao.interfaz.IServicioDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logisticsfideexpress.dao.interfaz.IClienteDAO;
import logisticsfideexpress.dao.interfaz.IConductorDAO;
import logisticsfideexpress.dao.interfaz.IVehiculoDAO;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.AsignacionViaje;
import logisticsfideexpress.modelo.Carga;
import logisticsfideexpress.modelo.Cliente;
import logisticsfideexpress.modelo.Conductor;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.modelo.enums.EstadoUsuario;

/**
 * Acceso a MySQL para servicio (con su carga embebida) y asignacion_viaje.
 * Los seguimientos los guarda SeguimientoDAO.
 */
public class ServicioDAO implements IServicioDAO {

    private static final String SELECT_BASE
            = "SELECT s.id_servicio, s.id_cliente, s.origen, s.destino, s.fecha_solicitud, s.estado, s.tarifa, "
            + "s.carga_descripcion, s.carga_peso_kg, s.carga_volumen, s.motivo_rechazo, "
            + "s.receptor_entrega, s.fecha_entrega, "
            + "u.nombre, u.correo, u.clave_hash, u.estado AS c_estado, "
            + "c.documento, c.razon_social, c.telefono, c.direccion "
            + "FROM servicio s "
            + "JOIN usuario u ON u.id_usuario = s.id_cliente "
            + "JOIN cliente c ON c.id_usuario = s.id_cliente ";

    // Se conservan los parámetros del constructor para no tocar ServiceFactory.
    private final IClienteDAO clientes;
    private final IVehiculoDAO vehiculos;
    private final IConductorDAO conductores;

    public ServicioDAO(IClienteDAO clientes, IVehiculoDAO vehiculos, IConductorDAO conductores) {
        this.clientes = clientes;
        this.vehiculos = vehiculos;
        this.conductores = conductores;
    }

    private static LocalDate aFecha(Date d) {
        return d == null ? null : d.toLocalDate();
    }

    private static String texto(String s) {
        return s == null ? "" : s;
    }

    // ---------------------------------------------------------------- servicios
    private ServicioTransporte mapear(ResultSet rs) throws SQLException {
        Cliente cli = new Cliente(rs.getInt("id_cliente"), rs.getString("nombre"),
                rs.getString("correo"), rs.getString("clave_hash"), rs.getString("documento"),
                rs.getString("razon_social"), rs.getString("telefono"), rs.getString("direccion"));
        cli.setEstado(EstadoUsuario.valueOf(rs.getString("c_estado")));
        Carga carga = new Carga(rs.getString("carga_descripcion"),
                rs.getDouble("carga_peso_kg"), rs.getDouble("carga_volumen"));
        ServicioTransporte s = new ServicioTransporte(rs.getInt("id_servicio"), rs.getString("origen"),
                rs.getString("destino"), rs.getDate("fecha_solicitud").toLocalDate(),
                rs.getDouble("tarifa"), cli, carga);
        s.setEstado(EstadoServicio.valueOf(rs.getString("estado")));
        s.setMotivoRechazo(rs.getString("motivo_rechazo"));
        s.setReceptorEntrega(rs.getString("receptor_entrega"));
        s.setFechaEntrega(aFecha(rs.getDate("fecha_entrega")));
        return s;
    }

    /** Ejecuta SELECT_BASE + condición con parámetros y carga las asignaciones. */
    private List<ServicioTransporte> consultar(String condicion, Object... params) {
        List<ServicioTransporte> lista = new ArrayList<>();
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
            throw new AccesoDatosException("No se pudo consultar los servicios. Verifique la conexión con MySQL.", e);
        }
        cargarAsignaciones(lista);
        return lista;
    }

    /** Pone los 12 parámetros comunes de INSERT y UPDATE (mismo orden en ambos). */
    private void parametros(PreparedStatement ps, ServicioTransporte s) throws SQLException {
        ps.setInt(1, s.getCliente().getId());
        ps.setString(2, s.getOrigen());
        ps.setString(3, s.getDestino());
        ps.setDate(4, Date.valueOf(s.getFechaSolicitud()));
        ps.setString(5, s.getEstado().name());
        ps.setDouble(6, s.getTarifa());
        ps.setString(7, s.getCarga().getDescripcion());
        ps.setDouble(8, s.getCarga().getPesoKg());
        ps.setDouble(9, s.getCarga().getVolumen());
        ps.setString(10, texto(s.getMotivoRechazo()));
        ps.setString(11, texto(s.getReceptorEntrega()));
        if (s.getFechaEntrega() == null) {
            ps.setNull(12, Types.DATE);
        } else {
            ps.setDate(12, Date.valueOf(s.getFechaEntrega()));
        }
    }

    @Override
    public void guardar(ServicioTransporte s) {
        String sql = "INSERT INTO servicio (id_cliente, origen, destino, fecha_solicitud, estado, tarifa, "
                + "carga_descripcion, carga_peso_kg, carga_volumen, motivo_rechazo, receptor_entrega, fecha_entrega) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            parametros(ps, s);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    s.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el servicio. Verifique la conexión con MySQL.", e);
        }
    }

    @Override
    public ServicioTransporte buscarPorId(int id) {
        List<ServicioTransporte> r = consultar("WHERE s.id_servicio = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<ServicioTransporte> listar() {
        return consultar("ORDER BY s.id_servicio");
    }

    @Override
    public void actualizar(ServicioTransporte s) {
        String sql = "UPDATE servicio SET id_cliente = ?, origen = ?, destino = ?, fecha_solicitud = ?, "
                + "estado = ?, tarifa = ?, carga_descripcion = ?, carga_peso_kg = ?, carga_volumen = ?, "
                + "motivo_rechazo = ?, receptor_entrega = ?, fecha_entrega = ? WHERE id_servicio = ?";
        try (Connection cn = ConexionBD.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            parametros(ps, s);
            ps.setInt(13, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar el servicio.", e);
        }
    }

    /** Baja lógica: el servicio queda RECHAZADO (no se borra el histórico). */
    @Override
    public void darDeBaja(int id) {
        String sql = "UPDATE servicio SET estado = 'RECHAZADO', "
                + "motivo_rechazo = CASE WHEN motivo_rechazo = '' THEN 'Anulado' ELSE motivo_rechazo END "
                + "WHERE id_servicio = ?";
        try (Connection cn = ConexionBD.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo anular el servicio.", e);
        }
    }

    @Override
    public int siguienteId() {
        return siguiente("SELECT COALESCE(MAX(id_servicio), 0) + 1 FROM servicio");
    }

    @Override
    public List<ServicioTransporte> buscarPorCliente(int idCliente) {
        return consultar("WHERE s.id_cliente = ? ORDER BY s.id_servicio", idCliente);
    }

    // Sobrecarga: por estado
    @Override
    public List<ServicioTransporte> buscar(EstadoServicio estado) {
        return consultar("WHERE s.estado = ? ORDER BY s.id_servicio", estado.name());
    }

    // Sobrecarga: por texto (origen, destino o razón social del cliente), filtrada en SQL
    @Override
    public List<ServicioTransporte> buscar(String texto) {
        String t = "%" + (texto == null ? "" : texto.trim()) + "%";
        return consultar("WHERE s.origen LIKE ? OR s.destino LIKE ? OR c.razon_social LIKE ? "
                + "ORDER BY s.id_servicio", t, t, t);
    }

    // ------------------------------------------------------------- asignaciones
    /**
     * Carga las asignaciones de todos los servicios de la lista con UNA consulta
     * (IN con marcadores ?) y cachea vehículos y conductores para no repetirlos.
     */
    private void cargarAsignaciones(List<ServicioTransporte> servicios) {
        if (servicios.isEmpty()) {
            return;
        }
        Map<Integer, ServicioTransporte> porId = new HashMap<>();
        StringBuilder marcas = new StringBuilder();
        for (ServicioTransporte s : servicios) {
            porId.put(s.getId(), s);
            marcas.append(marcas.length() == 0 ? "?" : ",?");
        }
        String sql = "SELECT id_asignacion, id_servicio, id_vehiculo, id_conductor, fecha_salida, "
                + "fecha_llegada, estado FROM asignacion_viaje WHERE id_servicio IN (" + marcas
                + ") ORDER BY id_asignacion";
        Map<Integer, Vehiculo> cacheV = new HashMap<>();
        Map<Integer, Conductor> cacheC = new HashMap<>();
        try (Connection cn = ConexionBD.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            for (ServicioTransporte s : servicios) {
                ps.setInt(i++, s.getId());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ServicioTransporte s = porId.get(rs.getInt("id_servicio"));
                    Vehiculo v = cacheV.computeIfAbsent(rs.getInt("id_vehiculo"), vehiculos::buscarPorId);
                    Conductor c = cacheC.computeIfAbsent(rs.getInt("id_conductor"), conductores::buscarPorId);
                    if (s == null || v == null || c == null) {
                        continue;
                    }
                    AsignacionViaje a = new AsignacionViaje(rs.getInt("id_asignacion"), s, v, c,
                            rs.getDate("fecha_salida").toLocalDate());
                    a.setFechaLlegada(aFecha(rs.getDate("fecha_llegada")));
                    a.setEstado(rs.getString("estado"));
                    s.agregarAsignacion(a);
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo cargar las asignaciones.", e);
        }
    }

    @Override
    public int siguienteIdAsignacion() {
        return siguiente("SELECT COALESCE(MAX(id_asignacion), 0) + 1 FROM asignacion_viaje");
    }

    /** Inserta la asignación y la agrega al servicio en memoria. */
    @Override
    public void guardarAsignacion(AsignacionViaje a) {
        String sql = "INSERT INTO asignacion_viaje (id_servicio, id_vehiculo, id_conductor, fecha_salida, "
                + "fecha_llegada, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getServicio().getId());
            ps.setInt(2, a.getVehiculo().getId());
            ps.setInt(3, a.getConductor().getId());
            ps.setDate(4, Date.valueOf(a.getFechaSalida()));
            if (a.getFechaLlegada() == null) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(a.getFechaLlegada()));
            }
            ps.setString(6, a.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    a.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar la asignacion. Verifique la conexión con MySQL.", e);
        }
        a.getServicio().agregarAsignacion(a);
    }

    /** Guarda los cambios de una asignación existente (por ejemplo, al finalizar el viaje). */
    @Override
    public void actualizarAsignacion(AsignacionViaje a) {
        String sql = "UPDATE asignacion_viaje SET fecha_salida = ?, fecha_llegada = ?, estado = ? "
                + "WHERE id_asignacion = ?";
        try (Connection cn = ConexionBD.obtener(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(a.getFechaSalida()));
            if (a.getFechaLlegada() == null) {
                ps.setNull(2, Types.DATE);
            } else {
                ps.setDate(2, Date.valueOf(a.getFechaLlegada()));
            }
            ps.setString(3, a.getEstado());
            ps.setInt(4, a.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo actualizar la asignacion.", e);
        }
    }

    @Override
    public List<AsignacionViaje> listarAsignaciones() {
        List<AsignacionViaje> todas = new ArrayList<>();
        for (ServicioTransporte s : listar()) {
            todas.addAll(s.getAsignaciones());
        }
        return todas;
    }

    /** Asignación en estado ACTIVA de un servicio (o null). */
    @Override
    public AsignacionViaje buscarAsignacionActiva(ServicioTransporte s) {
        for (AsignacionViaje a : s.getAsignaciones()) {
            if (AsignacionViaje.ACTIVA.equals(a.getEstado())) {
                return a;
            }
        }
        return null;
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
