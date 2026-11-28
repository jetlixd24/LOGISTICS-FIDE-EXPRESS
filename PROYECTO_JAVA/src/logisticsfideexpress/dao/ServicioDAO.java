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
import logisticsfideexpress.modelo.AsignacionViaje;
import logisticsfideexpress.modelo.Carga;
import logisticsfideexpress.modelo.Cliente;
import logisticsfideexpress.modelo.Conductor;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.util.ArchivoUtil;

/**
 * Guarda dos archivos (ServicioTransporte contiene sus asignaciones y su
 * carga): data/servicios.txt ->
 * id;origen;destino;fechaSolicitud;estado;tarifa;idCliente;
 * cargaDescripcion;cargaPesoKg;cargaVolumen;motivoRechazo;receptorEntrega;fechaEntrega
 * data/asignaciones.txt ->
 * id;idServicio;idVehiculo;idConductor;fechaSalida;fechaLlegada;estado Los
 * seguimientos los guarda SeguimientoRepository.
 */
public class ServicioDAO extends RepositorioArchivo<ServicioTransporte> {

    private static final String RUTA = "data/servicios.txt";
    private static final String RUTA_ASIGNACIONES = "data/asignaciones.txt";

    private final ClienteDAO clientes;
    private final VehiculoDAO vehiculos;
    private final ConductorDAO conductores;

    public ServicioDAO(ClienteDAO clientes, VehiculoDAO vehiculos,
            ConductorDAO conductores) {
        super(RUTA);
        this.clientes = clientes;
        this.vehiculos = vehiculos;
        this.conductores = conductores;
        cargar();
        cargarAsignaciones();
    }

    // ---------------------------------------------------------------- servicios
    @Override
    protected String aLinea(ServicioTransporte s) {
        Carga carga = s.getCarga();
        return String.join(ArchivoUtil.SEPARADOR,
                String.valueOf(s.getId()),
                ArchivoUtil.limpiar(s.getOrigen()),
                ArchivoUtil.limpiar(s.getDestino()),
                s.getFechaSolicitud().toString(),
                s.getEstado().name(),
                String.valueOf(s.getTarifa()),
                String.valueOf(s.getCliente().getId()),
                ArchivoUtil.limpiar(carga.getDescripcion()),
                String.valueOf(carga.getPesoKg()),
                String.valueOf(carga.getVolumen()),
                ArchivoUtil.limpiar(s.getMotivoRechazo()),
                ArchivoUtil.limpiar(s.getReceptorEntrega()),
                s.getFechaEntrega() == null ? "" : s.getFechaEntrega().toString());
    }

    @Override
    protected ServicioTransporte deLinea(String[] c) {
        Cliente cliente = clientes.buscarPorId(Integer.parseInt(c[6]));
        if (cliente == null) {
            throw new IllegalStateException("cliente inexistente");
        }
        Carga carga = new Carga(c[7], Double.parseDouble(c[8]), Double.parseDouble(c[9]));
        ServicioTransporte s = new ServicioTransporte(Integer.parseInt(c[0]), c[1], c[2],
                LocalDate.parse(c[3]), Double.parseDouble(c[5]), cliente, carga);
        s.setEstado(EstadoServicio.valueOf(c[4]));
        s.setMotivoRechazo(c[10]);
        s.setReceptorEntrega(c[11]);
        s.setFechaEntrega(c[12].isEmpty() ? null : LocalDate.parse(c[12]));
        return s;
    }

    @Override
    protected int idDe(ServicioTransporte s) {
        return s.getId();
    }

    @Override
    protected void fijarId(ServicioTransporte s, int id) {
        s.setId(id);
    }

    /**
     * Baja logica: el servicio queda RECHAZADO (no se borra el historico).
     */
    @Override
    protected void marcarBaja(ServicioTransporte s) {
        s.setEstado(EstadoServicio.RECHAZADO);
        if (s.getMotivoRechazo() == null || s.getMotivoRechazo().isEmpty()) {
            s.setMotivoRechazo("Anulado");
        }
    }

    public List<ServicioTransporte> buscarPorCliente(int idCliente) {
        List<ServicioTransporte> resultado = new ArrayList<>();
        for (ServicioTransporte s : items) {
            if (s.getCliente().getId() == idCliente) {
                resultado.add(s);
            }
        }
        return resultado;
    }

    // Sobrecarga: por estado
    public List<ServicioTransporte> buscar(EstadoServicio estado) {
        List<ServicioTransporte> resultado = new ArrayList<>();
        for (ServicioTransporte s : items) {
            if (s.getEstado() == estado) {
                resultado.add(s);
            }
        }
        return resultado;
    }

    // Sobrecarga: por texto (origen, destino o cliente)
    public List<ServicioTransporte> buscar(String texto) {
        String t = texto == null ? "" : texto.trim().toLowerCase();
        List<ServicioTransporte> resultado = new ArrayList<>();
        for (ServicioTransporte s : items) {
            if (s.getOrigen().toLowerCase().contains(t) || s.getDestino().toLowerCase().contains(t)
                    || s.getCliente().getRazonSocial().toLowerCase().contains(t)) {
                resultado.add(s);
            }
        }
        return resultado;
    }

    // ------------------------------------------------------------- asignaciones
    private void cargarAsignaciones() {
        for (String linea : ArchivoUtil.leerLineas(RUTA_ASIGNACIONES)) {
            try {
                String[] c = ArchivoUtil.partir(linea);
                ServicioTransporte s = buscarPorId(Integer.parseInt(c[1]));
                Vehiculo v = vehiculos.buscarPorId(Integer.parseInt(c[2]));
                Conductor cond = conductores.buscarPorId(Integer.parseInt(c[3]));
                if (s == null || v == null || cond == null) {
                    throw new IllegalStateException("servicio, vehiculo o conductor inexistente");
                }
                AsignacionViaje a = new AsignacionViaje(Integer.parseInt(c[0]), s, v, cond, LocalDate.parse(c[4]));
                a.setFechaLlegada(c[5].isEmpty() ? null : LocalDate.parse(c[5]));
                a.setEstado(c[6]);
                s.agregarAsignacion(a);
            } catch (RuntimeException e) {
                System.err.println("Linea ignorada en " + RUTA_ASIGNACIONES + ": " + linea + " (" + e.getMessage() + ")");
            }
        }
    }

    /**
     * Reescribe el archivo con las asignaciones de todos los servicios.
     */
    private void persistirAsignaciones() {
        List<String> lineas = new ArrayList<>();
        for (ServicioTransporte s : items) {
            for (AsignacionViaje a : s.getAsignaciones()) {
                lineas.add(String.join(ArchivoUtil.SEPARADOR,
                        String.valueOf(a.getId()),
                        String.valueOf(s.getId()),
                        String.valueOf(a.getVehiculo().getId()),
                        String.valueOf(a.getConductor().getId()),
                        a.getFechaSalida().toString(),
                        a.getFechaLlegada() == null ? "" : a.getFechaLlegada().toString(),
                        ArchivoUtil.limpiar(a.getEstado())));
            }
        }
        ArchivoUtil.escribirLineas(RUTA_ASIGNACIONES, lineas);
    }

    public int siguienteIdAsignacion() {
        int max = 0;
        for (AsignacionViaje a : listarAsignaciones()) {
            max = Math.max(max, a.getId());
        }
        return max + 1;
    }

    /**
     * Agrega la asignacion al servicio y la guarda en el archivo.
     */
    public void guardarAsignacion(AsignacionViaje a) {
        if (a.getId() <= 0) {
            a.setId(siguienteIdAsignacion());
        }
        a.getServicio().agregarAsignacion(a);
        persistirAsignaciones();
    }

    /**
     * Guarda los cambios hechos a una asignacion existente (por ejemplo, al
     * finalizar el viaje).
     */
    public void actualizarAsignacion(AsignacionViaje a) {
        persistirAsignaciones();
    }

    public List<AsignacionViaje> listarAsignaciones() {
        List<AsignacionViaje> todas = new ArrayList<>();
        for (ServicioTransporte s : items) {
            todas.addAll(s.getAsignaciones());
        }
        return todas;
    }

    /**
     * Asignacion en estado ACTIVA de un servicio (o null).
     */
    public AsignacionViaje buscarAsignacionActiva(ServicioTransporte s) {
        for (AsignacionViaje a : s.getAsignaciones()) {
            if (AsignacionViaje.ACTIVA.equals(a.getEstado())) {
                return a;
            }
        }
        return null;
    }
}
