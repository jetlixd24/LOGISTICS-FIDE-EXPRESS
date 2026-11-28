/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.util;

/**
 *
 * @author JETLI
 */
import logisticsfideexpress.excepciones.ValidacionException;

/**
 * Validaciones de documentos peruanos y formatos de contacto. Cada regla tiene
 * una versión booleana (xxxValido) y una que lanza ValidacionException con un
 * mensaje claro para mostrar en la ventana.
 */
public final class ValidadorDocumento {

    private static final int[] PESOS_RUC = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private ValidadorDocumento() { }

    // ---------- versiones booleanas ----------

    public static boolean dniValido(String dni) {
        return dni != null && dni.trim().matches("\\d{8}");
    }

    /** RUC: 11 dígitos, prefijo 10/15/17/20 y dígito verificador (módulo 11). */
    public static boolean rucValido(String ruc) {
        if (ruc == null || !ruc.trim().matches("\\d{11}")) {
            return false;
        }
        String r = ruc.trim();
        String prefijo = r.substring(0, 2);
        if (!(prefijo.equals("10") || prefijo.equals("15") || prefijo.equals("17") || prefijo.equals("20"))) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            suma += (r.charAt(i) - '0') * PESOS_RUC[i];
        }
        int digito = (11 - (suma % 11)) % 10;
        return digito == (r.charAt(10) - '0');
    }

    /** Celular peruano: 9 dígitos que empiezan con 9. */
    public static boolean telefonoValido(String telefono) {
        return telefono != null && telefono.trim().matches("9\\d{8}");
    }

    /** Placa: 3 letras/números, guion y 3 letras/números (ej. ABC-123). */
    public static boolean placaValida(String placa) {
        return placa != null && placa.trim().toUpperCase().matches("[A-Z0-9]{3}-[A-Z0-9]{3}");
    }

    // ---------- versiones que lanzan ValidacionException ----------

    public static void dni(String dni, String campo) throws ValidacionException {
        Validador.noVacio(dni, campo);
        if (!dniValido(dni)) {
            throw new ValidacionException("'" + campo + "' debe tener exactamente 8 digitos (DNI).");
        }
    }

    public static void ruc(String ruc, String campo) throws ValidacionException {
        Validador.noVacio(ruc, campo);
        if (!ruc.trim().matches("\\d{11}")) {
            throw new ValidacionException("'" + campo + "' debe tener exactamente 11 digitos (RUC).");
        }
        if (!rucValido(ruc)) {
            throw new ValidacionException("El RUC ingresado no es valido (prefijo o digito verificador incorrecto).");
        }
    }

    /** Documento de un cliente: DNI (8 dígitos) o RUC (11 dígitos). */
    public static void dniORuc(String documento, String campo) throws ValidacionException {
        Validador.noVacio(documento, campo);
        String d = documento.trim();
        if (d.matches("\\d{8}")) {
            return;
        }
        if (d.matches("\\d{11}")) {
            ruc(d, campo);
            return;
        }
        throw new ValidacionException("'" + campo + "' debe ser un DNI (8 digitos) o un RUC (11 digitos).");
    }

    public static void telefono(String telefono, String campo) throws ValidacionException {
        Validador.noVacio(telefono, campo);
        if (!telefonoValido(telefono)) {
            throw new ValidacionException("'" + campo + "' debe tener 9 digitos y empezar con 9.");
        }
    }

    public static void placa(String placa, String campo) throws ValidacionException {
        Validador.noVacio(placa, campo);
        if (!placaValida(placa)) {
            throw new ValidacionException("'" + campo + "' debe tener el formato ABC-123.");
        }
    }
}
