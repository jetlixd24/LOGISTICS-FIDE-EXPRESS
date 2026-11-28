/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import logisticsfideexpress.dao.GenericDAO;
import java.util.List;
import logisticsfideexpress.modelo.Usuario;

/**
 *
 * @author JETLI
 */
public interface IUsuarioDAO extends GenericDAO<Usuario> {

    /** Búsqueda por texto (nombre o correo), filtrada en SQL con LIKE. */
    List<Usuario> buscar(String texto);

    Usuario buscarPorCorreo(String correo);
    
}
