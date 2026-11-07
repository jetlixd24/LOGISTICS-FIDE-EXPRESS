/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.controlador;

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
import logisticsfideexpress.modelo.Carga;
import logisticsfideexpress.modelo.Cliente;
import logisticsfideexpress.modelo.Conductor;
import logisticsfideexpress.modelo.SeguimientoCarga;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.enums.EstadoConductor;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.modelo.enums.EstadoVehiculo;
import logisticsfideexpress.repository.ClienteRepository;
import logisticsfideexpress.repository.ConductorRepository;
import logisticsfideexpress.repository.SeguimientoRepository;
import logisticsfideexpress.repository.ServicioRepository;
import logisticsfideexpress.repository.VehiculoRepository;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.util.Validador;

/**
 * RF21-RF25: el cliente solicita, el administrador aprueba/rechaza y asigna
 * vehiculo + conductor. Cada cambio de estado deja una huella en el seguimiento
 * para que el cliente vea la linea de tiempo.
 */
public class GestorServicios {

    private final ServicioRepository repo;
    private final ClienteRepository clientes;
    private final VehiculoRepository vehiculos;
    private final ConductorRepository conductores;
    private final SeguimientoRepository seguimientos;

    public GestorServicios(ServicioRepository repo, ClienteRepository clientes, VehiculoRepository vehiculos,
            ConductorRepository conductores, SeguimientoRepository seguimientos) {
        this.repo = repo;
        this.clientes = clientes;
        this.vehiculos = vehiculos;
        this.conductores = conductores;
        this.seguimientos = seguimientos;
    }

    // ------------------------------------------------------------ panel cliente
    /**
     * RF21: el cliente autenticado solicita un servicio. volumen puede ir
     * vacio.
     */
    public ServicioTransporte solicitar(String origen, String destino, String descripcionCarga,
            String pesoKg, String volumen)
            throws ValidacionException, OperacionNoPermitidaException {
        Sesion.exigirCliente();
        Validador.noVacio(origen, "Origen");
        Validador.noVacio(destino, "Destino");
        Validador.noVacio(descripcionCarga, "Descripcion de la carga");
        double peso = Validador.numeroPositivo(pesoKg, "Peso (kg)");
        double vol = Validador.esVacio(volumen) ? 0 : Validador.numeroNoNegativo(volumen, "Volumen");
        Cliente cliente = clientes.buscarPorId(Sesion.getUsuario().getId());
        ServicioTransporte s = new ServicioTransporte(origen.trim(), destino.trim(), LocalDate.now(),
                0, cliente, new Carga(descripcionCarga.trim(), peso, vol));
        repo.guardar(s);
        registrarEvento(s, EstadoServicio.PENDIENTE, s.getOrigen(), "Solicitud registrada por el cliente");
        return s;
    }

    /**
     * RF22: el cliente ve solo sus solicitudes (RN07).
     */
    public List<ServicioTransporte> misSolicitudes() throws OperacionNoPermitidaException {
        Sesion.exigirCliente();
        return repo.buscarPorCliente(Sesion.getUsuario().getId());
    }

    // ------------------------------------------------------ consultas (ambos roles)
    /**
     * El administrador ve cualquier servicio; el cliente solo los suyos.
     */
    public ServicioTransporte obtener(int id) throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        ServicioTransporte s = repo.buscarPorId(id);
        if (s == null) {
            throw new RegistroNoEncontradoException("No existe el servicio solicitado.");
        }
        if (Sesion.esAdministrador()) {
            return s;
        }
        if (Sesion.esCliente() && s.getCliente().getId() == Sesion.getUsuario().getId()) {
            return s;
        }
        throw new OperacionNoPermitidaException("No tiene permiso para ver este servicio.");
    }

    // -------------------------------------------------------- panel administrador
    /**
     * RF22
     */
    public List<ServicioTransporte> listar() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.listar();
    }

    // Sobrecarga: filtro por estado
    public List<ServicioTransporte> listar(EstadoServicio estado) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.buscar(estado);
    }

    // Sobrecarga: filtro por texto (origen, destino o cliente)
    public List<ServicioTransporte> listar(String texto) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.buscar(texto);
    }

    /**
     * RF23: aprueba una solicitud PENDIENTE y fija la tarifa.
     */
    public void aprobar(int idServicio, String tarifa)
            throws ValidacionException, RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        ServicioTransporte s = requerirServicio(idServicio);
        exigirEstado(s, EstadoServicio.PENDIENTE, "Solo se pueden aprobar solicitudes pendientes.");
        double monto = Validador.numeroPositivo(tarifa, "Tarifa");
        s.setTarifa(monto);
        s.setEstado(EstadoServicio.APROBADO);
        repo.actualizar(s);
        registrarEvento(s, EstadoServicio.APROBADO, s.getOrigen(), "Solicitud aprobada");
    }

    /**
     * RF23: rechaza una solicitud PENDIENTE; el motivo es obligatorio.
     */
    public void rechazar(int idServicio, String motivo)
            throws ValidacionException, RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        ServicioTransporte s = requerirServicio(idServicio);
        exigirEstado(s, EstadoServicio.PENDIENTE, "Solo se pueden rechazar solicitudes pendientes.");
        Validador.noVacio(motivo, "Motivo de rechazo");
        s.setMotivoRechazo(motivo.trim());
        s.setEstado(EstadoServicio.RECHAZADO);
        repo.actualizar(s);
        registrarEvento(s, EstadoServicio.RECHAZADO, s.getOrigen(), "Rechazada: " + motivo.trim());
    }

    /**
     * RF24: asigna vehiculo y conductor a un servicio APROBADO. fechaSalida en
     * AAAA-MM-DD; si va vacia se usa la fecha de hoy.
     */
    public AsignacionViaje asignar(int idServicio, int idVehiculo, int idConductor, String fechaSalida)
            throws ValidacionException, RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        ServicioTransporte s = requerirServicio(idServicio);
        exigirEstado(s, EstadoServicio.APROBADO, "Solo se pueden asignar servicios aprobados.");
        Vehiculo v = vehiculos.buscarPorId(idVehiculo);
        if (v == null) {
            throw new RegistroNoEncontradoException("El vehiculo seleccionado no existe.");
        }
        Conductor c = conductores.buscarPorId(idConductor);
        if (c == null) {
            throw new RegistroNoEncontradoException("El conductor seleccionado no existe.");
        }
        LocalDate salida = Validador.esVacio(fechaSalida) ? LocalDate.now() : Validador.aFecha(fechaSalida, "Fecha de salida");
        if (v.getEstado() != EstadoVehiculo.DISPONIBLE) {
            throw new OperacionNoPermitidaException("El vehiculo no esta disponible (estado: " + v.getEstado() + ").");
        }
        if (c.getEstado() != EstadoConductor.DISPONIBLE) {
            throw new OperacionNoPermitidaException("El conductor no esta disponible (estado: " + c.getEstado() + ").");
        }
        if (s.getCarga().getPesoKg() > v.getCapacidadKg()) {
            throw new OperacionNoPermitidaException("La carga (" + s.getCarga().getPesoKg()
                    + " kg) supera la capacidad del vehiculo (" + v.getCapacidadKg() + " kg).");
        }
        AsignacionViaje a = new AsignacionViaje(s, v, c, salida);
        repo.guardarAsignacion(a);
        v.setEstado(EstadoVehiculo.EN_SERVICIO);
        vehiculos.actualizar(v);
        c.setEstado(EstadoConductor.EN_SERVICIO);
        conductores.actualizar(c);
        s.setEstado(EstadoServicio.ASIGNADO);
        repo.actualizar(s);
        registrarEvento(s, EstadoServicio.ASIGNADO, s.getOrigen(),
                "Asignado vehiculo " + v.getPlaca() + " y conductor " + c.getNombre());
        return a;
    }

    /**
     * RF25: asignaciones de todos los servicios.
     */
    public List<AsignacionViaje> listarAsignaciones() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.listarAsignaciones();
    }

    // ------------------------------------------------------------------ apoyo
    private ServicioTransporte requerirServicio(int id) throws RegistroNoEncontradoException {
        ServicioTransporte s = repo.buscarPorId(id);
        if (s == null) {
            throw new RegistroNoEncontradoException("No existe el servicio solicitado.");
        }
        return s;
    }

    private void exigirEstado(ServicioTransporte s, EstadoServicio esperado, String mensaje)
            throws OperacionNoPermitidaException {
        if (s.getEstado() != esperado) {
            throw new OperacionNoPermitidaException(mensaje + " Estado actual: " + s.getEstado() + ".");
        }
    }

    private void registrarEvento(ServicioTransporte s, EstadoServicio estado, String ubicacion, String observacion) {
        seguimientos.guardar(new SeguimientoCarga(s, LocalDateTime.now(), estado, ubicacion, observacion));
    }
}
