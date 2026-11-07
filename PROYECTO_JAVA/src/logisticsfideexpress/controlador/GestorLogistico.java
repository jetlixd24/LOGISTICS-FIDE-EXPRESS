/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.controlador;

/**
 *
 * @author JETLI
 */


import logisticsfideexpress.repository.ClienteRepository;
import logisticsfideexpress.repository.ConductorRepository;
import logisticsfideexpress.repository.CostoRepository;
import logisticsfideexpress.repository.FacturaRepository;
import logisticsfideexpress.repository.LiquidacionRepository;
import logisticsfideexpress.repository.PagoRepository;
import logisticsfideexpress.repository.ProductoRepository;
import logisticsfideexpress.repository.ProveedorRepository;
import logisticsfideexpress.repository.SeguimientoRepository;
import logisticsfideexpress.repository.ServicioRepository;
import logisticsfideexpress.repository.TransportistaRepository;
import logisticsfideexpress.repository.UsuarioRepository;
import logisticsfideexpress.repository.VehiculoRepository;

/**
 * Fachada principal: crea todos los repositorios (cargan sus archivos de data/)
 * y todos los gestores. Main y las ventanas solo hablan con esta clase.
 * El orden de creacion importa: cada repositorio necesita los que referencia.
 */
public class GestorLogistico {

    private final GestorUsuarios usuarios;
    private final GestorClientes clientes;
    private final GestorTransportistas transportistas;
    private final GestorVehiculos vehiculos;
    private final GestorConductores conductores;
    private final GestorAlmacen almacen;
    private final GestorServicios servicios;
    private final GestorSeguimiento seguimiento;
    private final GestorCostos costos;
    private final GestorLiquidaciones liquidaciones;
    private final GestorFacturacion facturacion;
    private final GestorReportes reportes;

    public GestorLogistico() {
        // --- semana 10: usuarios, flota y conductores
        UsuarioRepository usuarioRepo = new UsuarioRepository();
        ClienteRepository clienteRepo = new ClienteRepository(usuarioRepo);
        TransportistaRepository transportistaRepo = new TransportistaRepository(usuarioRepo);
        VehiculoRepository vehiculoRepo = new VehiculoRepository(transportistaRepo);
        ConductorRepository conductorRepo = new ConductorRepository(vehiculoRepo);

        // --- semana 12: almacen, servicios y seguimiento
        ProveedorRepository proveedorRepo = new ProveedorRepository();
        ProductoRepository productoRepo = new ProductoRepository(proveedorRepo);
        ServicioRepository servicioRepo = new ServicioRepository(clienteRepo, vehiculoRepo, conductorRepo);
        SeguimientoRepository seguimientoRepo = new SeguimientoRepository(servicioRepo);

        // --- semana 13: costos, liquidaciones, facturas y pagos
        CostoRepository costoRepo = new CostoRepository(servicioRepo);
        LiquidacionRepository liquidacionRepo = new LiquidacionRepository(servicioRepo);
        FacturaRepository facturaRepo = new FacturaRepository();
        PagoRepository pagoRepo = new PagoRepository(facturaRepo);

        this.clientes = new GestorClientes(clienteRepo, usuarioRepo);
        this.usuarios = new GestorUsuarios(usuarioRepo, clientes);
        this.transportistas = new GestorTransportistas(transportistaRepo, usuarioRepo);
        this.vehiculos = new GestorVehiculos(vehiculoRepo, transportistaRepo);
        this.conductores = new GestorConductores(conductorRepo, vehiculoRepo);
        this.almacen = new GestorAlmacen(productoRepo, proveedorRepo);
        this.servicios = new GestorServicios(servicioRepo, clienteRepo, vehiculoRepo, conductorRepo, seguimientoRepo);
        this.seguimiento = new GestorSeguimiento(seguimientoRepo, servicioRepo, vehiculoRepo, conductorRepo);
        this.costos = new GestorCostos(costoRepo, servicioRepo);
        this.liquidaciones = new GestorLiquidaciones(liquidacionRepo, servicioRepo, costoRepo);
        this.facturacion = new GestorFacturacion(facturaRepo, pagoRepo, servicioRepo, clienteRepo,
                proveedorRepo, transportistaRepo);
        this.reportes = new GestorReportes(servicioRepo, vehiculoRepo, productoRepo, costoRepo,
                liquidacionRepo, facturaRepo);

        usuarios.asegurarAdministradorInicial();
    }

    public GestorUsuarios getUsuarios() { return usuarios; }
    public GestorClientes getClientes() { return clientes; }
    public GestorTransportistas getTransportistas() { return transportistas; }
    public GestorVehiculos getVehiculos() { return vehiculos; }
    public GestorConductores getConductores() { return conductores; }
    public GestorAlmacen getAlmacen() { return almacen; }
    public GestorServicios getServicios() { return servicios; }
    public GestorSeguimiento getSeguimiento() { return seguimiento; }
    public GestorCostos getCostos() { return costos; }
    public GestorLiquidaciones getLiquidaciones() { return liquidaciones; }
    public GestorFacturacion getFacturacion() { return facturacion; }
    public GestorReportes getReportes() { return reportes; }
}