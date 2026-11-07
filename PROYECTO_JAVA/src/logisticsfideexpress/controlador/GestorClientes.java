/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.controlador;

/**
 *
 * @author JETLI
 */
import java.util.List;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.RegistroDuplicadoException;
import logisticsfideexpress.excepciones.RegistroNoEncontradoException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.modelo.Cliente;
import logisticsfideexpress.modelo.Usuario;
import logisticsfideexpress.repository.ClienteRepository;
import logisticsfideexpress.repository.UsuarioRepository;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.util.SeguridadUtil;
import logisticsfideexpress.util.Validador;

/**
 * RF05-RF07 (y el registro propio del cliente, RF02).
 */
public class GestorClientes {

    private final ClienteRepository repo;
    private final UsuarioRepository usuarios;

    public GestorClientes(ClienteRepository repo, UsuarioRepository usuarios) {
        this.repo = repo;
        this.usuarios = usuarios;
    }

    /**
     * RF05: el administrador registra un cliente.
     */
    public Cliente registrar(String nombre, String correo, String clave, String documento,
            String razonSocial, String telefono, String direccion)
            throws ValidacionException, RegistroDuplicadoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Cliente c = crear(nombre, correo, clave, documento, razonSocial, telefono, direccion);
        c.setRegistradoPor(Sesion.getUsuario());
        repo.guardar(c);
        return c;
    }

    /**
     * RF02 (CU21): el cliente se registra solo; no requiere sesion.
     */
    public Cliente registrarse(String nombre, String correo, String clave, String documento,
            String razonSocial, String telefono, String direccion)
            throws ValidacionException, RegistroDuplicadoException {
        Cliente c = crear(nombre, correo, clave, documento, razonSocial, telefono, direccion);
        repo.guardar(c);
        return c;
    }

    private Cliente crear(String nombre, String correo, String clave, String documento,
            String razonSocial, String telefono, String direccion)
            throws ValidacionException, RegistroDuplicadoException {
        Validador.noVacio(nombre, "Nombre");
        Validador.correoValido(correo);
        Validador.longitudMinima(clave, 6, "Contrasena");
        Validador.noVacio(documento, "Documento");
        Validador.noVacio(razonSocial, "Razon social");
        Validador.noVacio(telefono, "Telefono");
        Validador.noVacio(direccion, "Direccion");
        if (usuarios.buscarPorCorreo(correo) != null) {
            throw new RegistroDuplicadoException("El correo ya esta registrado.");
        }
        if (repo.buscarPorDocumento(documento) != null) {
            throw new RegistroDuplicadoException("El documento ya esta registrado.");
        }
        return new Cliente(nombre.trim(), correo.trim().toLowerCase(), SeguridadUtil.hash(clave),
                documento.trim(), razonSocial.trim(), telefono.trim(), direccion.trim());
    }

    /**
     * RF06: el administrador consulta todos los clientes.
     */
    public List<Cliente> listar() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.listar();
    }

    // Sobrecarga: consulta con filtro de texto (nombre, razon social o documento)
    public List<Cliente> listar(String texto) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.buscar(texto);
    }

    public Cliente buscarPorId(int id) throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Cliente c = repo.buscarPorId(id);
        if (c == null) {
            throw new RegistroNoEncontradoException("No existe el cliente solicitado.");
        }
        return c;
    }

    /**
     * RF06 / RN07: el cliente solo ve sus propios datos.
     */
    public Cliente obtenerMisDatos() throws OperacionNoPermitidaException {
        Sesion.exigirCliente();
        return repo.buscarPorId(Sesion.getUsuario().getId());
    }

    /**
     * RF07: el administrador modifica los datos de un cliente.
     */
    public void modificar(int id, String nombre, String documento, String razonSocial,
            String telefono, String direccion)
            throws ValidacionException, RegistroDuplicadoException,
            RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Cliente c = repo.buscarPorId(id);
        if (c == null) {
            throw new RegistroNoEncontradoException("No existe el cliente solicitado.");
        }
        Validador.noVacio(nombre, "Nombre");
        Validador.noVacio(documento, "Documento");
        Validador.noVacio(razonSocial, "Razon social");
        Validador.noVacio(telefono, "Telefono");
        Validador.noVacio(direccion, "Direccion");
        Cliente otro = repo.buscarPorDocumento(documento);
        if (otro != null && otro.getId() != id) {
            throw new RegistroDuplicadoException("El documento ya esta registrado.");
        }
        c.setNombre(nombre.trim());
        c.setDocumento(documento.trim());
        c.setRazonSocial(razonSocial.trim());
        c.setTelefono(telefono.trim());
        c.setDireccion(direccion.trim());
        repo.actualizar(c);
    }

    /**
     * RF07 (CU24): el cliente actualiza telefono, direccion y, opcionalmente,
     * su clave.
     */
    public void actualizarMisDatos(String telefono, String direccion, String nuevaClave)
            throws ValidacionException, OperacionNoPermitidaException {
        Sesion.exigirCliente();
        Validador.noVacio(telefono, "Telefono");
        Validador.noVacio(direccion, "Direccion");
        Cliente c = repo.buscarPorId(Sesion.getUsuario().getId());
        c.setTelefono(telefono.trim());
        c.setDireccion(direccion.trim());
        if (!Validador.esVacio(nuevaClave)) {
            Validador.longitudMinima(nuevaClave, 6, "Contrasena");
            c.setClaveHash(SeguridadUtil.hash(nuevaClave));
        }
        repo.actualizar(c);
    }

    public void darDeBaja(int id) throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        if (repo.buscarPorId(id) == null) {
            throw new RegistroNoEncontradoException("No existe el cliente solicitado.");
        }
        repo.darDeBaja(id);
    }

    /**
     * Usado por GestorUsuarios para evitar duplicar el codigo de login.
     */
    Usuario usuarioPorCorreo(String correo) {
        return usuarios.buscarPorCorreo(correo);
    }
}
