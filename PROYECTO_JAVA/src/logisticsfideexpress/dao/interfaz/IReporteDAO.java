/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import java.time.LocalDate;
import java.util.List;
import logisticsfideexpress.modelo.ResumenPeriodo;

/**
 *
 * @author JETLI
 */
public interface IReporteDAO {

    /** Ingresos (pagos de facturas de venta) y egresos (pagos de compra) por mes. */
    List<ResumenPeriodo> ingresosEgresos(LocalDate desde, LocalDate hasta);
    
}
