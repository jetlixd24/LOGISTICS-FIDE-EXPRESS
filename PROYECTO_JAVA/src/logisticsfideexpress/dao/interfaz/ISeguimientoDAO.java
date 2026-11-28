/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import logisticsfideexpress.dao.GenericDAO;
import java.util.List;
import logisticsfideexpress.modelo.SeguimientoCarga;

/**
 *
 * @author JETLI
 */
public interface ISeguimientoDAO extends GenericDAO<SeguimientoCarga> {

    /** Seguimientos de un servicio, del más antiguo al más reciente. */
    List<SeguimientoCarga> buscarPorServicio(int idServicio);

    /** Último seguimiento registrado de un servicio (o null). */
    SeguimientoCarga buscarUltimo(int idServicio);
    
}
