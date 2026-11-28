/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.servicio;

import logisticsfideexpress.dao.interfaz.IUsuarioDAO;
/**
 *
 * @author JETLI
 */
import java.util.List;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.RegistroDuplicadoException;
import logisticsfideexpress.excepciones.RegistroNoEncontradoException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.modelo.Administrador;
import logisticsfideexpress.modelo.Cliente;
import logisticsfideexpress.modelo.Usuario;
import logisticsfideexpress.modelo.enums.EstadoUsuario;
import logisticsfideexpress.modelo.enums.TipoUsuario;
import logisticsfideexpress.util.SeguridadUtil;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.util.Validador;

/**
 * RF01-RF04: login, cierre de sesion, registro de usuarios y permisos.
 */
public class UsuarioService {

    public static final String ADMIN_CORREO = "admin@fide.com";
    public static final String ADMIN_CLAVE = "admin123";

    private final IUsuarioDAO repo;
    private final ClienteService gestorClientes;

    public UsuarioService(IUsuarioDAO repo, ClienteService gestorClientes) {
        this.repo = repo;
        this.gestorClientes = gestorClientes;
    }

    /**
     * Si no hay usuarios, crea el administrador inicial para poder entrar la
     * primera vez.
     */
    public void asegurarAdministradorInicial() {
        if (repo.listar().isEmpty()) {
            repo.guardar(new Administrador("Administrador", ADMIN_CORREO,
                    SeguridadUtil.hash(ADMIN_CLAVE), "Gerente general"));
        }
    }

    /**
     * RF01 + RF03: valida credenciales, guarda la sesion y devuelve el usuario
     * (con su rol).
     */
    public Usuario login(String correo, String clave)
            throws ValidacionException, OperacionNoPermitidaException {
        Validador.noVacio(correo, "Correo");
        Validador.noVacio(clave, "Contrasena");
        Usuario u = repo.buscarPorCorreo(correo);
        if (u == null || !SeguridadUtil.verificar(clave, u.getClaveHash())) {
            throw new ValidacionException("Correo o contrasena incorrectos.");
        }
        if (!u.estaActivo()) {
            throw new OperacionNoPermitidaException("El usuario esta inactivo.");
        }
        if (u.getTipo() == TipoUsuario.TRANSPORTISTA) {
            throw new OperacionNoPermitidaException("El transportista no tiene acceso en esta version.");
        }
        Sesion.iniciar(u);
        return u;
    }

    /**
     * RF04
     */
    public void cerrarSesion() {
        Sesion.cerrar();
    }

    /**
     * RF02 (CU02): el administrador crea otro administrador.
     */
    public Administrador crearAdministrador(String nombre, String correo, String clave, String cargo)
            throws ValidacionException, RegistroDuplicadoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Validador.noVacio(nombre, "Nombre");
        Validador.correoValido(correo);
        Validador.longitudMinima(clave, 6, "Contrasena");
        Validador.noVacio(cargo, "Cargo");
        if (repo.buscarPorCorreo(correo) != null) {
            throw new RegistroDuplicadoException("El correo ya esta registrado.");
        }
        Administrador a = new Administrador(nombre.trim(), correo.trim().toLowerCase(),
                SeguridadUtil.hash(clave), cargo.trim());
        a.setRegistradoPor(Sesion.getUsuario());
        repo.guardar(a);
        return a;
    }

    /**
     * RF02 (CU21): registro propio del cliente (FrmRegistroCliente).
     */
    public Cliente registrarCliente(String nombre, String correo, String clave, String documento,
            String razonSocial, String telefono, String direccion)
            throws ValidacionException, RegistroDuplicadoException {
        return gestorClientes.registrarse(nombre, correo, clave, documento, razonSocial, telefono, direccion);
    }

    public List<Usuario> listar() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.listar();
    }

    // Sobrecarga: con filtro de texto
    public List<Usuario> listar(String texto) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.buscar(texto);
    }

    /**
     * Baja logica (RF03): no se elimina el usuario; tampoco puedes desactivarte
     * a ti mismo.
     */
    public void cambiarEstado(int id, EstadoUsuario estado)
            throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Usuario u = repo.buscarPorId(id);
        if (u == null) {
            throw new RegistroNoEncontradoException("No existe el usuario solicitado.");
        }
        if (u.getId() == Sesion.getUsuario().getId() && estado == EstadoUsuario.INACTIVO) {
            throw new OperacionNoPermitidaException("No puedes desactivar tu propio usuario.");
        }
        u.setEstado(estado);
        repo.actualizar(u);
    }
}
