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
import logisticsfideexpress.modelo.enums.EstadoGasto;

public class CostoViaje {

    private int id;
    private ServicioTransporte servicio;
    private String tipo; // combustible, peaje, viaticos, otros
    private double monto;
    private LocalDate fecha;
    private EstadoGasto estado = EstadoGasto.PENDIENTE;

    public CostoViaje(int id, ServicioTransporte servicio, String tipo, double monto, LocalDate fecha) {
        this.id = id;
        this.servicio = servicio;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
    }

    public CostoViaje(ServicioTransporte servicio, String tipo, double monto, LocalDate fecha) {
        this(0, servicio, tipo, monto, fecha);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ServicioTransporte getServicio() {
        return servicio;
    }

    public String getTipo() {
        return tipo;
    }

    public double getMonto() {
        return monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public EstadoGasto getEstado() {
        return estado;
    }

    public void setEstado(EstadoGasto estado) {
        this.estado = estado;
    }
}
