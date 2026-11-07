/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logisticsfideexpress.repository;

/**
 *
 * @author JETLI
 */
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.modelo.Pago;
import logisticsfideexpress.util.ArchivoUtil;

/**
 * data/pagos.txt Formato: id;idFactura;fecha;monto;metodo Los pagos no se
 * borran ni se dan de baja (historico contable).
 */
public class PagoRepository extends RepositorioArchivo<Pago> {

    private static final String RUTA = "data/pagos.txt";
    private final FacturaRepository facturas;

    public PagoRepository(FacturaRepository facturas) {
        super(RUTA);
        this.facturas = facturas;
        cargar();
    }

    @Override
    protected String aLinea(Pago p) {
        return String.join(ArchivoUtil.SEPARADOR,
                String.valueOf(p.getId()),
                String.valueOf(p.getFactura().getId()),
                p.getFecha().toString(),
                String.valueOf(p.getMonto()),
                ArchivoUtil.limpiar(p.getMetodo()));
    }

    @Override
    protected Pago deLinea(String[] c) {
        Factura factura = facturas.buscarPorId(Integer.parseInt(c[1]));
        if (factura == null) {
            throw new IllegalStateException("factura inexistente");
        }
        return new Pago(Integer.parseInt(c[0]), factura, LocalDate.parse(c[2]),
                Double.parseDouble(c[3]), c[4]);
    }

    @Override
    protected int idDe(Pago p) {
        return p.getId();
    }

    @Override
    protected void fijarId(Pago p, int id) {
        p.setId(id);
    }

    @Override
    protected void marcarBaja(Pago p) {
        throw new UnsupportedOperationException("Los pagos no se dan de baja (historico contable).");
    }

    public List<Pago> buscarPorFactura(int idFactura) {
        List<Pago> resultado = new ArrayList<>();
        for (Pago p : items) {
            if (p.getFactura().getId() == idFactura) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Pagos de las facturas de un tercero (por ejemplo, los pagos de un
     * cliente).
     */
    public List<Pago> buscarPorTercero(String terceroTipo, int terceroId) {
        List<Pago> resultado = new ArrayList<>();
        for (Pago p : items) {
            Factura f = p.getFactura();
            if (f.getTerceroTipo().equals(terceroTipo) && f.getTerceroId() == terceroId) {
                resultado.add(p);
            }
        }
        return resultado;
    }
}
