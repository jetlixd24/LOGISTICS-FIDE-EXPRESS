/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.util;

/**
 *
 * @author JETLI
 */
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import logisticsfideexpress.excepciones.ValidacionException;

public class Validador {

    private Validador() { }

    public static void noVacio(String valor, String campo) throws ValidacionException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException("El campo '" + campo + "' es obligatorio.");
        }
    }

    public static void correoValido(String correo) throws ValidacionException {
        noVacio(correo, "Correo");
        if (!correo.trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new ValidacionException("El correo no tiene un formato valido.");
        }
    }

    public static void longitudMinima(String valor, int min, String campo) throws ValidacionException {
        noVacio(valor, campo);
        if (valor.trim().length() < min) {
            throw new ValidacionException("'" + campo + "' debe tener al menos " + min + " caracteres.");
        }
    }

    /** Solo digitos y con longitud exacta (DNI = 8, RUC = 11). */
    public static void digitosExactos(String valor, int longitud, String campo) throws ValidacionException {
        noVacio(valor, campo);
        if (!valor.trim().matches("\\d{" + longitud + "}")) {
            throw new ValidacionException("'" + campo + "' debe tener exactamente " + longitud + " digitos.");
        }
    }

    public static void soloDigitos(String valor, String campo) throws ValidacionException {
        noVacio(valor, campo);
        if (!valor.trim().matches("\\d+")) {
            throw new ValidacionException("'" + campo + "' solo admite numeros.");
        }
    }

    public static void numeroPositivo(double valor, String campo) throws ValidacionException {
        if (valor <= 0) {
            throw new ValidacionException("'" + campo + "' debe ser mayor a cero.");
        }
    }

    public static void numeroNoNegativo(double valor, String campo) throws ValidacionException {
        if (valor < 0) {
            throw new ValidacionException("'" + campo + "' no puede ser negativo.");
        }
    }

    // Sobrecarga: convierte texto (JTextField) a numero y valida
    public static double numeroPositivo(String texto, String campo) throws ValidacionException {
        double valor = aDouble(texto, campo);
        numeroPositivo(valor, campo);
        return valor;
    }

    public static double numeroNoNegativo(String texto, String campo) throws ValidacionException {
        double valor = aDouble(texto, campo);
        numeroNoNegativo(valor, campo);
        return valor;
    }

    public static double aDouble(String texto, String campo) throws ValidacionException {
        noVacio(texto, campo);
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValidacionException("'" + campo + "' debe ser un numero valido.");
        }
    }

    public static int aEntero(String texto, String campo) throws ValidacionException {
        noVacio(texto, campo);
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new ValidacionException("'" + campo + "' debe ser un numero entero valido.");
        }
    }

    /** Fecha en formato AAAA-MM-DD. */
    public static LocalDate aFecha(String texto, String campo) throws ValidacionException {
        noVacio(texto, campo);
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new ValidacionException("'" + campo + "' debe tener formato AAAA-MM-DD.");
        }
    }

    public static boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
