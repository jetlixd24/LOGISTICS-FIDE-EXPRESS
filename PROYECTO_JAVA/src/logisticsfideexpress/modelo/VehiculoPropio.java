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

public class VehiculoPropio extends Vehiculo {

    private double kilometraje;
    private LocalDate fechaCompra;

    public VehiculoPropio(int id, String placa, String marca, double capacidadKg,
            double kilometraje, LocalDate fechaCompra) {
        super(id, placa, marca, capacidadKg);
        this.kilometraje = kilometraje;
        this.fechaCompra = fechaCompra;
    }

    public VehiculoPropio(String placa, String marca, double capacidadKg,
            double kilometraje, LocalDate fechaCompra) {
        this(0, placa, marca, capacidadKg, kilometraje, fechaCompra);
    }

    @Override
    public String getTipo() {
        return "PROPIO";
    }

    public double getKilometraje() {
        return kilometraje;
    }

    public void setKilometraje(double kilometraje) {
        this.kilometraje = kilometraje;
    }

    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDate fechaCompra) {
        this.fechaCompra = fechaCompra;
    }
}
