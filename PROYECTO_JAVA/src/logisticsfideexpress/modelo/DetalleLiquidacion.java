/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.modelo;

/**
 *
 * @author JETLI
 */
/** Composicion: solo existe dentro de una Liquidacion. */
public class DetalleLiquidacion {

    private String concepto;
    private double monto;

    public DetalleLiquidacion(String concepto, double monto) {
        this.concepto = concepto;
        this.monto = monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public double getMonto() {
        return monto;
    }
}
