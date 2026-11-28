/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import java.time.LocalDate;
import logisticsfideexpress.dao.GenericDAO;
import java.util.List;
import logisticsfideexpress.modelo.Liquidacion;

/**
 *
 * @author JETLI
 */
public interface ILiquidacionDAO extends GenericDAO<Liquidacion> {

    // Sobrecarga: por rango de fechas
    List<Liquidacion> buscar(LocalDate desde, LocalDate hasta);


    Liquidacion buscarPorServicio(int idServicio);

    List<Liquidacion> listar();
    
}
