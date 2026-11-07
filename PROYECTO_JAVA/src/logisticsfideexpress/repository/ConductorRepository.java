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
import logisticsfideexpress.modelo.Conductor;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.enums.EstadoConductor;
import logisticsfideexpress.util.ArchivoUtil;

/**
 * data/conductores.txt Formato:
 * id;dni;nombre;licencia;categoria;estado;idVehiculoHabitual (0 = ninguno)
 */
public class ConductorRepository extends RepositorioArchivo<Conductor> {

    private static final String RUTA = "data/conductores.txt";
    private final VehiculoRepository vehiculos;

    public ConductorRepository(VehiculoRepository vehiculos) {
        super(RUTA);
        this.vehiculos = vehiculos;
        cargar();
    }

    @Override
    protected String aLinea(Conductor c) {
        String[] campos = {
            String.valueOf(c.getId()), c.getDni(), c.getNombre(), c.getLicencia(), c.getCategoria(),
            c.getEstado().name(),
            String.valueOf(c.getVehiculoHabitual() == null ? 0 : c.getVehiculoHabitual().getId())
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
    protected Conductor deLinea(String[] c) {
        Conductor conductor = new Conductor(Integer.parseInt(c[0]), c[1], c[2], c[3], c[4]);
        conductor.setEstado(EstadoConductor.valueOf(c[5]));
        int idVehiculo = Integer.parseInt(c[6]);
        if (idVehiculo > 0) {
            conductor.setVehiculoHabitual(vehiculos.buscarPorId(idVehiculo));
        }
        return conductor;
    }

    @Override
    protected int idDe(Conductor c) {
        return c.getId();
    }

    @Override
    protected void fijarId(Conductor c, int id) {
        c.setId(id);
    }

    @Override
    protected void marcarBaja(Conductor c) {
        c.setEstado(EstadoConductor.INACTIVO);
    }

    public Conductor buscarPorLicencia(String licencia) {
        if (licencia == null) {
            return null;
        }
        for (Conductor c : items) {
            if (c.getLicencia().equalsIgnoreCase(licencia.trim())) {
                return c;
            }
        }
        return null;
    }

    /**
     * Conductor que ya tiene asociado ese vehiculo como habitual (o null).
     */
    public Conductor buscarPorVehiculoHabitual(Vehiculo v) {
        for (Conductor c : items) {
            if (c.getVehiculoHabitual() != null && c.getVehiculoHabitual().getId() == v.getId()) {
                return c;
            }
        }
        return null;
    }

    public List<Conductor> buscar(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        List<Conductor> resultado = new ArrayList<>();
        for (Conductor c : items) {
            if (c.getNombre().toLowerCase().contains(t) || c.getDni().contains(t)
                    || c.getLicencia().toLowerCase().contains(t)) {
                resultado.add(c);
            }
        }
        return resultado;
    }
}
