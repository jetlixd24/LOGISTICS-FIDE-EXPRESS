/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package logisticsfideexpress.dao.interfaz;

import logisticsfideexpress.dao.GenericDAO;
import java.util.List;
import logisticsfideexpress.modelo.MovimientoAlmacen;
import logisticsfideexpress.modelo.Producto;

/**
 *
 * @author JETLI
 */
public interface IProductoDAO extends GenericDAO<Producto> {

    // Sobrecarga: por texto (código o nombre), filtrada en SQL
    List<Producto> buscar(String texto);

    /** Productos cuyo stock llegó o bajó del mínimo. */
    List<Producto> buscarBajoMinimo();

    Producto buscarPorCodigo(String codigo);

    void guardarMovimiento(MovimientoAlmacen m);

    List<MovimientoAlmacen> listarMovimientos();

    // Sobrecarga: movimientos de un producto
    List<MovimientoAlmacen> listarMovimientos(int idProducto);

    int siguienteIdMovimiento();
    
}
