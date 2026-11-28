/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.servicio;

/**
 *
 * @author JETLI
 */
import java.util.List;
import java.util.UUID;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.RegistroDuplicadoException;
import logisticsfideexpress.excepciones.RegistroNoEncontradoException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.modelo.Transportista;
import logisticsfideexpress.dao.TransportistaDAO;
import logisticsfideexpress.dao.UsuarioDAO;
import logisticsfideexpress.util.SeguridadUtil;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.util.Validador;

/**
 * RF08-RF09. El transportista no tiene panel propio: lo registra el
 * administrador.
 */
public class TransportistaService {

    private final TransportistaDAO repo;
    private final UsuarioDAO usuarios;

    public TransportistaService(TransportistaDAO repo, UsuarioDAO usuarios) {
        this.repo = repo;
        this.usuarios = usuarios;
    }

    public Transportista registrar(String nombre, String correo, String ruc,
            String razonSocial, String telefono)
            throws ValidacionException, RegistroDuplicadoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Validador.noVacio(nombre, "Nombre de contacto");
        Validador.correoValido(correo);
        Validador.digitosExactos(ruc, 11, "RUC");
        Validador.noVacio(razonSocial, "Razon social");
        Validador.noVacio(telefono, "Telefono");
        if (repo.buscarPorRuc(ruc) != null) {
            throw new RegistroDuplicadoException("El RUC ya esta registrado.");
        }
        if (usuarios.buscarPorCorreo(correo) != null) {
            throw new RegistroDuplicadoException("El correo ya esta registrado.");
        }
        // Sin panel propio: clave aleatoria, nadie puede iniciar sesion con esta cuenta.
        Transportista t = new Transportista(nombre.trim(), correo.trim().toLowerCase(),
                SeguridadUtil.hash(UUID.randomUUID().toString()),
                ruc.trim(), razonSocial.trim(), telefono.trim());
        t.setRegistradoPor(Sesion.getUsuario());
        repo.guardar(t);
        return t;
    }

    public List<Transportista> listar() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.listar();
    }

    // Sobrecarga: con filtro (razon social o RUC)
    public List<Transportista> listar(String texto) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.buscar(texto);
    }

    public Transportista buscarPorId(int id) throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Transportista t = repo.buscarPorId(id);
        if (t == null) {
            throw new RegistroNoEncontradoException("No existe el transportista solicitado.");
        }
        return t;
    }
}
