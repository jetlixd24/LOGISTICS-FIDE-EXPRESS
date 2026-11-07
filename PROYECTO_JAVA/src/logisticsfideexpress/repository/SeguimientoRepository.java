/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.repository;

/**
 *
 * @author JETLI
 */
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import logisticsfideexpress.modelo.SeguimientoCarga;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.util.ArchivoUtil;

/**
 * data/seguimientos.txt Formato:
 * id;idServicio;fechaHora;estado;ubicacion;observacion Es historico: no se da
 * de baja. Al cargar, cada seguimiento se agrega a su servicio.
 */
public class SeguimientoRepository extends RepositorioArchivo<SeguimientoCarga> {

    private static final String RUTA = "data/seguimientos.txt";
    private final ServicioRepository servicios;

    public SeguimientoRepository(ServicioRepository servicios) {
        super(RUTA);
        this.servicios = servicios;
        cargar();
    }

    @Override
    protected String aLinea(SeguimientoCarga s) {
        return String.join(ArchivoUtil.SEPARADOR,
                String.valueOf(s.getId()),
                String.valueOf(s.getServicio().getId()),
                s.getFechaHora().toString(),
                s.getEstado().name(),
                ArchivoUtil.limpiar(s.getUbicacion()),
                ArchivoUtil.limpiar(s.getObservacion()));
    }

    @Override
    protected SeguimientoCarga deLinea(String[] c) {
        ServicioTransporte servicio = servicios.buscarPorId(Integer.parseInt(c[1]));
        if (servicio == null) {
            throw new IllegalStateException("servicio inexistente");
        }
        SeguimientoCarga s = new SeguimientoCarga(Integer.parseInt(c[0]), servicio,
                LocalDateTime.parse(c[2]), EstadoServicio.valueOf(c[3]), c[4], c[5]);
        servicio.agregarSeguimiento(s);
        return s;
    }

    @Override
    protected int idDe(SeguimientoCarga s) {
        return s.getId();
    }

    @Override
    protected void fijarId(SeguimientoCarga s, int id) {
        s.setId(id);
    }

    @Override
    protected void marcarBaja(SeguimientoCarga s) {
        throw new UnsupportedOperationException("Los seguimientos son historicos y no se dan de baja.");
    }

    /**
     * Guarda el seguimiento y lo agrega tambien a la lista del servicio
     * (agregacion en memoria).
     */
    @Override
    public void guardar(SeguimientoCarga s) {
        super.guardar(s);
        s.getServicio().agregarSeguimiento(s);
    }

    /**
     * Seguimientos de un servicio, del mas antiguo al mas reciente.
     */
    public List<SeguimientoCarga> buscarPorServicio(int idServicio) {
        List<SeguimientoCarga> resultado = new ArrayList<>();
        for (SeguimientoCarga s : items) {
            if (s.getServicio().getId() == idServicio) {
                resultado.add(s);
            }
        }
        resultado.sort(Comparator.comparing(SeguimientoCarga::getFechaHora));
        return resultado;
    }

    /**
     * Ultimo seguimiento registrado de un servicio (o null).
     */
    public SeguimientoCarga buscarUltimo(int idServicio) {
        List<SeguimientoCarga> lista = buscarPorServicio(idServicio);
        return lista.isEmpty() ? null : lista.get(lista.size() - 1);
    }
}
