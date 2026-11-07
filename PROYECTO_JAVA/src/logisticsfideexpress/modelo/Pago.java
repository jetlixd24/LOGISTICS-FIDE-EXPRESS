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

public class Pago {

    private int id;
    private Factura factura;
    private LocalDate fecha;
    private double monto;
    private String metodo;

    public Pago(int id, Factura factura, LocalDate fecha, double monto, String metodo) {
        this.id = id;
        this.factura = factura;
        this.fecha = fecha;
        this.monto = monto;
        this.metodo = metodo;
    }

    public Pago(Factura factura, LocalDate fecha, double monto, String metodo) {
        this(0, factura, fecha, monto, metodo);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Factura getFactura() {
        return factura;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getMonto() {
        return monto;
    }

    public String getMetodo() {
        return metodo;
    }
}
