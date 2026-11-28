/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.servicio;

import logisticsfideexpress.dao.interfaz.IPagoDAO;
import logisticsfideexpress.dao.interfaz.IFacturaDAO;
import logisticsfideexpress.dao.interfaz.IProveedorDAO;
import logisticsfideexpress.dao.interfaz.ITransportistaDAO;
/**
 *
 * @author JETLI
 */
import java.time.LocalDate;
import java.util.List;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.RegistroDuplicadoException;
import logisticsfideexpress.excepciones.RegistroNoEncontradoException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.modelo.Cliente;
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.modelo.Pago;
import logisticsfideexpress.modelo.Proveedor;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.Transportista;
import logisticsfideexpress.modelo.enums.EstadoFactura;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.modelo.enums.TipoFactura;
import logisticsfideexpress.dao.interfaz.IClienteDAO;
import logisticsfideexpress.dao.interfaz.IServicioDAO;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.util.Validador;

/**
 * RF34-RF37: facturas de venta (al cliente), facturas de compra (proveedor o
 * transportista) y pagos. El cliente solo consulta sus propias facturas y pagos
 * (RN07).
 */
public class FacturacionService {

    private final IFacturaDAO facturas;
    private final IPagoDAO pagos;
    private final IServicioDAO servicios;
    private final IClienteDAO clientes;
    private final IProveedorDAO proveedores;
    private final ITransportistaDAO transportistas;

    public FacturacionService(IFacturaDAO facturas, IPagoDAO pagos, IServicioDAO servicios,
            IClienteDAO clientes, IProveedorDAO proveedores,
            ITransportistaDAO transportistas) {
        this.facturas = facturas;
        this.pagos = pagos;
        this.servicios = servicios;
        this.clientes = clientes;
        this.proveedores = proveedores;
        this.transportistas = transportistas;
    }

    // ------------------------------------------------------------- emision
    /**
     * RF34: factura de venta de un servicio ENTREGADO. El numero se arma con el
     * id del servicio (F001-000007), asi un servicio no puede facturarse dos
     * veces.
     */
    public Factura emitirFacturaVenta(int idServicio)
            throws RegistroNoEncontradoException, RegistroDuplicadoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        ServicioTransporte s = servicios.buscarPorId(idServicio);
        if (s == null) {
            throw new RegistroNoEncontradoException("No existe el servicio solicitado.");
        }
        if (s.getEstado() != EstadoServicio.ENTREGADO) {
            throw new OperacionNoPermitidaException("Solo se facturan servicios entregados. Estado actual: " + s.getEstado() + ".");
        }
        if (s.getTarifa() <= 0) {
            throw new OperacionNoPermitidaException("El servicio no tiene una tarifa valida.");
        }
        String numero = String.format("F001-%06d", s.getId());
        if (facturas.buscarPorNumero(numero) != null) {
            throw new RegistroDuplicadoException("El servicio ya fue facturado (" + numero + ").");
        }
        Cliente c = s.getCliente();
        Factura f = new Factura(numero, TipoFactura.VENTA, LocalDate.now(),
                Factura.TERCERO_CLIENTE, c.getId(), c.getRazonSocial(), s.getTarifa());
        facturas.guardar(f);
        return f;
    }

    /**
     * RF35: factura de compra recibida. terceroTipo = Factura.TERCERO_PROVEEDOR
     * o Factura.TERCERO_TRANSPORTISTA.
     */
    public Factura registrarFacturaCompra(String numero, String terceroTipo, int idTercero, String total)
            throws ValidacionException, RegistroDuplicadoException,
            RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Validador.noVacio(numero, "Numero de factura");
        Validador.noVacio(terceroTipo, "Tipo de tercero");
        double importe = Validador.numeroPositivo(total, "Total");

        String nombre;
        if (Factura.TERCERO_PROVEEDOR.equals(terceroTipo)) {
            Proveedor p = proveedores.buscarPorId(idTercero);
            if (p == null) {
                throw new RegistroNoEncontradoException("El proveedor seleccionado no existe.");
            }
            nombre = p.getRazonSocial();
        } else if (Factura.TERCERO_TRANSPORTISTA.equals(terceroTipo)) {
            Transportista t = transportistas.buscarPorId(idTercero);
            if (t == null) {
                throw new RegistroNoEncontradoException("El transportista seleccionado no existe.");
            }
            nombre = t.getRazonSocial();
        } else {
            throw new ValidacionException("El tercero de una factura de compra debe ser PROVEEDOR o TRANSPORTISTA.");
        }
        if (facturas.existe(numero, terceroTipo, idTercero)) {
            throw new RegistroDuplicadoException("Ya existe una factura con ese numero para ese tercero.");
        }
        Factura f = new Factura(numero.trim().toUpperCase(), TipoFactura.COMPRA, LocalDate.now(),
                terceroTipo, idTercero, nombre, importe);
        facturas.guardar(f);
        return f;
    }

    // --------------------------------------------------------------- pagos
    /**
     * RF37: registra un pago (total o parcial) y actualiza saldo y estado de la
     * factura.
     */
    public Pago registrarPago(int idFactura, String monto, String metodo)
            throws ValidacionException, RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Factura f = facturas.buscarPorId(idFactura);
        if (f == null) {
            throw new RegistroNoEncontradoException("No existe la factura solicitada.");
        }
        if (f.getEstado() == EstadoFactura.PAGADA) {
            throw new OperacionNoPermitidaException("La factura ya esta pagada.");
        }
        Validador.noVacio(metodo, "Metodo de pago");
        double importe = Validador.numeroPositivo(monto, "Monto");
        try {
            f.registrarPago(importe); // el modelo valida que no supere el saldo
        } catch (IllegalArgumentException e) {
            throw new ValidacionException(e.getMessage() + " Saldo pendiente: " + f.getSaldo() + ".");
        }
        Pago p = new Pago(f, LocalDate.now(), importe, metodo.trim());
        pagos.guardar(p);   // sp_registrar_pago: inserta el pago y actualiza saldo/estado
        return p;
    }

    // ----------------------------------------------- consultas del administrador
    /**
     * RF36
     */
    public List<Factura> listarFacturas() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return facturas.listar();
    }

    // Sobrecarga: por tipo (VENTA o COMPRA)
    public List<Factura> listarFacturas(TipoFactura tipo) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return facturas.buscar(tipo);
    }

    // Sobrecarga: por estado (PENDIENTE, PARCIAL, PAGADA)
    public List<Factura> listarFacturas(EstadoFactura estado) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return facturas.buscar(estado);
    }

    public List<Pago> listarPagos() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return pagos.listar();
    }

    // Sobrecarga: pagos de una factura
    public List<Pago> listarPagos(int idFactura) throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        if (facturas.buscarPorId(idFactura) == null) {
            throw new RegistroNoEncontradoException("No existe la factura solicitada.");
        }
        return pagos.buscarPorFactura(idFactura);
    }

    // ------------------------------------------------------- panel del cliente
    /**
     * FrmMisFacturas: solo las facturas de venta del cliente autenticado.
     */
    public List<Factura> misFacturas() throws OperacionNoPermitidaException {
        Sesion.exigirCliente();
        return facturas.buscarPorTercero(Factura.TERCERO_CLIENTE, Sesion.getUsuario().getId());
    }

    /**
     * FrmMisPagos: solo los pagos de las facturas del cliente autenticado.
     */
    public List<Pago> misPagos() throws OperacionNoPermitidaException {
        Sesion.exigirCliente();
        return pagos.buscarPorTercero(Factura.TERCERO_CLIENTE, Sesion.getUsuario().getId());
    }
}
