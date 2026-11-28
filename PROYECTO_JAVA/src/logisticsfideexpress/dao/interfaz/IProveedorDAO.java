/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import logisticsfideexpress.dao.GenericDAO;
import java.util.List;
import logisticsfideexpress.modelo.Proveedor;

/**
 *
 * @author JETLI
 */
public interface IProveedorDAO extends GenericDAO<Proveedor> {

    // Búsqueda por texto (razón social o RUC), filtrada en SQL
    List<Proveedor> buscar(String texto);

    Proveedor buscarPorRuc(String ruc);
    
}
