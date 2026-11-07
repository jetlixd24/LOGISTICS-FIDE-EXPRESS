package logisticsfideexpress.vista.componentes;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatos comunes de las ventanas: moneda y fechas. */
public final class Formato {

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formato() { }

    /** Ejemplo: S/ 1250.50 */
    public static String moneda(double valor) {
        return String.format(Locale.US, "S/ %.2f", valor);
    }
}
