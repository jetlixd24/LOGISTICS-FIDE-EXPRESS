/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import logisticsfideexpress.dao.GenericDAO;
import java.util.List;
import logisticsfideexpress.modelo.AsignacionViaje;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoServicio;

/**
 *
 * @author JETLI
 */
public interface IServicioDAO extends GenericDAO<ServicioTransporte> {

    /** Guarda los cambios de una asignación existente (por ejemplo, al finalizar el viaje). */
    void actualizarAsignacion(AsignacionViaje a);

    // Sobrecarga: por estado
    List<ServicioTransporte> buscar(EstadoServicio estado);

    // Sobrecarga: por texto (origen, destino o razón social del cliente), filtrada en SQL
    List<ServicioTransporte> buscar(String texto);

    /** Asignación en estado ACTIVA de un servicio (o null). */
    AsignacionViaje buscarAsignacionActiva(ServicioTransporte s);

    List<ServicioTransporte> buscarPorCliente(int idCliente);

    ServicioTransporte buscarPorId(int id);

    /** Inserta la asignación y la agrega al servicio en memoria. */
    void guardarAsignacion(AsignacionViaje a);

    List<AsignacionViaje> listarAsignaciones();

    int siguienteIdAsignacion();
    
}
