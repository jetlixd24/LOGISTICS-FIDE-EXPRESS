/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.modelo;

import logisticsfideexpress.modelo.enums.TipoUsuario;

/**
 *
 * @author JETLI
 */
public class Cliente extends Usuario {

    private String documento;
    private String razonSocial;
    private String telefono;
    private String direccion;

    public Cliente(int id, String nombre, String correo, String claveHash,
            String documento, String razonSocial, String telefono, String direccion) {
        super(id, nombre, correo, claveHash, TipoUsuario.CLIENTE);
        this.documento = documento;
        this.razonSocial = razonSocial;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public Cliente(String nombre, String correo, String claveHash,
            String documento, String razonSocial, String telefono, String direccion) {
        this(0, nombre, correo, claveHash, documento, razonSocial, telefono, direccion);
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @Override
    public String toString() {
        return razonSocial + " (" + documento + ")";
    }
}
