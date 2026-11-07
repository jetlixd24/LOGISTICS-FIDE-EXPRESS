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

public class AsignacionViaje {

    public static final String ACTIVA = "ACTIVA";
    public static final String FINALIZADA = "FINALIZADA";

    private int id;
    private ServicioTransporte servicio;
    private Vehiculo vehiculo;
    private Conductor conductor;
    private LocalDate fechaSalida;
    private LocalDate fechaLlegada;
    private String estado = ACTIVA;

    public AsignacionViaje(int id, ServicioTransporte servicio, Vehiculo vehiculo,
            Conductor conductor, LocalDate fechaSalida) {
        this.id = id;
        this.servicio = servicio;
        this.vehiculo = vehiculo;
        this.conductor = conductor;
        this.fechaSalida = fechaSalida;
    }

    public AsignacionViaje(ServicioTransporte servicio, Vehiculo vehiculo,
            Conductor conductor, LocalDate fechaSalida) {
        this(0, servicio, vehiculo, conductor, fechaSalida);
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

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDate getFechaLlegada() {
        return fechaLlegada;
    }

    public void setFechaLlegada(LocalDate fechaLlegada) {
        this.fechaLlegada = fechaLlegada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
