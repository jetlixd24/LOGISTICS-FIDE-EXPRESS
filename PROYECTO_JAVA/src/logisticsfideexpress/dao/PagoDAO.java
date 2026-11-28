/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

import logisticsfideexpress.dao.interfaz.IFacturaDAO;
/**
 *
 * @author JETLI
 */
import logisticsfideexpress.dao.interfaz.IPagoDAO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.modelo.Pago;

/**
 * Acceso a MySQL para pago. guardar() llama a sp_registrar_pago, que inserta el
 * pago y actualiza saldo y estado de la factura en una sola transacción.
 * Los pagos no se modifican ni se dan de baja (histórico contable, RN08).
 */
public class PagoDAO implements IPagoDAO {

    private static final String SELECT_BASE
            = "SELECT p.id_pago, p.id_factura, p.fecha, p.monto, p.metodo "
            + "FROM pago p JOIN factura f ON f.id_factura = p.id_factura ";

    private final IFacturaDAO facturas;

    public PagoDAO(IFacturaDAO facturas) {
        this.facturas = facturas;
    }

    /** Convierte una fila en Pago; las facturas ya leídas se reutilizan (caché). */
    private Pago mapear(ResultSet rs, Map<Integer, Factura> cache) throws SQLException {
        int idFactura = rs.getInt("id_factura");
        Factura f = cache.get(idFactura);
        if (f == null) {
            f = facturas.buscarPorId(idFactura);
            cache.put(idFactura, f);
        }
        if (f == null) {
            return null;
        }
        return new Pago(rs.getInt("id_pago"), f, rs.getDate("fecha").toLocalDate(),
                rs.getDouble("monto"), rs.getString("metodo"));
    }

    private List<Pago> consultar(String condicion, Object... params) {
        List<Pago> lista = new ArrayList<>();
        Map<Integer, Factura> cache = new HashMap<>();
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement(SELECT_BASE + condicion)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pago p = mapear(rs, cache);
                    if (p != null) {
                        lista.add(p);
                    }
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar los pagos. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }

    /** Registra el pago con sp_registrar_pago (pago + saldo + estado, transaccional). */
    @Override
    public void guardar(Pago p) {
        try (Connection cn = ConexionBD.obtener();
                CallableStatement cs = cn.prepareCall("{CALL sp_registrar_pago(?, ?, ?, ?)}")) {
            cs.setInt(1, p.getFactura().getId());
            cs.setBigDecimal(2, BigDecimal.valueOf(p.getMonto()).setScale(2, RoundingMode.HALF_UP));
            cs.setString(3, p.getMetodo());
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();
            p.setId(cs.getInt(4));
        } catch (SQLException e) {
            // SQLSTATE 45000 = mensaje propio del SIGNAL (llega claro al usuario)
            if ("45000".equals(e.getSQLState())) {
                throw new AccesoDatosException(e.getMessage(), e);
            }
            throw new AccesoDatosException("No se pudo registrar el pago. Verifique la conexión con MySQL.", e);
        }
    }

    @Override
    public Pago buscarPorId(int id) {
        List<Pago> r = consultar("WHERE p.id_pago = ?", id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Override
    public List<Pago> listar() {
        return consultar("ORDER BY p.id_pago");
    }

    @Override
    public void actualizar(Pago p) {
        throw new UnsupportedOperationException("Los pagos no se modifican (historico contable).");
    }

    @Override
    public void darDeBaja(int id) {
        throw new UnsupportedOperationException("Los pagos no se dan de baja (historico contable).");
    }

    @Override
    public int siguienteId() {
        try (Connection cn = ConexionBD.obtener();
                PreparedStatement ps = cn.prepareStatement("SELECT COALESCE(MAX(id_pago), 0) + 1 FROM pago");
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo obtener el siguiente id.", e);
        }
    }

    @Override
    public List<Pago> buscarPorFactura(int idFactura) {
        return consultar("WHERE p.id_factura = ? ORDER BY p.id_pago", idFactura);
    }

    /** Pagos de las facturas de un tercero (por ejemplo, los pagos de un cliente). */
    @Override
    public List<Pago> buscarPorTercero(String terceroTipo, int terceroId) {
        String col;
        if (Factura.TERCERO_CLIENTE.equals(terceroTipo)) {
            col = "f.id_cliente";
        } else if (Factura.TERCERO_PROVEEDOR.equals(terceroTipo)) {
            col = "f.id_proveedor";
        } else if (Factura.TERCERO_TRANSPORTISTA.equals(terceroTipo)) {
            col = "f.id_transportista";
        } else {
            return new ArrayList<>();
        }
        return consultar("WHERE " + col + " = ? ORDER BY p.id_pago", terceroId);
    }
}