/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.repository;

/**
 *
 * @author JETLI
 */
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.modelo.Transportista;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.VehiculoAfiliado;
import logisticsfideexpress.modelo.VehiculoPropio;
import logisticsfideexpress.modelo.enums.EstadoVehiculo;
import logisticsfideexpress.util.ArchivoUtil;

/**
 * data/vehiculos.txt Formato: id;tipo;placa;marca;capacidadKg;estado;e1;e2
 * PROPIO: e1=kilometraje e2=fechaCompra (AAAA-MM-DD, puede ir vacia) AFILIADO:
 * e1=tarifaAcordada e2=idTransportista
 */
public class VehiculoRepository extends RepositorioArchivo<Vehiculo> {

    private static final String RUTA = "data/vehiculos.txt";
    private final TransportistaRepository transportistas;

    public VehiculoRepository(TransportistaRepository transportistas) {
        super(RUTA);
        this.transportistas = transportistas;
        cargar();
    }

    @Override
    protected String aLinea(Vehiculo v) {
        String e1, e2;
        if (v instanceof VehiculoPropio p) {
            e1 = String.valueOf(p.getKilometraje());
            e2 = p.getFechaCompra() == null ? "" : p.getFechaCompra().toString();
        } else {
            VehiculoAfiliado a = (VehiculoAfiliado) v;
            e1 = String.valueOf(a.getTarifaAcordada());
            e2 = String.valueOf(a.getTransportista().getId());
        }
        String[] campos = {
            String.valueOf(v.getId()), v.getTipo(), v.getPlaca(), v.getMarca(),
            String.valueOf(v.getCapacidadKg()), v.getEstado().name(), e1, e2
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
    protected Vehiculo deLinea(String[] c) {
        int id = Integer.parseInt(c[0]);
        double capacidad = Double.parseDouble(c[4]);
        Vehiculo v;
        if (c[1].equals("PROPIO")) {
            LocalDate fecha = c[7].isEmpty() ? null : LocalDate.parse(c[7]);
            v = new VehiculoPropio(id, c[2], c[3], capacidad, Double.parseDouble(c[6]), fecha);
        } else {
            Transportista t = transportistas.buscarPorId(Integer.parseInt(c[7]));
            if (t == null) {
                throw new IllegalStateException("transportista inexistente");
            }
            VehiculoAfiliado a = new VehiculoAfiliado(id, c[2], c[3], capacidad, Double.parseDouble(c[6]), t);
            t.agregarVehiculo(a);
            v = a;
        }
        v.setEstado(EstadoVehiculo.valueOf(c[5]));
        return v;
    }

    @Override
    protected int idDe(Vehiculo v) {
        return v.getId();
    }

    @Override
    protected void fijarId(Vehiculo v, int id) {
        v.setId(id);
    }

    @Override
    protected void marcarBaja(Vehiculo v) {
        v.setEstado(EstadoVehiculo.INACTIVO);
    }

    public Vehiculo buscarPorPlaca(String placa) {
        if (placa == null) {
            return null;
        }
        for (Vehiculo v : items) {
            if (v.getPlaca().equalsIgnoreCase(placa.trim())) {
                return v;
            }
        }
        return null;
    }

    // Sobrecarga: por texto (placa o marca)
    public List<Vehiculo> buscar(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        List<Vehiculo> resultado = new ArrayList<>();
        for (Vehiculo v : items) {
            if (v.getPlaca().toLowerCase().contains(t) || v.getMarca().toLowerCase().contains(t)) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    // Sobrecarga: por estado
    public List<Vehiculo> buscar(EstadoVehiculo estado) {
        List<Vehiculo> resultado = new ArrayList<>();
        for (Vehiculo v : items) {
            if (v.getEstado() == estado) {
                resultado.add(v);
            }
        }
        return resultado;
    }
}
