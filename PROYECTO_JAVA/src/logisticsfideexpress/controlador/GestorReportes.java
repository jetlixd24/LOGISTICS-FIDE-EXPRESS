/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.controlador;

/**
 *
 * @author JETLI
 */
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.modelo.CostoViaje;
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.modelo.Liquidacion;
import logisticsfideexpress.modelo.Producto;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.enums.EstadoFactura;
import logisticsfideexpress.modelo.enums.EstadoGasto;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.modelo.enums.EstadoVehiculo;
import logisticsfideexpress.modelo.enums.TipoFactura;
import logisticsfideexpress.repository.CostoRepository;
import logisticsfideexpress.repository.FacturaRepository;
import logisticsfideexpress.repository.LiquidacionRepository;
import logisticsfideexpress.repository.ProductoRepository;
import logisticsfideexpress.repository.ServicioRepository;
import logisticsfideexpress.repository.VehiculoRepository;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.util.Validador;

/**
 * RF38-RF40: reportes de solo lectura (no modifica nada). Solo administrador.
 */
public class GestorReportes {

    private final ServicioRepository servicios;
    private final VehiculoRepository vehiculos;
    private final ProductoRepository productos;
    private final CostoRepository costos;
    private final LiquidacionRepository liquidaciones;
    private final FacturaRepository facturas;

    public GestorReportes(ServicioRepository servicios, VehiculoRepository vehiculos, ProductoRepository productos,
            CostoRepository costos, LiquidacionRepository liquidaciones, FacturaRepository facturas) {
        this.servicios = servicios;
        this.vehiculos = vehiculos;
        this.productos = productos;
        this.costos = costos;
        this.liquidaciones = liquidaciones;
        this.facturas = facturas;
    }

    /**
     * RF38: cantidad de servicios por estado.
     */
    public Map<EstadoServicio, Integer> serviciosPorEstado() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return contar(servicios.listar());
    }

    // Sobrecarga: servicios solicitados entre dos fechas (AAAA-MM-DD)
    public Map<EstadoServicio, Integer> serviciosPorEstado(String desde, String hasta)
            throws ValidacionException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        LocalDate d = Validador.aFecha(desde, "Fecha desde");
        LocalDate h = Validador.aFecha(hasta, "Fecha hasta");
        if (h.isBefore(d)) {
            throw new ValidacionException("La fecha final no puede ser anterior a la inicial.");
        }
        List<ServicioTransporte> filtrados = new java.util.ArrayList<>();
        for (ServicioTransporte s : servicios.listar()) {
            if (!s.getFechaSolicitud().isBefore(d) && !s.getFechaSolicitud().isAfter(h)) {
                filtrados.add(s);
            }
        }
        return contar(filtrados);
    }

    private Map<EstadoServicio, Integer> contar(List<ServicioTransporte> lista) {
        Map<EstadoServicio, Integer> mapa = new EnumMap<>(EstadoServicio.class);
        for (EstadoServicio e : EstadoServicio.values()) {
            mapa.put(e, 0);
        }
        for (ServicioTransporte s : lista) {
            mapa.merge(s.getEstado(), 1, Integer::sum);
        }
        return mapa;
    }

    /**
     * RF38: estado de la flota (vehiculos por estado).
     */
    public Map<EstadoVehiculo, Integer> flotaPorEstado() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Map<EstadoVehiculo, Integer> mapa = new EnumMap<>(EstadoVehiculo.class);
        for (EstadoVehiculo e : EstadoVehiculo.values()) {
            mapa.put(e, 0);
        }
        for (Vehiculo v : vehiculos.listar()) {
            mapa.merge(v.getEstado(), 1, Integer::sum);
        }
        return mapa;
    }

    /**
     * RF38: productos con stock bajo el minimo.
     */
    public List<Producto> productosBajoMinimo() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return productos.buscarBajoMinimo();
    }

    /**
     * RF39: total de gastos APROBADOS de todos los servicios.
     */
    public double totalGastosAprobados() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        double total = 0;
        for (CostoViaje c : costos.buscar(EstadoGasto.APROBADO)) {
            total += c.getMonto();
        }
        return redondear(total);
    }

    /**
     * RF39: utilidad acumulada de las liquidaciones.
     */
    public double utilidadTotal() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return redondear(sumarUtilidad(liquidaciones.listar()));
    }

    // Sobrecarga: utilidad entre dos fechas (AAAA-MM-DD)
    public double utilidadTotal(String desde, String hasta) throws ValidacionException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        LocalDate d = Validador.aFecha(desde, "Fecha desde");
        LocalDate h = Validador.aFecha(hasta, "Fecha hasta");
        if (h.isBefore(d)) {
            throw new ValidacionException("La fecha final no puede ser anterior a la inicial.");
        }
        return redondear(sumarUtilidad(liquidaciones.buscar(d, h)));
    }

    private double sumarUtilidad(List<Liquidacion> lista) {
        double total = 0;
        for (Liquidacion l : lista) {
            total += l.getUtilidad();
        }
        return total;
    }

    /**
     * RF40: facturas con saldo pendiente (PENDIENTE o PARCIAL), de venta y de
     * compra.
     */
    public List<Factura> facturasConSaldo() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        List<Factura> lista = facturas.buscar(EstadoFactura.PENDIENTE);
        lista.addAll(facturas.buscar(EstadoFactura.PARCIAL));
        return lista;
    }

    /**
     * RF40: cuentas por cobrar (saldo de las facturas de venta).
     */
    public double cuentasPorCobrar() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return redondear(sumarSaldo(facturas.buscar(TipoFactura.VENTA)));
    }

    /**
     * RF40: cuentas por pagar (saldo de las facturas de compra).
     */
    public double cuentasPorPagar() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return redondear(sumarSaldo(facturas.buscar(TipoFactura.COMPRA)));
    }

    private double sumarSaldo(List<Factura> lista) {
        double total = 0;
        for (Factura f : lista) {
            total += f.getSaldo();
        }
        return total;
    }

    /**
     * Texto listo para imprimir en el menu de consola o mostrar en FrmReportes.
     */
    public String resumenGeneral() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE GENERAL ===\n");
        sb.append("Servicios por estado:\n");
        for (Map.Entry<EstadoServicio, Integer> e : serviciosPorEstado().entrySet()) {
            sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append('\n');
        }
        sb.append("Flota por estado:\n");
        for (Map.Entry<EstadoVehiculo, Integer> e : flotaPorEstado().entrySet()) {
            sb.append("  ").append(e.getKey()).append(": ").append(e.getValue()).append('\n');
        }
        sb.append("Productos bajo stock minimo: ").append(productosBajoMinimo().size()).append('\n');
        sb.append(String.format(Locale.US, "Gastos aprobados: S/ %.2f%n", totalGastosAprobados()));
        sb.append(String.format(Locale.US, "Utilidad liquidada: S/ %.2f%n", utilidadTotal()));
        sb.append(String.format(Locale.US, "Cuentas por cobrar: S/ %.2f%n", cuentasPorCobrar()));
        sb.append(String.format(Locale.US, "Cuentas por pagar: S/ %.2f%n", cuentasPorPagar()));
        return sb.toString();
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
