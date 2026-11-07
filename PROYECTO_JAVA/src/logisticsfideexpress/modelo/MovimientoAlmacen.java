/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.modelo;

/**
 *
 * @author JETLI
 */
import java.time.LocalDate;
import logisticsfideexpress.modelo.enums.TipoMovimiento;

public class MovimientoAlmacen {

    private int id;
    private Producto producto;
    private LocalDate fecha;
    private TipoMovimiento tipo;
    private int cantidad;

    public MovimientoAlmacen(int id, Producto producto, LocalDate fecha, TipoMovimiento tipo, int cantidad) {
        this.id = id;
        this.producto = producto;
        this.fecha = fecha;
        this.tipo = tipo;
        this.cantidad = cantidad;
    }

    public MovimientoAlmacen(Producto producto, LocalDate fecha, TipoMovimiento tipo, int cantidad) {
        this(0, producto, fecha, tipo, cantidad);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public int getCantidad() {
        return cantidad;
    }
}
