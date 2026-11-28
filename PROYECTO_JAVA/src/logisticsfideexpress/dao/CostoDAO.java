/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.modelo.CostoViaje;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoGasto;
import logisticsfideexpress.util.ArchivoUtil;

/**
 * data/costos.txt Formato: id;idServicio;tipo;monto;fecha;estado Baja logica:
 * el gasto queda RECHAZADO.
 */
public class CostoDAO extends RepositorioArchivo<CostoViaje> {

    private static final String RUTA = "data/costos.txt";
    private final ServicioDAO servicios;

    public CostoDAO(ServicioDAO servicios) {
        super(RUTA);
        this.servicios = servicios;
        cargar();
    }

    @Override
    protected String aLinea(CostoViaje c) {
        return String.join(ArchivoUtil.SEPARADOR,
                String.valueOf(c.getId()),
                String.valueOf(c.getServicio().getId()),
                ArchivoUtil.limpiar(c.getTipo()),
                String.valueOf(c.getMonto()),
                c.getFecha().toString(),
                c.getEstado().name());
    }

    @Override
    protected CostoViaje deLinea(String[] c) {
        ServicioTransporte servicio = servicios.buscarPorId(Integer.parseInt(c[1]));
        if (servicio == null) {
            throw new IllegalStateException("servicio inexistente");
        }
        CostoViaje costo = new CostoViaje(Integer.parseInt(c[0]), servicio, c[2],
                Double.parseDouble(c[3]), LocalDate.parse(c[4]));
        costo.setEstado(EstadoGasto.valueOf(c[5]));
        return costo;
    }

    @Override
    protected int idDe(CostoViaje c) {
        return c.getId();
    }

    @Override
    protected void fijarId(CostoViaje c, int id) {
        c.setId(id);
    }

    @Override
    protected void marcarBaja(CostoViaje c) {
        c.setEstado(EstadoGasto.RECHAZADO);
    }

    public List<CostoViaje> buscarPorServicio(int idServicio) {
        List<CostoViaje> resultado = new ArrayList<>();
        for (CostoViaje c : items) {
            if (c.getServicio().getId() == idServicio) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    // Sobrecarga: por estado
    public List<CostoViaje> buscar(EstadoGasto estado) {
        List<CostoViaje> resultado = new ArrayList<>();
        for (CostoViaje c : items) {
            if (c.getEstado() == estado) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    // Sobrecarga: por servicio y estado
    public List<CostoViaje> buscar(int idServicio, EstadoGasto estado) {
        List<CostoViaje> resultado = new ArrayList<>();
        for (CostoViaje c : items) {
            if (c.getServicio().getId() == idServicio && c.getEstado() == estado) {
                resultado.add(c);
            }
        }
        return resultado;
    }
}
