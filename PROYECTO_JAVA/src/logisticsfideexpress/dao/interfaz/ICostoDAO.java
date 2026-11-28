/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import logisticsfideexpress.dao.GenericDAO;
import java.util.List;
import logisticsfideexpress.modelo.CostoViaje;
import logisticsfideexpress.modelo.enums.EstadoGasto;

/**
 *
 * @author JETLI
 */
public interface ICostoDAO extends GenericDAO<CostoViaje> {

    // Sobrecarga: por estado
    List<CostoViaje> buscar(EstadoGasto estado);

    // Sobrecarga: por servicio y estado
    List<CostoViaje> buscar(int idServicio, EstadoGasto estado);

    List<CostoViaje> buscarPorServicio(int idServicio);
    
}
