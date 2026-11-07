/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.modelo;

/**
 *
 * @author JETLI
 */
import java.time.LocalDateTime;
import logisticsfideexpress.modelo.enums.EstadoServicio;

public class SeguimientoCarga {

    private int id;
    private ServicioTransporte servicio;
    private LocalDateTime fechaHora;
    private EstadoServicio estado;
    private String ubicacion;
    private String observacion;

    public SeguimientoCarga(int id, ServicioTransporte servicio, LocalDateTime fechaHora,
            EstadoServicio estado, String ubicacion, String observacion) {
        this.id = id;
        this.servicio = servicio;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.observacion = observacion;
    }

    public SeguimientoCarga(ServicioTransporte servicio, LocalDateTime fechaHora,
            EstadoServicio estado, String ubicacion, String observacion) {
        this(0, servicio, fechaHora, estado, ubicacion, observacion);
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

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public EstadoServicio getEstado() {
        return estado;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public String getObservacion() {
        return observacion;
    }
}
