/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.servicio;

import logisticsfideexpress.dao.interfaz.ICostoDAO;
/**
 *
 * @author JETLI
 */
import java.time.LocalDate;
import java.util.List;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.RegistroNoEncontradoException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.modelo.CostoViaje;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoGasto;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.dao.interfaz.IServicioDAO;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.util.Validador;

/**
 * RF29-RF32: gastos del viaje (combustible, peaje, viaticos, otros) y su
 * aprobacion. Solo administrador.
 */
public class CostoService {

    private final ICostoDAO repo;
    private final IServicioDAO servicios;

    public CostoService(ICostoDAO repo, IServicioDAO servicios) {
        this.repo = repo;
        this.servicios = servicios;
    }

    /**
     * RF29: registra un gasto PENDIENTE. Solo servicios ya asignados (ASIGNADO,
     * EN_RUTA o ENTREGADO). fecha en AAAA-MM-DD; si va vacia se usa hoy.
     */
    public CostoViaje registrar(int idServicio, String tipo, String monto, String fecha)
            throws ValidacionException, RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        ServicioTransporte s = servicios.buscarPorId(idServicio);
        if (s == null) {
            throw new RegistroNoEncontradoException("No existe el servicio solicitado.");
        }
        EstadoServicio e = s.getEstado();
        if (e != EstadoServicio.ASIGNADO && e != EstadoServicio.EN_RUTA && e != EstadoServicio.ENTREGADO) {
            throw new OperacionNoPermitidaException("Solo se registran gastos de servicios asignados, en ruta o entregados.");
        }
        Validador.noVacio(tipo, "Tipo de gasto");
        double importe = Validador.numeroPositivo(monto, "Monto");
        LocalDate f = Validador.esVacio(fecha) ? LocalDate.now() : Validador.aFecha(fecha, "Fecha");
        CostoViaje c = new CostoViaje(s, tipo.trim().toLowerCase(), importe, f);
        repo.guardar(c);
        return c;
    }

    /**
     * RF31: aprueba un gasto PENDIENTE.
     */
    public void aprobar(int idCosto) throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        cambiarEstado(idCosto, EstadoGasto.APROBADO);
    }

    /**
     * RF31: rechaza un gasto PENDIENTE.
     */
    public void rechazar(int idCosto) throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        cambiarEstado(idCosto, EstadoGasto.RECHAZADO);
    }

    private void cambiarEstado(int idCosto, EstadoGasto nuevo)
            throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        CostoViaje c = repo.buscarPorId(idCosto);
        if (c == null) {
            throw new RegistroNoEncontradoException("No existe el gasto solicitado.");
        }
        if (c.getEstado() != EstadoGasto.PENDIENTE) {
            throw new OperacionNoPermitidaException("El gasto ya fue " + c.getEstado()
                    + "; solo se resuelven gastos pendientes.");
        }
        c.setEstado(nuevo);
        repo.actualizar(c);
    }

    /**
     * RF30
     */
    public List<CostoViaje> listar() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.listar();
    }

    // Sobrecarga: filtro por estado
    public List<CostoViaje> listar(EstadoGasto estado) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.buscar(estado);
    }

    // Sobrecarga: gastos de un servicio
    public List<CostoViaje> listarPorServicio(int idServicio)
            throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        if (servicios.buscarPorId(idServicio) == null) {
            throw new RegistroNoEncontradoException("No existe el servicio solicitado.");
        }
        return repo.buscarPorServicio(idServicio);
    }

    /**
     * RF32: suma de los gastos APROBADOS de un servicio.
     */
    public double totalAprobado(int idServicio) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        double total = 0;
        for (CostoViaje c : repo.buscar(idServicio, EstadoGasto.APROBADO)) {
            total += c.getMonto();
        }
        return Math.round(total * 100.0) / 100.0;
    }
}
