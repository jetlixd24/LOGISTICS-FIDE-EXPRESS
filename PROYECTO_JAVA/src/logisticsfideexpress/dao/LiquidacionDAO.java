/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.dao;

/**
 *
 * @author JETLI
 */
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.modelo.DetalleLiquidacion;
import logisticsfideexpress.modelo.Liquidacion;
import logisticsfideexpress.modelo.ServicioTransporte;
import logisticsfideexpress.util.ArchivoUtil;

/**
 * data/liquidaciones.txt Formato:
 * id;idServicio;fecha;totalGastos;utilidad;detalles detalles = concepto:monto
 * separados por '|' (los detalles viven DENTRO de la liquidacion: composicion)
 * Las liquidaciones no se dan de baja (historico contable).
 */
public class LiquidacionDAO extends RepositorioArchivo<Liquidacion> {

    private static final String RUTA = "data/liquidaciones.txt";
    private static final String SEP_DETALLE = "|";
    private static final String SEP_MONTO = ":";

    private final ServicioDAO servicios;

    public LiquidacionDAO(ServicioDAO servicios) {
        super(RUTA);
        this.servicios = servicios;
        cargar();
    }

    @Override
    protected String aLinea(Liquidacion l) {
        StringBuilder detalles = new StringBuilder();
        for (DetalleLiquidacion d : l.getDetalles()) {
            if (detalles.length() > 0) {
                detalles.append(SEP_DETALLE);
            }
            String concepto = ArchivoUtil.limpiar(d.getConcepto()).replace(SEP_DETALLE, "/").replace(SEP_MONTO, "-");
            detalles.append(concepto).append(SEP_MONTO).append(d.getMonto());
        }
        return String.join(ArchivoUtil.SEPARADOR,
                String.valueOf(l.getId()),
                String.valueOf(l.getServicio().getId()),
                l.getFecha().toString(),
                String.valueOf(l.getTotalGastos()),
                String.valueOf(l.getUtilidad()),
                detalles.toString());
    }

    @Override
    protected Liquidacion deLinea(String[] c) {
        ServicioTransporte servicio = servicios.buscarPorId(Integer.parseInt(c[1]));
        if (servicio == null) {
            throw new IllegalStateException("servicio inexistente");
        }
        Liquidacion l = new Liquidacion(Integer.parseInt(c[0]), servicio, LocalDate.parse(c[2]));
        double totalGastos = Double.parseDouble(c[3]);
        double utilidad = Double.parseDouble(c[4]);
        if (c.length > 5 && !c[5].isEmpty()) {
            for (String detalle : c[5].split("\\" + SEP_DETALLE)) {
                int pos = detalle.lastIndexOf(SEP_MONTO);
                l.agregarDetalle(detalle.substring(0, pos), Double.parseDouble(detalle.substring(pos + 1)));
            }
        }
        // totalGastos y utilidad se recalculan con el mismo metodo del modelo: ingreso = gastos + utilidad
        l.calcular(totalGastos + utilidad);
        return l;
    }

    @Override
    protected int idDe(Liquidacion l) {
        return l.getId();
    }

    @Override
    protected void fijarId(Liquidacion l, int id) {
        l.setId(id);
    }

    @Override
    protected void marcarBaja(Liquidacion l) {
        throw new UnsupportedOperationException("Las liquidaciones no se dan de baja (historico contable).");
    }

    public Liquidacion buscarPorServicio(int idServicio) {
        for (Liquidacion l : items) {
            if (l.getServicio().getId() == idServicio) {
                return l;
            }
        }
        return null;
    }

    // Sobrecarga: por rango de fechas
    public List<Liquidacion> buscar(LocalDate desde, LocalDate hasta) {
        List<Liquidacion> resultado = new ArrayList<>();
        for (Liquidacion l : items) {
            if (!l.getFecha().isBefore(desde) && !l.getFecha().isAfter(hasta)) {
                resultado.add(l);
            }
        }
        return resultado;
    }
}
