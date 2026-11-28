/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import logisticsfideexpress.dao.interfaz.IReporteDAO;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.excepciones.AccesoDatosException;
import logisticsfideexpress.modelo.ResumenPeriodo;

/** Consultas de solo lectura para reportes y gráfico (llama a sp_ingresos_egresos). */
public class ReporteDAO implements IReporteDAO {

    /** Ingresos (pagos de facturas de venta) y egresos (pagos de compra) por mes. */
    @Override
    public List<ResumenPeriodo> ingresosEgresos(LocalDate desde, LocalDate hasta) {
        List<ResumenPeriodo> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtener();
                CallableStatement cs = cn.prepareCall("{CALL sp_ingresos_egresos(?, ?)}")) {
            cs.setDate(1, Date.valueOf(desde));
            cs.setDate(2, Date.valueOf(hasta));
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ResumenPeriodo(rs.getString("periodo"),
                            rs.getDouble("ingresos"), rs.getDouble("egresos")));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException(
                    "No se pudo generar el reporte de ingresos y egresos. Verifique la conexión con MySQL.", e);
        }
        return lista;
    }
}