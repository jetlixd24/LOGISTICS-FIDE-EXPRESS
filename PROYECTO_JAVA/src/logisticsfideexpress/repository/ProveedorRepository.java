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
import logisticsfideexpress.modelo.Proveedor;
import logisticsfideexpress.util.ArchivoUtil;

/**
 * data/proveedores.txt Formato: id;ruc;razonSocial;telefono Los proveedores no
 * se eliminan: si ya tienen productos o facturas, se perderia el historial.
 */
public class ProveedorRepository extends RepositorioArchivo<Proveedor> {

    private static final String RUTA = "data/proveedores.txt";

    public ProveedorRepository() {
        super(RUTA);
        cargar();
    }

    @Override
    protected String aLinea(Proveedor p) {
        return String.join(ArchivoUtil.SEPARADOR,
                String.valueOf(p.getId()),
                ArchivoUtil.limpiar(p.getRuc()),
                ArchivoUtil.limpiar(p.getRazonSocial()),
                ArchivoUtil.limpiar(p.getTelefono()));
    }

    @Override
    protected Proveedor deLinea(String[] c) {
        return new Proveedor(Integer.parseInt(c[0]), c[1], c[2], c[3]);
    }

    @Override
    protected int idDe(Proveedor p) {
        return p.getId();
    }

    @Override
    protected void fijarId(Proveedor p, int id) {
        p.setId(id);
    }

    @Override
    protected void marcarBaja(Proveedor p) {
        throw new UnsupportedOperationException("Los proveedores no se dan de baja (se conserva el historial).");
    }

    public Proveedor buscarPorRuc(String ruc) {
        if (ruc == null) {
            return null;
        }
        for (Proveedor p : items) {
            if (p.getRuc().equalsIgnoreCase(ruc.trim())) {
                return p;
            }
        }
        return null;
    }

    // Sobrecarga: por texto (razon social o RUC)
    public List<Proveedor> buscar(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        List<Proveedor> resultado = new ArrayList<>();
        for (Proveedor p : items) {
            if (p.getRazonSocial().toLowerCase().contains(t) || p.getRuc().contains(t)) {
                resultado.add(p);
            }
        }
        return resultado;
    }
}
