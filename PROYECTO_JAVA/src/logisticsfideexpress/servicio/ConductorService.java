/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.servicio;

import logisticsfideexpress.dao.interfaz.IConductorDAO;
import logisticsfideexpress.dao.interfaz.IVehiculoDAO;
/**
 *
 * @author JETLI
 */
import java.util.List;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.RegistroDuplicadoException;
import logisticsfideexpress.excepciones.RegistroNoEncontradoException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.modelo.Conductor;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.enums.EstadoVehiculo;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.util.Validador;
import logisticsfideexpress.util.ValidadorDocumento;
/**
 * RF14-RF15.
 */
public class ConductorService {

    private final IConductorDAO repo;
    private final IVehiculoDAO vehiculos;

    public ConductorService(IConductorDAO repo, IVehiculoDAO vehiculos) {
        this.repo = repo;
        this.vehiculos = vehiculos;
    }

    /**
     * RF14 (+ RF15 opcional): idVehiculoHabitual = 0 si no se asocia vehiculo.
     */
    public Conductor registrar(String dni, String nombre, String licencia, String categoria,
            int idVehiculoHabitual)
            throws ValidacionException, RegistroDuplicadoException,
            RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        ValidadorDocumento.dni(dni, "DNI");
        Validador.noVacio(nombre, "Nombre");
        Validador.noVacio(licencia, "Licencia");
        Validador.noVacio(categoria, "Categoria");
        if (repo.buscarPorLicencia(licencia.trim().toUpperCase()) != null) {
            throw new RegistroDuplicadoException("La licencia ya esta registrada.");
        }
        Conductor c = new Conductor(dni.trim(), nombre.trim(), licencia.trim().toUpperCase(), categoria.trim());
        if (idVehiculoHabitual > 0) {
            c.setVehiculoHabitual(validarVehiculoAsociable(idVehiculoHabitual, 0));
        }
        repo.guardar(c);
        return c;
    }

    /**
     * RF15: no asocia un vehiculo INACTIVO ni uno ya asociado a otro conductor.
     */
    public void asociarVehiculo(int idConductor, int idVehiculo)
            throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Conductor c = repo.buscarPorId(idConductor);
        if (c == null) {
            throw new RegistroNoEncontradoException("No existe el conductor solicitado.");
        }
        c.setVehiculoHabitual(validarVehiculoAsociable(idVehiculo, idConductor));
        repo.actualizar(c);
    }

    private Vehiculo validarVehiculoAsociable(int idVehiculo, int idConductorActual)
            throws RegistroNoEncontradoException, OperacionNoPermitidaException {
        Vehiculo v = vehiculos.buscarPorId(idVehiculo);
        if (v == null) {
            throw new RegistroNoEncontradoException("El vehiculo seleccionado no existe.");
        }
        if (v.getEstado() == EstadoVehiculo.INACTIVO) {
            throw new OperacionNoPermitidaException("No se puede asociar un vehiculo inactivo.");
        }
        Conductor otro = repo.buscarPorVehiculoHabitual(v);
        if (otro != null && otro.getId() != idConductorActual) {
            throw new OperacionNoPermitidaException("El vehiculo ya esta asociado a " + otro.getNombre() + ".");
        }
        return v;
    }

    public List<Conductor> listar() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.listar();
    }

    // Sobrecarga: con filtro (nombre, DNI o licencia)
    public List<Conductor> listar(String texto) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.buscar(texto);
    }
}
