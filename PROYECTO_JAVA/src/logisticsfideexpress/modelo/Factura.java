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
import logisticsfideexpress.modelo.enums.EstadoFactura;
import logisticsfideexpress.modelo.enums.TipoFactura;

public class Factura {

    public static final String TERCERO_CLIENTE = "CLIENTE";
    public static final String TERCERO_PROVEEDOR = "PROVEEDOR";
    public static final String TERCERO_TRANSPORTISTA = "TRANSPORTISTA";

    private int id;
    private String numero;
    private TipoFactura tipo;
    private LocalDate fecha;
    private String terceroTipo;   // CLIENTE, PROVEEDOR o TRANSPORTISTA
    private int terceroId;
    private String terceroNombre;
    private double total;
    private double saldo;
    private EstadoFactura estado = EstadoFactura.PENDIENTE;

    public Factura(int id, String numero, TipoFactura tipo, LocalDate fecha,
            String terceroTipo, int terceroId, String terceroNombre, double total) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.fecha = fecha;
        this.terceroTipo = terceroTipo;
        this.terceroId = terceroId;
        this.terceroNombre = terceroNombre;
        this.total = total;
        this.saldo = total;
    }

    public Factura(String numero, TipoFactura tipo, LocalDate fecha,
            String terceroTipo, int terceroId, String terceroNombre, double total) {
        this(0, numero, tipo, fecha, terceroTipo, terceroId, terceroNombre, total);
    }

    /**
     * RN06: el pago no supera el saldo; actualiza saldo y estado.
     */
    public void registrarPago(double monto) {
        if (monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        if (monto > saldo + 0.0001) {
            throw new IllegalArgumentException("El pago supera el saldo pendiente.");
        }
        saldo = Math.round((saldo - monto) * 100.0) / 100.0;
        estado = saldo <= 0.0 ? EstadoFactura.PAGADA : EstadoFactura.PARCIAL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public TipoFactura getTipo() {
        return tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getTerceroTipo() {
        return terceroTipo;
    }

    public int getTerceroId() {
        return terceroId;
    }

    public String getTerceroNombre() {
        return terceroNombre;
    }

    public double getTotal() {
        return total;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public void setEstado(EstadoFactura estado) {
        this.estado = estado;
    }
}
