/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.servicio;

/**
 *
 * @author JETLI
 */


import logisticsfideexpress.dao.ClienteDAO;
import logisticsfideexpress.dao.ConductorDAO;
import logisticsfideexpress.dao.CostoDAO;
import logisticsfideexpress.dao.FacturaDAO;
import logisticsfideexpress.dao.LiquidacionDAO;
import logisticsfideexpress.dao.PagoDAO;
import logisticsfideexpress.dao.ProductoDAO;
import logisticsfideexpress.dao.ProveedorDAO;
import logisticsfideexpress.dao.SeguimientoDAO;
import logisticsfideexpress.dao.ServicioDAO;
import logisticsfideexpress.dao.TransportistaDAO;
import logisticsfideexpress.dao.UsuarioDAO;
import logisticsfideexpress.dao.VehiculoDAO;

/**
 * Fachada principal: crea todos los repositorios (cargan sus archivos de data/)
 * y todos los gestores. Main y las ventanas solo hablan con esta clase.
 * El orden de creacion importa: cada repositorio necesita los que referencia.
 */
public class ServiceFactory {

    private final UsuarioService usuarios;
    private final ClienteService clientes;
    private final TransportistaService transportistas;
    private final VehiculoService vehiculos;
    private final ConductorService conductores;
    private final AlmacenService almacen;
    private final ServicioService servicios;
    private final SeguimientoService seguimiento;
    private final CostoService costos;
    private final LiquidacionService liquidaciones;
    private final FacturacionService facturacion;
    private final ReporteService reportes;

    public ServiceFactory() {
        // --- semana 10: usuarios, flota y conductores
        UsuarioDAO usuarioRepo = new UsuarioDAO();
        ClienteDAO clienteRepo = new ClienteDAO(usuarioRepo);
        TransportistaDAO transportistaRepo = new TransportistaDAO(usuarioRepo);
        VehiculoDAO vehiculoRepo = new VehiculoDAO(transportistaRepo);
        ConductorDAO conductorRepo = new ConductorDAO(vehiculoRepo);

        // --- semana 12: almacen, servicios y seguimiento
        ProveedorDAO proveedorRepo = new ProveedorDAO();
        ProductoDAO productoRepo = new ProductoDAO(proveedorRepo);
        ServicioDAO servicioRepo = new ServicioDAO(clienteRepo, vehiculoRepo, conductorRepo);
        SeguimientoDAO seguimientoRepo = new SeguimientoDAO(servicioRepo);

        // --- semana 13: costos, liquidaciones, facturas y pagos
        CostoDAO costoRepo = new CostoDAO(servicioRepo);
        LiquidacionDAO liquidacionRepo = new LiquidacionDAO(servicioRepo);
        FacturaDAO facturaRepo = new FacturaDAO();
        PagoDAO pagoRepo = new PagoDAO(facturaRepo);

        this.clientes = new ClienteService(clienteRepo, usuarioRepo);
        this.usuarios = new UsuarioService(usuarioRepo, clientes);
        this.transportistas = new TransportistaService(transportistaRepo, usuarioRepo);
        this.vehiculos = new VehiculoService(vehiculoRepo, transportistaRepo);
        this.conductores = new ConductorService(conductorRepo, vehiculoRepo);
        this.almacen = new AlmacenService(productoRepo, proveedorRepo);
        this.servicios = new ServicioService(servicioRepo, clienteRepo, vehiculoRepo, conductorRepo, seguimientoRepo);
        this.seguimiento = new SeguimientoService(seguimientoRepo, servicioRepo, vehiculoRepo, conductorRepo);
        this.costos = new CostoService(costoRepo, servicioRepo);
        this.liquidaciones = new LiquidacionService(liquidacionRepo, servicioRepo, costoRepo);
        this.facturacion = new FacturacionService(facturaRepo, pagoRepo, servicioRepo, clienteRepo,
                proveedorRepo, transportistaRepo);
        this.reportes = new ReporteService(servicioRepo, vehiculoRepo, productoRepo, costoRepo,
                liquidacionRepo, facturaRepo);

        usuarios.asegurarAdministradorInicial();
    }

    public UsuarioService getUsuarios() { return usuarios; }
    public ClienteService getClientes() { return clientes; }
    public TransportistaService getTransportistas() { return transportistas; }
    public VehiculoService getVehiculos() { return vehiculos; }
    public ConductorService getConductores() { return conductores; }
    public AlmacenService getAlmacen() { return almacen; }
    public ServicioService getServicios() { return servicios; }
    public SeguimientoService getSeguimiento() { return seguimiento; }
    public CostoService getCostos() { return costos; }
    public LiquidacionService getLiquidaciones() { return liquidaciones; }
    public FacturacionService getFacturacion() { return facturacion; }
    public ReporteService getReportes() { return reportes; }
}