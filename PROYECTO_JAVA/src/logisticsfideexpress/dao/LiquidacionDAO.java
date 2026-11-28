/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import logisticsfideexpress.dao.interfaz.ILiquidacionDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logisticsfideexpress.dao.interfaz.IServicioDAO;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.DetalleLiquidacion;
import logisticsfideexpress.modelo.Liquidacion;
import logisticsfideexpress.modelo.ServicioTransporte;

/**
 * Acceso a MySQL para liquidacion + detalle_liquidacion (composición: los
 * detalles se guardan y se leen junto con la liquidación). Las liquidaciones no
 * se modifican ni se dan de baja (histórico contable).
 */
public class LiquidacionDAO implements ILiquidacionDAO {

    private static final int MYSQL_DUPLICADO = 1062;

    private static final String SELECT_BASE
            = "SELECT id_liquidacion, id_servicio, fecha, total_gastos, utilidad FROM liquidacion ";

    private final IServicioDAO servicios;

    public LiquidacionDAO(IServicioDAO servicios) {
        this.servicios = servicios;
    }

    /** Convierte una fila en Liquidacion y carga sus detalles con la misma conexión. */
    private Liquidacion mapear(Connection cn, ResultSet rs, Map<Integer, ServicioTransporte> cache)
            throws SQLException {
        int idServicio = rs.getInt("id_servicio");
        ServicioTransporte s = cache.get(idServicio);
        if (s == null) {
            s = servicios.buscarPorId(idServicio);
            cache.put(idServicio, s);
        }
        if (s == null) {
            return null;
        }
        int idLiquidacion = rs.getInt("id_liquidacion");
        Liquidacion l = new Liquidacion(idLiquidacion, s, rs.getDate("fecha").toLocalDate());
        double totalGastos = rs.getDouble("total_gastos");
        double utilidad = rs.getDouble("utilidad");

        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT concepto, monto FROM detalle_liquidacion WHERE id_liquidacion = ? ORDER BY id_detalle")) {
            ps.setInt(1, idLiquidacion);
            try (ResultSet rd = ps.executeQuery()) {
                while (rd.next()) {
                    l.agregarDetalle(rd.getString("concepto"), rd.getDouble("monto"));
                }
            }
        }
        // mismo cálculo del modelo: ingreso = gastos + utilidad
        l.calcular(totalGastos + utilidad);
        return l;
    }

    private List<Liquidacion> consultar(String condicion, Object... params) {
        List<Liquidacion> lista = new ArrayList<>();
        Map<Integer, ServicioTransporte> cache = new HashMap<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + condicion)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Liquidacion l = mapear(cn, rs, cache);
                    if (l != null) {
                        lista.add(l);
                    }
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar las liquidaciones. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    /** Guarda la liquidación y sus detalles en una sola transacción. */
    @Override
    public void guardar(Liquidacion l) {
        String sqlL = "INSERT INTO liquidacion (id_servicio, fecha, total_gastos, utilidad) VALUES (?, ?, ?, ?)";
        String sqlD = "INSERT INTO detalle_liquidacion (id_liquidacion, concepto, monto) VALUES (?, ?, ?)";
        try (Connection cn = ConexionBD.obtener()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psL = cn.prepareStatement(sqlL, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement psD = cn.prepareStatement(sqlD)) {
                psL.setInt(1, l.getServicio().getId());
                psL.setDate(2, Date.valueOf(l.getFecha()));
                psL.setDouble(3, l.getTotalGastos());
                psL.setDouble(4, l.getUtilidad());
                psL.executeUpdate();
                try (ResultSet rs = psL.getGeneratedKeys()) {
                    rs.next();
                    l.setId(rs.getInt(1));
                }
                for (DetalleLiquidacion d : l.getDetalles()) {
                    psD.setInt(1, l.getId());
                    psD.setString(2, d.getConcepto());
                    psD.setDouble(3, d.getMonto());
                    psD.addBatch();
                }
                psD.executeBatch();
                cn.commit();
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == MYSQL_DUPLICADO) {
                throw new AccesoDatosException("El servicio ya fue liquidado.", e);
            }
            throw new AccesoDatosException("No se pudo guardar la liquidación. Verifique la conexión con MySQL.", e);
        }
    }

    @Override
    public Liquidacion buscarPorId(int id) {
        List<Liquidacion> r = consultar("WHERE id_liquidacion = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Liquidacion> listar() {
        return consultar("ORDER BY id_liquidacion");
    }

    @Override
    public void actualizar(Liquidacion l) {
        throw new UnsupportedOperationException("Las liquidaciones no se modifican (histórico contable).");
    }

    @Override
    public void darDeBaja(int id) {
        throw new UnsupportedOperationException("Las liquidaciones no se dan de baja (histórico contable).");
    }

    @Override
    public int siguienteId() {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(
                        "SELECT COALESCE(MAX(id_liquidacion), 0) + 1 FROM liquidacion");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener el siguiente id.", e);
        }
    }

    @Override
    public Liquidacion buscarPorServicio(int idServicio) {
        List<Liquidacion> r = consultar("WHERE id_servicio = ?", idServicio);
        return r.isEmpty() ? null : r.get(0);
    }

    // Sobrecarga: por rango de fechas
    public List<Liquidacion> buscar(LocalDate desde, LocalDate hasta) {
        return consultar("WHERE fecha BETWEEN ? AND ? ORDER BY fecha, id_liquidacion",
                Date.valueOf(desde), Date.valueOf(hasta));
    }
}