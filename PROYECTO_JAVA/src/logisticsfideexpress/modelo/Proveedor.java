/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.modelo;

/**
 *
 * @author JETLI
 */
public class Proveedor {

    private int id;
    private String ruc;
    private String razonSocial;
    private String telefono;

    public Proveedor(int id, String ruc, String razonSocial, String telefono) {
        this.id = id;
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.telefono = telefono;
    }

    public Proveedor(String ruc, String razonSocial, String telefono) {
        this(0, ruc, razonSocial, telefono);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public String toString() {
        return razonSocial + " (" + ruc + ")";
    }
}
