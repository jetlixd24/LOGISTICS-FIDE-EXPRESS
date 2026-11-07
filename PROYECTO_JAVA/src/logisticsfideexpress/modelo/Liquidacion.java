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

public class Liquidacion {

    private int id;
    private ServicioTransporte servicio;
    private LocalDate fecha;
    private double totalGastos;
    private double utilidad;
    private final List<DetalleLiquidacion> detalles = new ArrayList<>(); // composicion

    public Liquidacion(int id, ServicioTransporte servicio, LocalDate fecha) {
        this.id = id;
        this.servicio = servicio;
        this.fecha = fecha;
    }

    public Liquidacion(ServicioTransporte servicio, LocalDate fecha) {
        this(0, servicio, fecha);
    }

    public void agregarDetalle(String concepto, double monto) {
        detalles.add(new DetalleLiquidacion(concepto, monto));
    }

    /**
     * RN05: utilidad = ingreso del servicio - suma de gastos aprobados.
     */
    public void calcular(double ingreso) {
        double suma = 0;
        for (DetalleLiquidacion d : detalles) {
            suma += d.getMonto();
        }
        totalGastos = Math.round(suma * 100.0) / 100.0;
        utilidad = Math.round((ingreso - totalGastos) * 100.0) / 100.0;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public double getTotalGastos() {
        return totalGastos;
    }

    public double getUtilidad() {
        return utilidad;
    }

    public List<DetalleLiquidacion> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }
}
