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
import java.util.List;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.RegistroDuplicadoException;
import logisticsfideexpress.excepciones.RegistroNoEncontradoException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.modelo.MovimientoAlmacen;
import logisticsfideexpress.modelo.Producto;
import logisticsfideexpress.modelo.Proveedor;
import logisticsfideexpress.modelo.enums.TipoMovimiento;
import logisticsfideexpress.repository.ProductoRepository;
import logisticsfideexpress.repository.ProveedorRepository;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.util.Validador;

/**
 * RF16-RF20 (productos y movimientos de almacen) y RF35 (proveedores). Solo
 * administrador.
 */
public class GestorAlmacen {

    private final ProductoRepository productos;
    private final ProveedorRepository proveedores;

    public GestorAlmacen(ProductoRepository productos, ProveedorRepository proveedores) {
        this.productos = productos;
        this.proveedores = proveedores;
    }

    // ---------------------------------------------------------------- proveedores
    public Proveedor registrarProveedor(String ruc, String razonSocial, String telefono)
            throws ValidacionException, RegistroDuplicadoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Validador.digitosExactos(ruc, 11, "RUC");
        Validador.noVacio(razonSocial, "Razon social");
        Validador.noVacio(telefono, "Telefono");
        if (proveedores.buscarPorRuc(ruc) != null) {
            throw new RegistroDuplicadoException("El RUC del proveedor ya esta registrado.");
        }
        Proveedor p = new Proveedor(ruc.trim(), razonSocial.trim(), telefono.trim());
        proveedores.guardar(p);
        return p;
    }

    public List<Proveedor> listarProveedores() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return proveedores.listar();
    }

    // Sobrecarga: filtro por texto (razon social o RUC)
    public List<Proveedor> listarProveedores(String texto) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return proveedores.buscar(texto);
    }

    // ---------------------------------------------------------------- productos
    /**
     * RF16: idProveedor = 0 si el producto no tiene proveedor asociado.
     */
    public Producto registrarProducto(String codigo, String nombre, String unidad, String stockInicial,
            String stockMinimo, int idProveedor)
            throws ValidacionException, RegistroDuplicadoException,
            RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Validador.noVacio(codigo, "Codigo");
        Validador.noVacio(nombre, "Nombre");
        Validador.noVacio(unidad, "Unidad");
        int stock = Validador.esVacio(stockInicial) ? 0 : enteroNoNegativo(stockInicial, "Stock inicial");
        int minimo = enteroNoNegativo(stockMinimo, "Stock minimo");
        if (productos.buscarPorCodigo(codigo) != null) {
            throw new RegistroDuplicadoException("El codigo del producto ya esta registrado.");
        }
        Producto p = new Producto(codigo.trim().toUpperCase(), nombre.trim(), unidad.trim(), stock, minimo);
        p.setProveedor(buscarProveedorOpcional(idProveedor));
        productos.guardar(p);
        if (stock > 0) {
            productos.guardarMovimiento(new MovimientoAlmacen(p, LocalDate.now(), TipoMovimiento.ENTRADA, stock));
        }
        return p;
    }

    /**
     * RF17: el stock no se edita aqui; cambia solo con entradas y salidas.
     */
    public void modificarProducto(int id, String nombre, String unidad, String stockMinimo, int idProveedor)
            throws ValidacionException, RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Producto p = requerirProducto(id);
        Validador.noVacio(nombre, "Nombre");
        Validador.noVacio(unidad, "Unidad");
        int minimo = enteroNoNegativo(stockMinimo, "Stock minimo");
        p.setNombre(nombre.trim());
        p.setUnidad(unidad.trim());
        p.setStockMinimo(minimo);
        p.setProveedor(buscarProveedorOpcional(idProveedor));
        productos.actualizar(p);
    }

    public List<Producto> listarProductos() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return productos.listar();
    }

    // Sobrecarga: filtro por texto (codigo o nombre)
    public List<Producto> listarProductos(String texto) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return productos.buscar(texto);
    }

    public Producto buscarProducto(int id) throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return requerirProducto(id);
    }

    /**
     * RF17: alerta de productos con stock igual o menor al minimo.
     */
    public List<Producto> productosBajoMinimo() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return productos.buscarBajoMinimo();
    }

    // -------------------------------------------------------------- movimientos
    /**
     * RF18
     */
    public MovimientoAlmacen registrarEntrada(int idProducto, String cantidad)
            throws ValidacionException, RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Producto p = requerirProducto(idProducto);
        int cant = enteroPositivo(cantidad, "Cantidad");
        p.setStock(p.getStock() + cant);
        productos.actualizar(p);
        MovimientoAlmacen m = new MovimientoAlmacen(p, LocalDate.now(), TipoMovimiento.ENTRADA, cant);
        productos.guardarMovimiento(m);
        return m;
    }

    /**
     * RF19: no se puede sacar mas de lo que hay en stock.
     */
    public MovimientoAlmacen registrarSalida(int idProducto, String cantidad)
            throws ValidacionException, RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Producto p = requerirProducto(idProducto);
        int cant = enteroPositivo(cantidad, "Cantidad");
        if (cant > p.getStock()) {
            throw new OperacionNoPermitidaException("Stock insuficiente: hay " + p.getStock()
                    + " " + p.getUnidad() + " de " + p.getNombre() + ".");
        }
        p.setStock(p.getStock() - cant);
        productos.actualizar(p);
        MovimientoAlmacen m = new MovimientoAlmacen(p, LocalDate.now(), TipoMovimiento.SALIDA, cant);
        productos.guardarMovimiento(m);
        return m;
    }

    /**
     * RF20: historial completo.
     */
    public List<MovimientoAlmacen> listarMovimientos() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return productos.listarMovimientos();
    }

    // Sobrecarga: historial de un producto
    public List<MovimientoAlmacen> listarMovimientos(int idProducto)
            throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        requerirProducto(idProducto);
        return productos.listarMovimientos(idProducto);
    }

    // ------------------------------------------------------------------ apoyo
    private Producto requerirProducto(int id) throws RegistroNoEncontradoException {
        Producto p = productos.buscarPorId(id);
        if (p == null) {
            throw new RegistroNoEncontradoException("No existe el producto solicitado.");
        }
        return p;
    }

    private Proveedor buscarProveedorOpcional(int idProveedor) throws RegistroNoEncontradoException {
        if (idProveedor <= 0) {
            return null;
        }
        Proveedor prov = proveedores.buscarPorId(idProveedor);
        if (prov == null) {
            throw new RegistroNoEncontradoException("El proveedor seleccionado no existe.");
        }
        return prov;
    }

    private int enteroPositivo(String texto, String campo) throws ValidacionException {
        int valor = Validador.aEntero(texto, campo);
        Validador.numeroPositivo(valor, campo);
        return valor;
    }

    private int enteroNoNegativo(String texto, String campo) throws ValidacionException {
        int valor = Validador.aEntero(texto, campo);
        Validador.numeroNoNegativo(valor, campo);
        return valor;
    }
}
