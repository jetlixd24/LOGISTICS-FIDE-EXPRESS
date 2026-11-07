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
public class Administrador extends Usuario {

    private String cargo;

    public Administrador(int id, String nombre, String correo, String claveHash, String cargo) {
        super(id, nombre, correo, claveHash, TipoUsuario.ADMINISTRADOR);
        this.cargo = cargo;
    }

    public Administrador(String nombre, String correo, String claveHash, String cargo) {
        this(0, nombre, correo, claveHash, cargo);
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}
