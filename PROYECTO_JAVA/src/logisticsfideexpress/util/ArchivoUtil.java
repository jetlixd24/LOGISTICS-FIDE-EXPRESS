/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.util;

/**
 *
 * @author JETLI
 */
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ArchivoUtil {

    public static final String SEPARADOR = ";";

    private ArchivoUtil() { }

    /** Lee todas las lineas no vacias. Si el archivo no existe, devuelve lista vacia. */
    public static List<String> leerLineas(String ruta) {
        List<String> lineas = new ArrayList<>();
        Path path = Paths.get(ruta);
        if (!Files.exists(path)) {
            return lineas;
        }
        try {
            for (String linea : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (!linea.trim().isEmpty()) {
                    lineas.add(linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo " + ruta + ": " + e.getMessage());
        }
        return lineas;
    }

    /** Sobrescribe el archivo con las lineas indicadas (crea la carpeta data/ si falta). */
    public static void escribirLineas(String ruta, List<String> lineas) {
        Path path = Paths.get(ruta);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error escribiendo " + ruta + ": " + e.getMessage());
        }
    }

    /** Evita que un ';' o un salto de linea escrito por el usuario rompa el formato. */
    public static String limpiar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace(SEPARADOR, ",").replace("\r", " ").replace("\n", " ").trim();
    }

    /** Parte una linea en campos (el -1 conserva los campos vacios del final). */
    public static String[] partir(String linea) {
        return linea.split(SEPARADOR, -1);
    }
}