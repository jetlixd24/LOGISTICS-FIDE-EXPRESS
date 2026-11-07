/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.modelo;

/**
 *
 * @author JETLI
 */
public class Producto {

    private int id;
    private String codigo;
    private String nombre;
    private String unidad;
    private int stock;
    private int stockMinimo;
    private Proveedor proveedor; // opcional

    public Producto(int id, String codigo, String nombre, String unidad, int stock, int stockMinimo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.unidad = unidad;
        this.stock = stock;
        this.stockMinimo = stockMinimo;
    }

    public Producto(String codigo, String nombre, String unidad, int stock, int stockMinimo) {
        this(0, codigo, nombre, unidad, stock, stockMinimo);
    }

    public boolean estaBajoMinimo() {
        return stock <= stockMinimo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}
