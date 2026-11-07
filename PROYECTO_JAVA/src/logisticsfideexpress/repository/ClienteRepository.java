/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.repository;

/**
 *
 * @author JETLI
 */
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.modelo.Cliente;
import logisticsfideexpress.modelo.Usuario;

/**
 * Vista especializada de UsuarioRepository: los clientes son usuarios
 * (herencia), por eso se guardan en data/usuarios.txt y no se duplican datos.
 */
public class ClienteRepository implements IRepositorio<Cliente> {

    private final UsuarioRepository usuarios;

    public ClienteRepository(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public void guardar(Cliente c) {
        usuarios.guardar(c);
    }

    @Override
    public Cliente buscarPorId(int id) {
        Usuario u = usuarios.buscarPorId(id);
        return (u instanceof Cliente c) ? c : null;
    }

    @Override
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        for (Usuario u : usuarios.listar()) {
            if (u instanceof Cliente c) {
                lista.add(c);
            }
        }
        return lista;
    }

    @Override
    public void actualizar(Cliente c) {
        usuarios.actualizar(c);
    }

    @Override
    public void darDeBaja(int id) {
        usuarios.darDeBaja(id);
    }

    @Override
    public int siguienteId() {
        return usuarios.siguienteId();
    }

    public Cliente buscarPorDocumento(String documento) {
        if (documento == null) {
            return null;
        }
        for (Cliente c : listar()) {
            if (c.getDocumento().equalsIgnoreCase(documento.trim())) {
                return c;
            }
        }
        return null;
    }

    // Sobrecarga: busqueda por texto (nombre, razon social o documento)
    public List<Cliente> buscar(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        List<Cliente> resultado = new ArrayList<>();
        for (Cliente c : listar()) {
            if (c.getNombre().toLowerCase().contains(t)
                    || c.getRazonSocial().toLowerCase().contains(t)
                    || c.getDocumento().toLowerCase().contains(t)) {
                resultado.add(c);
            }
        }
        return resultado;
    }
}
