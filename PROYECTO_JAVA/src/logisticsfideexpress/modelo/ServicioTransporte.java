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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import logisticsfideexpress.modelo.enums.EstadoServicio;

public class ServicioTransporte {

    private int id;
    private String origen;
    private String destino;
    private LocalDate fechaSolicitud;
    private EstadoServicio estado = EstadoServicio.PENDIENTE;
    private double tarifa;
    private Cliente cliente;
    private final Carga carga; // composicion: la carga no existe sin el servicio
    private String motivoRechazo = "";
    private String receptorEntrega = "";
    private LocalDate fechaEntrega;
    private final List<AsignacionViaje> asignaciones = new ArrayList<>();
    private final List<SeguimientoCarga> seguimientos = new ArrayList<>();

    public ServicioTransporte(int id, String origen, String destino, LocalDate fechaSolicitud,
            double tarifa, Cliente cliente, Carga carga) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.fechaSolicitud = fechaSolicitud;
        this.tarifa = tarifa;
        this.cliente = cliente;
        this.carga = carga;
    }

    public ServicioTransporte(String origen, String destino, LocalDate fechaSolicitud,
            double tarifa, Cliente cliente, Carga carga) {
        this(0, origen, destino, fechaSolicitud, tarifa, cliente, carga);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public EstadoServicio getEstado() {
        return estado;
    }

    public void setEstado(EstadoServicio estado) {
        this.estado = estado;
    }

    public double getTarifa() {
        return tarifa;
    }

    public void setTarifa(double tarifa) {
        this.tarifa = tarifa;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Carga getCarga() {
        return carga;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public String getReceptorEntrega() {
        return receptorEntrega;
    }

    public void setReceptorEntrega(String receptorEntrega) {
        this.receptorEntrega = receptorEntrega;
    }

    public LocalDate getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDate fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public List<AsignacionViaje> getAsignaciones() {
        return Collections.unmodifiableList(asignaciones);
    }

    public void agregarAsignacion(AsignacionViaje a) {
        asignaciones.add(a);
    }

    public List<SeguimientoCarga> getSeguimientos() {
        return Collections.unmodifiableList(seguimientos);
    }

    public void agregarSeguimiento(SeguimientoCarga s) {
        seguimientos.add(s);
    }

    @Override
    public String toString() {
        return "Servicio #" + id + " " + origen + " -> " + destino;
    }
}
