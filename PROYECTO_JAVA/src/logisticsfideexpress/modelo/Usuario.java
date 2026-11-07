/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.modelo;

import logisticsfideexpress.modelo.enums.EstadoUsuario;
import logisticsfideexpress.modelo.enums.TipoUsuario;

/**
 *
 * @author JETLI
 */
public class Usuario {
    private int id;
    private String nombre;
    private String correo;
    private String claveHash;
    private TipoUsuario tipo;
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;
    private Usuario registradoPor;

    protected Usuario(int id, String nombre, String correo, String claveHash, TipoUsuario tipo) {

        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.claveHash = claveHash;
        this.tipo = tipo;

    }

    protected Usuario(String nombre, String correo, String claveHash, TipoUsuario tipo){
    
    this(0, nombre, correo, claveHash, tipo);
    
    }
    
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public void setClaveHash(String calveHash) {
        this.claveHash = calveHash;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Usuario registradoPor) {
        this.registradoPor = registradoPor;
    }

    public boolean estaActivo() {
        return estado == EstadoUsuario.ACTIVO;
    }

    @Override
    public String toString() {
        return nombre;
    }

    
}
