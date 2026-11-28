/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.servicio;

import logisticsfideexpress.dao.interfaz.ILiquidacionDAO;
/**
 *
 * @author JETLI
 */
import java.time.LocalDate;
import java.util.List;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.RegistroDuplicadoException;
import logisticsfideexpress.excepciones.RegistroNoEncontradoException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.modelo.CostoViaje;
import logisticsfideexpress.modelo.Liquidacion;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.modelo.enums.EstadoGasto;
import logisticsfideexpress.modelo.enums.EstadoServicio;
import logisticsfideexpress.dao.interfaz.ICostoDAO;
import logisticsfideexpress.dao.interfaz.IServicioDAO;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.util.Validador;

/**
 * RF33 y RF39: liquidacion de un servicio entregado (ingreso - gastos aprobados
 * = utilidad).
 */
public class LiquidacionService {

    private final ILiquidacionDAO repo;
    private final IServicioDAO servicios;
    private final ICostoDAO costos;

    public LiquidacionService(ILiquidacionDAO repo, IServicioDAO servicios, ICostoDAO costos) {
        this.repo = repo;
        this.servicios = servicios;
        this.costos = costos;
    }

    /**
     * RF33: genera la liquidacion de un servicio ENTREGADO. Reglas: - una sola
     * liquidacion por servicio; - no puede haber gastos pendientes de aprobar;
     * - solo cuentan los gastos APROBADOS; el ingreso es la tarifa del
     * servicio.
     */
    public Liquidacion generar(int idServicio)
            throws RegistroNoEncontradoException, RegistroDuplicadoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        ServicioTransporte s = servicios.buscarPorId(idServicio);
        if (s == null) {
            throw new RegistroNoEncontradoException("No existe el servicio solicitado.");
        }
        if (s.getEstado() != EstadoServicio.ENTREGADO) {
            throw new OperacionNoPermitidaException("Solo se liquidan servicios entregados. Estado actual: " + s.getEstado() + ".");
        }
        if (repo.buscarPorServicio(idServicio) != null) {
            throw new RegistroDuplicadoException("El servicio ya fue liquidado.");
        }
        if (!costos.buscar(idServicio, EstadoGasto.PENDIENTE).isEmpty()) {
            throw new OperacionNoPermitidaException("Hay gastos pendientes de aprobar para este servicio.");
        }
        Liquidacion l = new Liquidacion(s, LocalDate.now());
        for (CostoViaje c : costos.buscar(idServicio, EstadoGasto.APROBADO)) {
            l.agregarDetalle(c.getTipo() + " (" + c.getFecha() + ")", c.getMonto());
        }
        l.calcular(s.getTarifa());
        repo.guardar(l);
        return l;
    }

    /**
     * RF39
     */
    public List<Liquidacion> listar() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.listar();
    }

    // Sobrecarga: por rango de fechas (AAAA-MM-DD)
    public List<Liquidacion> listar(String desde, String hasta)
            throws ValidacionException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        LocalDate d = Validador.aFecha(desde, "Fecha desde");
        LocalDate h = Validador.aFecha(hasta, "Fecha hasta");
        if (h.isBefore(d)) {
            throw new ValidacionException("La fecha final no puede ser anterior a la inicial.");
        }
        return repo.buscar(d, h);
    }

    public Liquidacion buscarPorServicio(int idServicio)
            throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Liquidacion l = repo.buscarPorServicio(idServicio);
        if (l == null) {
            throw new RegistroNoEncontradoException("El servicio aun no tiene liquidacion.");
        }
        return l;
    }
}
