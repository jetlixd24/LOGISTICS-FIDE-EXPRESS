/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.servicio;

import logisticsfideexpress.dao.interfaz.ISeguimientoDAO;
import logisticsfideexpress.dao.interfaz.IServicioDAO;
import logisticsfideexpress.dao.interfaz.IConductorDAO;
import logisticsfideexpress.dao.interfaz.IVehiculoDAO;
/**
 *
 * @author JETLI
 */
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.RegistroNoEncontradoException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.modelo.AsignacionViaje;
import logisticsfideexpress.modelo.Conductor;
import logisticsfideexpress.modelo.SeguimientoCarga;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.enums.EstadoConductor;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.modelo.enums.EstadoVehiculo;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.util.Validador;

/**
 * RF26-RF28: el administrador actualiza el avance (EN_RUTA, ENTREGADO) y el cliente consulta
 * la linea de tiempo de sus servicios. Al entregar, se libera el vehiculo y el conductor.
 */
public class SeguimientoService {

    private final ISeguimientoDAO repo;
    private final IServicioDAO servicios;
    private final IVehiculoDAO vehiculos;
    private final IConductorDAO conductores;

    public SeguimientoService(ISeguimientoDAO repo, IServicioDAO servicios,
                             IVehiculoDAO vehiculos, IConductorDAO conductores) {
        this.repo = repo;
        this.servicios = servicios;
        this.vehiculos = vehiculos;
        this.conductores = conductores;
    }

    /**
     * RF26/RF27: registra un avance. Transiciones validas:
     *   ASIGNADO -> EN_RUTA | EN_RUTA -> EN_RUTA (nueva ubicacion) | EN_RUTA -> ENTREGADO.
     * receptor solo es obligatorio al entregar (RF28).
     */
    public SeguimientoCarga registrarAvance(int idServicio, EstadoServicio nuevoEstado, String ubicacion,
                                            String observacion, String receptor)
            throws ValidacionException, RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        ServicioTransporte s = servicios.buscarPorId(idServicio);
        if (s == null) {
            throw new RegistroNoEncontradoException("No existe el servicio solicitado.");
        }
        if (nuevoEstado == null) {
            throw new ValidacionException("Debe indicar el nuevo estado.");
        }
        validarTransicion(s.getEstado(), nuevoEstado);
        
        AsignacionViaje activa = servicios.buscarAsignacionActiva(s);
        if (activa != null && LocalDate.now().isBefore(activa.getFechaSalida())) {
            throw new OperacionNoPermitidaException("No se puede registrar el avance: la fecha de salida del viaje es "
                    + activa.getFechaSalida() + " y hoy es " + LocalDate.now() + ".");
        }
        
        
        Validador.noVacio(ubicacion, "Ubicacion");
        if (nuevoEstado == EstadoServicio.ENTREGADO) {
            Validador.noVacio(receptor, "Receptor de la entrega");
        }

        SeguimientoCarga seg = new SeguimientoCarga(s, LocalDateTime.now(), nuevoEstado, ubicacion.trim(),
                Validador.esVacio(observacion) ? "" : observacion.trim());
        s.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoServicio.ENTREGADO) {
            s.setReceptorEntrega(receptor.trim());
            s.setFechaEntrega(LocalDate.now());
            finalizarAsignacion(s);
        }
        servicios.actualizar(s);
        repo.guardar(seg);
        return seg;
    }

    /** RF27 / RN07: el administrador ve cualquier servicio; el cliente solo los suyos. */
    public List<SeguimientoCarga> consultar(int idServicio)
            throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        ServicioTransporte s = servicios.buscarPorId(idServicio);
        if (s == null) {
            throw new RegistroNoEncontradoException("No existe el servicio solicitado.");
        }
        boolean propio = Sesion.esCliente() && s.getCliente().getId() == Sesion.getUsuario().getId();
        if (!Sesion.esAdministrador() && !propio) {
            throw new OperacionNoPermitidaException("No tiene permiso para ver el seguimiento de este servicio.");
        }
        return repo.buscarPorServicio(idServicio);
    }

    /** Ultimo avance registrado (o null si aun no hay). */
    public SeguimientoCarga ultimoAvance(int idServicio)
            throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        List<SeguimientoCarga> lista = consultar(idServicio);
        return lista.isEmpty() ? null : lista.get(lista.size() - 1);
    }

    // ------------------------------------------------------------------ apoyo

    private void validarTransicion(EstadoServicio actual, EstadoServicio nuevo) throws OperacionNoPermitidaException {
        boolean valida = (actual == EstadoServicio.ASIGNADO && nuevo == EstadoServicio.EN_RUTA)
                || (actual == EstadoServicio.EN_RUTA
                    && (nuevo == EstadoServicio.EN_RUTA || nuevo == EstadoServicio.ENTREGADO));
        if (!valida) {
            throw new OperacionNoPermitidaException("No se puede pasar de " + actual + " a " + nuevo + ".");
        }
    }

    /** Cierra la asignacion activa y libera al vehiculo y al conductor. */
    private void finalizarAsignacion(ServicioTransporte s) {
        AsignacionViaje a = servicios.buscarAsignacionActiva(s);
        if (a == null) {
            return;
        }
        a.setEstado(AsignacionViaje.FINALIZADA);
        a.setFechaLlegada(LocalDate.now());
        servicios.actualizarAsignacion(a);
        Vehiculo v = a.getVehiculo();
        if (v.getEstado() == EstadoVehiculo.EN_SERVICIO) {
            v.setEstado(EstadoVehiculo.DISPONIBLE);
            vehiculos.actualizar(v);
        }
        Conductor c = a.getConductor();
        if (c.getEstado() == EstadoConductor.EN_SERVICIO) {
            c.setEstado(EstadoConductor.DISPONIBLE);
            conductores.actualizar(c);
        }
    }
}
