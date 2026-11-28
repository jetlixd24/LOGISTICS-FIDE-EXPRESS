/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import logisticsfideexpress.dao.GenericDAO;
import java.util.List;
import logisticsfideexpress.modelo.Conductor;
import logisticsfideexpress.modelo.Vehiculo;

/**
 *
 * @author JETLI
 */
public interface IConductorDAO extends GenericDAO<Conductor> {

    List<Conductor> buscar(String texto);

    Conductor buscarPorLicencia(String licencia);

    /** Conductor que ya tiene asociado ese vehículo como habitual (o null). */
    Conductor buscarPorVehiculoHabitual(Vehiculo v);
    
}
