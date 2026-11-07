/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.modelo;

/**
 *
 * @author JETLI
 */
public class VehiculoAfiliado extends Vehiculo {

    private double tarifaAcordada;
    private Transportista transportista;

    public VehiculoAfiliado(int id, String placa, String marca, double capacidadKg,
            double tarifaAcordada, Transportista transportista) {
        super(id, placa, marca, capacidadKg);
        this.tarifaAcordada = tarifaAcordada;
        this.transportista = transportista;
    }

    public VehiculoAfiliado(String placa, String marca, double capacidadKg,
            double tarifaAcordada, Transportista transportista) {
        this(0, placa, marca, capacidadKg, tarifaAcordada, transportista);
    }

    @Override
    public String getTipo() {
        return "AFILIADO";
    }

    public double getTarifaAcordada() {
        return tarifaAcordada;
    }

    public void setTarifaAcordada(double tarifaAcordada) {
        this.tarifaAcordada = tarifaAcordada;
    }

    public Transportista getTransportista() {
        return transportista;
    }

    public void setTransportista(Transportista transportista) {
        this.transportista = transportista;
    }
}
