/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.modelo;

/**
 *
 * @author JETLI
 */
import logisticsfideexpress.modelo.enums.EstadoVehiculo;

/**
 * Clase abstracta: cada hija responde distinto a getTipo() (polimorfismo).
 */
public abstract class Vehiculo {

    private int id;
    private String placa;
    private String marca;
    private double capacidadKg;
    private EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    protected Vehiculo(int id, String placa, String marca, double capacidadKg) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.capacidadKg = capacidadKg;
    }

    public abstract String getTipo();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public double getCapacidadKg() {
        return capacidadKg;
    }

    public void setCapacidadKg(double capacidadKg) {
        this.capacidadKg = capacidadKg;
    }

    public EstadoVehiculo getEstado() {
        return estado;
    }

    public void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return placa + " - " + marca;
    }
}
