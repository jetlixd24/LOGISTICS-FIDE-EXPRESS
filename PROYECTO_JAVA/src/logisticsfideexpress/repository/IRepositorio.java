/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.repository;

/**
 *
 * @author JETLI
 */
import java.util.List;

/** Contrato comun de todos los repositorios (ArrayList+archivos ahora, JDBC en la semana 14). */
public interface IRepositorio<T> {

    void guardar(T entidad);

    /** Devuelve null si no existe. */
    T buscarPorId(int id);

    List<T> listar();

    void actualizar(T entidad);

    /** Baja logica: no se borran pagos, facturas ni historicos. */
    void darDeBaja(int id);

    int siguienteId();
}
