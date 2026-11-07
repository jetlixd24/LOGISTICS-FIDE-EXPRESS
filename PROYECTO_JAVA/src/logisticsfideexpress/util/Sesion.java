/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.util;

/**
 *
 * @author JETLI
 */
import logisticsfideexpress.excepciones.OperacionNoPermitidaException;
import logisticsfideexpress.modelo.Usuario;
import logisticsfideexpress.modelo.enums.TipoUsuario;

/** Usuario actual de la aplicacion (RF01, RF03, RF04). */
public class Sesion {

    private static Usuario usuarioActual;

    private Sesion() { }

    public static void iniciar(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static void cerrar() {
        usuarioActual = null;
    }

    public static Usuario getUsuario() {
        return usuarioActual;
    }

    public static boolean haySesion() {
        return usuarioActual != null;
    }

    public static boolean esAdministrador() {
        return usuarioActual != null && usuarioActual.getTipo() == TipoUsuario.ADMINISTRADOR;
    }

    public static boolean esCliente() {
        return usuarioActual != null && usuarioActual.getTipo() == TipoUsuario.CLIENTE;
    }

    /** RNF03: los gestores llaman a este metodo antes de operaciones del administrador. */
    public static void exigirAdministrador() throws OperacionNoPermitidaException {
        if (!esAdministrador()) {
            throw new OperacionNoPermitidaException("Esta operacion es solo para el administrador.");
        }
    }

    public static void exigirCliente() throws OperacionNoPermitidaException {
        if (!esCliente()) {
            throw new OperacionNoPermitidaException("Esta operacion es solo para clientes.");
        }
    }
}
