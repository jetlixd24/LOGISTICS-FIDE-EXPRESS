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
import logisticsfideexpress.modelo.Transportista;
import logisticsfideexpress.modelo.Usuario;

/**
 * Vista especializada de UsuarioRepository (los transportistas tambien son
 * usuarios).
 */
public class TransportistaRepository implements IRepositorio<Transportista> {

    private final UsuarioRepository usuarios;

    public TransportistaRepository(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public void guardar(Transportista t) {
        usuarios.guardar(t);
    }

    @Override
    public Transportista buscarPorId(int id) {
        Usuario u = usuarios.buscarPorId(id);
        return (u instanceof Transportista t) ? t : null;
    }

    @Override
    public List<Transportista> listar() {
        List<Transportista> lista = new ArrayList<>();
        for (Usuario u : usuarios.listar()) {
            if (u instanceof Transportista t) {
                lista.add(t);
            }
        }
        return lista;
    }

    @Override
    public void actualizar(Transportista t) {
        usuarios.actualizar(t);
    }

    @Override
    public void darDeBaja(int id) {
        usuarios.darDeBaja(id);
    }

    @Override
    public int siguienteId() {
        return usuarios.siguienteId();
    }

    public Transportista buscarPorRuc(String ruc) {
        if (ruc == null) {
            return null;
        }
        for (Transportista t : listar()) {
            if (t.getRuc().equals(ruc.trim())) {
                return t;
            }
        }
        return null;
    }

    public List<Transportista> buscar(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        List<Transportista> resultado = new ArrayList<>();
        for (Transportista x : listar()) {
            if (x.getRazonSocial().toLowerCase().contains(t) || x.getRuc().contains(t)) {
                resultado.add(x);
            }
        }
        return resultado;
    }
}
