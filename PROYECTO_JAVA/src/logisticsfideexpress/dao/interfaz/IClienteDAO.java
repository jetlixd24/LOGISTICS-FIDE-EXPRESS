/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import java.util.List;
import logisticsfideexpress.dao.GenericDAO;
import logisticsfideexpress.modelo.Cliente;

/**
 *
 * @author JETLI
 */
public interface IClienteDAO extends GenericDAO<Cliente> {

    // Búsqueda por texto (nombre, razón social o documento), filtrada en SQL
    List<Cliente> buscar(String texto);

    Cliente buscarPorDocumento(String documento);
    
}
