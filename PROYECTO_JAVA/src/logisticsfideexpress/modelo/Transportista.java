/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.modelo;

/**
 *
 * @author JETLI
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import logisticsfideexpress.modelo.enums.TipoUsuario;

public class Transportista extends Usuario {

    private String ruc;
    private String razonSocial;
    private String telefono;
    private final List<VehiculoAfiliado> vehiculos = new ArrayList<>(); // agregacion

    public Transportista(int id, String nombre, String correo, String claveHash,
            String ruc, String razonSocial, String telefono) {
        super(id, nombre, correo, claveHash, TipoUsuario.TRANSPORTISTA);
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.telefono = telefono;
    }

    public Transportista(String nombre, String correo, String claveHash,
            String ruc, String razonSocial, String telefono) {
        this(0, nombre, correo, claveHash, ruc, razonSocial, telefono);
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }   

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<VehiculoAfiliado> getVehiculos() {
        return Collections.unmodifiableList(vehiculos);
    }

    public void agregarVehiculo(VehiculoAfiliado v) {
        if (!vehiculos.contains(v)) {
            vehiculos.add(v);
        }
    }

    @Override
    public String toString() {
        return razonSocial + " (" + ruc + ")";
    }
}
