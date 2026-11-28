/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import logisticsfideexpress.dao.GenericDAO;
import java.util.List;
import logisticsfideexpress.modelo.Pago;

/**
 *
 * @author JETLI
 */
public interface IPagoDAO extends GenericDAO<Pago> {

    List<Pago> buscarPorFactura(int idFactura);

    /** Pagos de las facturas de un tercero (por ejemplo, los pagos de un cliente). */
    List<Pago> buscarPorTercero(String terceroTipo, int terceroId);
    
}
