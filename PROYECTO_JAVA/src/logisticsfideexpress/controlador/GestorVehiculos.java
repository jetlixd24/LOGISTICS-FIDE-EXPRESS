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
import java.util.List;
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.excepciones.RegistroDuplicadoException;
import logisticsfideexpress.excepciones.RegistroNoEncontradoException;
import logisticsfideexpress.excepciones.ValidacionException;
import logisticsfideexpress.modelo.Transportista;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.VehiculoAfiliado;
import logisticsfideexpress.modelo.VehiculoPropio;
import logisticsfideexpress.modelo.enums.EstadoVehiculo;
import logisticsfideexpress.repository.TransportistaRepository;
import logisticsfideexpress.repository.VehiculoRepository;
import logisticsfideexpress.util.Sesion;
import logisticsfideexpress.util.Validador;

/**
 * RF10-RF13.
 */
public class GestorVehiculos {

    private final VehiculoRepository repo;
    private final TransportistaRepository transportistas;

    public GestorVehiculos(VehiculoRepository repo, TransportistaRepository transportistas) {
        this.repo = repo;
        this.transportistas = transportistas;
    }

    /**
     * RF10: vehiculo propio. kilometraje y fechaCompra (AAAA-MM-DD) pueden ir
     * vacios.
     */
    public VehiculoPropio registrarPropio(String placa, String marca, String capacidadKg,
            String kilometraje, String fechaCompra)
            throws ValidacionException, RegistroDuplicadoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Validador.noVacio(placa, "Placa");
        Validador.noVacio(marca, "Marca");
        double capacidad = Validador.numeroPositivo(capacidadKg, "Capacidad (kg)");
        double km = Validador.esVacio(kilometraje) ? 0 : Validador.numeroNoNegativo(kilometraje, "Kilometraje");
        LocalDate fecha = Validador.esVacio(fechaCompra) ? null : Validador.aFecha(fechaCompra, "Fecha de compra");
        validarPlacaUnica(placa);
        VehiculoPropio v = new VehiculoPropio(placa.trim().toUpperCase(), marca.trim(), capacidad, km, fecha);
        repo.guardar(v);
        return v;
    }

    /**
     * RF11: exige un transportista existente y placa unica.
     */
    public VehiculoAfiliado registrarAfiliado(int idTransportista, String placa, String marca,
            String capacidadKg, String tarifaAcordada)
            throws ValidacionException, RegistroDuplicadoException,
            RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Transportista t = transportistas.buscarPorId(idTransportista);
        if (t == null) {
            throw new RegistroNoEncontradoException("El transportista seleccionado no existe.");
        }
        Validador.noVacio(placa, "Placa");
        Validador.noVacio(marca, "Marca");
        double capacidad = Validador.numeroPositivo(capacidadKg, "Capacidad (kg)");
        double tarifa = Validador.numeroPositivo(tarifaAcordada, "Tarifa acordada");
        validarPlacaUnica(placa);
        VehiculoAfiliado v = new VehiculoAfiliado(placa.trim().toUpperCase(), marca.trim(), capacidad, tarifa, t);
        repo.guardar(v);
        t.agregarVehiculo(v);
        return v;
    }

    private void validarPlacaUnica(String placa) throws RegistroDuplicadoException {
        if (repo.buscarPorPlaca(placa) != null) {
            throw new RegistroDuplicadoException("La placa ya esta registrada.");
        }
    }

    /**
     * RF12
     */
    public List<Vehiculo> listar() throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.listar();
    }

    // Sobrecarga: filtro por estado
    public List<Vehiculo> listar(EstadoVehiculo estado) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.buscar(estado);
    }

    // Sobrecarga: filtro por texto (placa o marca)
    public List<Vehiculo> listar(String texto) throws OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        return repo.buscar(texto);
    }

    /**
     * RF13: un vehiculo EN_SERVICIO no puede pasar a INACTIVO.
     */
    public void modificar(int id, String marca, String capacidadKg, EstadoVehiculo nuevoEstado)
            throws ValidacionException, RegistroNoEncontradoException, OperacionNoPermitidaException {
        Sesion.exigirAdministrador();
        Vehiculo v = repo.buscarPorId(id);
        if (v == null) {
            throw new RegistroNoEncontradoException("No existe el vehiculo solicitado.");
        }
        Validador.noVacio(marca, "Marca");
        double capacidad = Validador.numeroPositivo(capacidadKg, "Capacidad (kg)");
        if (v.getEstado() == EstadoVehiculo.EN_SERVICIO && nuevoEstado == EstadoVehiculo.INACTIVO) {
            throw new OperacionNoPermitidaException("Un vehiculo en servicio no puede pasar a inactivo.");
        }
        v.setMarca(marca.trim());
        v.setCapacidadKg(capacidad);
        v.setEstado(nuevoEstado);
        repo.actualizar(v);
    }
}
