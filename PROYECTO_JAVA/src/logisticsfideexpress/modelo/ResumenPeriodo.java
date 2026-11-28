/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.modelo;

/**
 *
 * @author JETLI
 */
/** Ingresos y egresos cobrados/pagados en un periodo (mes AAAA-MM). */
public class ResumenPeriodo {

    private final String periodo;
    private final double ingresos;
    private final double egresos;

    public ResumenPeriodo(String periodo, double ingresos, double egresos) {
        this.periodo = periodo;
        this.ingresos = ingresos;
        this.egresos = egresos;
    }

    public String getPeriodo() {
        return periodo;
    }

    public double getIngresos() {
        return ingresos;
    }

    public double getEgresos() {
        return egresos;
    }
}