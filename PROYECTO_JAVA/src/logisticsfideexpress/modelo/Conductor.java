/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.modelo;

/**
 *
 * @author JETLI
 */
import logisticsfideexpress.modelo.enums.EstadoConductor;

public class Conductor {

    private int id;
    private String dni;
    private String nombre;
    private String licencia;
    private String categoria;
    private EstadoConductor estado = EstadoConductor.DISPONIBLE;
    private Vehiculo vehiculoHabitual; // puede ser null

    public Conductor(int id, String dni, String nombre, String licencia, String categoria) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.licencia = licencia;
        this.categoria = categoria;
    }

    public Conductor(String dni, String nombre, String licencia, String categoria) {
        this(0, dni, nombre, licencia, categoria);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLicencia() {
        return licencia;
    }

    public void setLicencia(String licencia) {
        this.licencia = licencia;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public EstadoConductor getEstado() {
        return estado;
    }

    public void setEstado(EstadoConductor estado) {
        this.estado = estado;
    }

    public Vehiculo getVehiculoHabitual() {
        return vehiculoHabitual;
    }

    public void setVehiculoHabitual(Vehiculo vehiculoHabitual) {
        this.vehiculoHabitual = vehiculoHabitual;
    }

    @Override
    public String toString() {
        return nombre + " (" + licencia + ")";
    }
}
