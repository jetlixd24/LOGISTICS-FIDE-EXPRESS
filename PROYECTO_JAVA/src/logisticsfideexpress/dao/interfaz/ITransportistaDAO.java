/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import logisticsfideexpress.dao.GenericDAO;
import java.util.List;
import logisticsfideexpress.modelo.Transportista;

/**
 *
 * @author JETLI
 */
public interface ITransportistaDAO extends GenericDAO<Transportista> {

    // Búsqueda por texto (razón social o RUC), filtrada en SQL
    List<Transportista> buscar(String texto);

    Transportista buscarPorRuc(String ruc);
    
}
