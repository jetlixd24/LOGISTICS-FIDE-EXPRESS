/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logisticsfideexpress.modelo.Administrador;
import logisticsfideexpress.modelo.Cliente;
import logisticsfideexpress.modelo.Transportista;
import logisticsfideexpress.modelo.Usuario;
import logisticsfideexpress.modelo.enums.EstadoUsuario;
import logisticsfideexpress.modelo.enums.TipoUsuario;
import logisticsfideexpress.util.ArchivoUtil;

/**
 * Guarda TODOS los usuarios (administradores, clientes y transportistas) en
 * data/usuarios.txt. Formato:
 * id;tipo;nombre;correo;claveHash;estado;registradoPorId;e1;e2;e3;e4
 * ADMINISTRADOR: e1=cargo CLIENTE: e1=documento e2=razonSocial e3=telefono
 * e4=direccion TRANSPORTISTA: e1=ruc e2=razonSocial e3=telefono
 */
public class UsuarioDAO extends RepositorioArchivo<Usuario> {

    private static final String RUTA = "data/usuarios.txt";
    private final Map<Usuario, Integer> pendientes = new HashMap<>();

    public UsuarioDAO() {
        super(RUTA);
        cargar();
        enlazarRegistradoPor();
    }

    private void enlazarRegistradoPor() {
        for (Map.Entry<Usuario, Integer> par : pendientes.entrySet()) {
            if (par.getValue() > 0) {
                par.getKey().setRegistradoPor(buscarPorId(par.getValue()));
            }
        }
        pendientes.clear();
    }

    @Override
    protected String aLinea(Usuario u) {
        String e1 = "", e2 = "", e3 = "", e4 = "";
        if (u instanceof Administrador a) {
            e1 = a.getCargo();
        } else if (u instanceof Cliente c) {
            e1 = c.getDocumento();
            e2 = c.getRazonSocial();
            e3 = c.getTelefono();
            e4 = c.getDireccion();
        } else if (u instanceof Transportista t) {
            e1 = t.getRuc();
            e2 = t.getRazonSocial();
            e3 = t.getTelefono();
        }
        String[] campos = {
            String.valueOf(u.getId()), u.getTipo().name(), u.getNombre(), u.getCorreo(),
            u.getClaveHash(), u.getEstado().name(),
            String.valueOf(u.getRegistradoPor() == null ? 0 : u.getRegistradoPor().getId()),
            e1, e2, e3, e4
        };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) {
                sb.append(ArchivoUtil.SEPARADOR);
            }
            sb.append(ArchivoUtil.limpiar(campos[i]));
        }
        return sb.toString();
    }

    @Override
    protected Usuario deLinea(String[] c) {
        int id = Integer.parseInt(c[0]);
        TipoUsuario tipo = TipoUsuario.valueOf(c[1]);
        Usuario u;
        switch (tipo) {
            case ADMINISTRADOR ->
                u = new Administrador(id, c[2], c[3], c[4], c[7]);
            case CLIENTE ->
                u = new Cliente(id, c[2], c[3], c[4], c[7], c[8], c[9], c[10]);
            default ->
                u = new Transportista(id, c[2], c[3], c[4], c[7], c[8], c[9]);
        }
        u.setEstado(EstadoUsuario.valueOf(c[5]));
        pendientes.put(u, Integer.parseInt(c[6]));
        return u;
    }

    @Override
    protected int idDe(Usuario u) {
        return u.getId();
    }

    @Override
    protected void fijarId(Usuario u, int id) {
        u.setId(id);
    }

    @Override
    protected void marcarBaja(Usuario u) {
        u.setEstado(EstadoUsuario.INACTIVO);
    }

    public Usuario buscarPorCorreo(String correo) {
        if (correo == null) {
            return null;
        }
        for (Usuario u : items) {
            if (u.getCorreo().equalsIgnoreCase(correo.trim())) {
                return u;
            }
        }
        return null;
    }

    // Sobrecarga: busqueda por texto (nombre o correo)
    public List<Usuario> buscar(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        List<Usuario> resultado = new ArrayList<>();
        for (Usuario u : items) {
            if (u.getNombre().toLowerCase().contains(t) || u.getCorreo().toLowerCase().contains(t)) {
                resultado.add(u);
            }
        }
        return resultado;
    }
}
