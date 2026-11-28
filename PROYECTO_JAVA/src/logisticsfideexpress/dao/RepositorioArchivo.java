/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import logisticsfideexpress.util.ArchivoUtil;

/**
 * Base generica: mantiene un ArrayList en memoria, un HashMap por id y
 * sincroniza con un archivo de texto en cada cambio.
 * Cada repositorio hijo solo define como convertir objeto <-> linea.
 * IMPORTANTE: el hijo debe llamar a cargar() al final de su constructor.
 */
public abstract class RepositorioArchivo<T> implements GenericDAO<T> {

    private final String ruta;
    protected final List<T> items = new ArrayList<>();
    protected final Map<Integer, T> indice = new HashMap<>();

    protected RepositorioArchivo(String ruta) {
        this.ruta = ruta;
    }

    protected abstract String aLinea(T entidad);

    protected abstract T deLinea(String[] campos);

    protected abstract int idDe(T entidad);

    protected abstract void fijarId(T entidad, int id);

    protected abstract void marcarBaja(T entidad);

    /** Lee el archivo y llena la lista (se llama al iniciar). */
    protected void cargar() {
        items.clear();
        indice.clear();
        for (String linea : ArchivoUtil.leerLineas(ruta)) {
            try {
                T entidad = deLinea(ArchivoUtil.partir(linea));
                items.add(entidad);
                indice.put(idDe(entidad), entidad);
            } catch (RuntimeException e) {
                System.err.println("Linea ignorada en " + ruta + ": " + linea + " (" + e.getMessage() + ")");
            }
        }
    }

    /** Escribe toda la lista en el archivo. */
    protected void persistir() {
        List<String> lineas = new ArrayList<>();
        for (T entidad : items) {
            lineas.add(aLinea(entidad));
        }
        ArchivoUtil.escribirLineas(ruta, lineas);
    }

    @Override
    public void guardar(T entidad) {
        if (idDe(entidad) <= 0) {
            fijarId(entidad, siguienteId());
        }
        items.add(entidad);
        indice.put(idDe(entidad), entidad);
        persistir();
    }

    @Override
    public T buscarPorId(int id) {
        return indice.get(id);
    }

    @Override
    public List<T> listar() {
        return new ArrayList<>(items);
    }

    @Override
    public void actualizar(T entidad) {
        int id = idDe(entidad);
        for (int i = 0; i < items.size(); i++) {
            if (idDe(items.get(i)) == id) {
                items.set(i, entidad);
                indice.put(id, entidad);
                persistir();
                return;
            }
        }
        throw new IllegalArgumentException("No existe un registro con id " + id);
    }

    @Override
    public void darDeBaja(int id) {
        T entidad = indice.get(id);
        if (entidad == null) {
            throw new IllegalArgumentException("No existe un registro con id " + id);
        }
        marcarBaja(entidad);
        persistir();
    }

    @Override
    public int siguienteId() {
        int max = 0;
        for (T entidad : items) {
            max = Math.max(max, idDe(entidad));
        }
        return max + 1;
    }
}
