/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import logisticsfideexpress.dao.GenericDAO;
import java.util.List;
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.modelo.enums.EstadoFactura;
import logisticsfideexpress.modelo.enums.TipoFactura;

/**
 *
 * @author JETLI
 */
public interface IFacturaDAO extends GenericDAO<Factura> {

    // Sobrecarga: por tipo (VENTA o COMPRA)
    List<Factura> buscar(TipoFactura tipo);

    // Sobrecarga: por estado
    List<Factura> buscar(EstadoFactura estado);

    Factura buscarPorNumero(String numero);

    /** Facturas de un tercero (por ejemplo, las facturas de venta de un cliente). */
    List<Factura> buscarPorTercero(String terceroTipo, int terceroId);

    /** True si ya existe una factura con ese número para ese mismo tercero. */
    boolean existe(String numero, String terceroTipo, int terceroId);
    
}
