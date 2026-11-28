/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import logisticsfideexpress.dao.GenericDAO;
import java.util.List;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.enums.EstadoVehiculo;

/**
 *
 * @author JETLI
 */
public interface IVehiculoDAO extends GenericDAO<Vehiculo> {

    // Sobrecarga: por texto (placa o marca), filtrada en SQL
    List<Vehiculo> buscar(String texto);

    // Sobrecarga: por estado
    List<Vehiculo> buscar(EstadoVehiculo estado);

    Vehiculo buscarPorPlaca(String placa);
    
}
