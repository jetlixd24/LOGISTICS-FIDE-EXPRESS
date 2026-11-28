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
import logisticsfideexpress.modelo.Factura;
import logisticsfideexpress.modelo.enums.EstadoFactura;
import logisticsfideexpress.modelo.enums.TipoFactura;
import logisticsfideexpress.util.ArchivoUtil;

/**
 * data/facturas.txt Formato:
 * id;numero;tipo;fecha;terceroTipo;terceroId;terceroNombre;total;saldo;estado
 * Las facturas no se borran ni se dan de baja (historico contable).
 */
public class FacturaDAO extends RepositorioArchivo<Factura> {

    private static final String RUTA = "data/facturas.txt";

    public FacturaDAO() {
        super(RUTA);
        cargar();
    }

    @Override
    protected String aLinea(Factura f) {
        return String.join(ArchivoUtil.SEPARADOR,
                String.valueOf(f.getId()),
                ArchivoUtil.limpiar(f.getNumero()),
                f.getTipo().name(),
                f.getFecha().toString(),
                ArchivoUtil.limpiar(f.getTerceroTipo()),
                String.valueOf(f.getTerceroId()),
                ArchivoUtil.limpiar(f.getTerceroNombre()),
                String.valueOf(f.getTotal()),
                String.valueOf(f.getSaldo()),
                f.getEstado().name());
    }

    @Override
    protected Factura deLinea(String[] c) {
        Factura f = new Factura(Integer.parseInt(c[0]), c[1], TipoFactura.valueOf(c[2]),
                LocalDate.parse(c[3]), c[4], Integer.parseInt(c[5]), c[6], Double.parseDouble(c[7]));
        f.setSaldo(Double.parseDouble(c[8]));
        f.setEstado(EstadoFactura.valueOf(c[9]));
        return f;
    }

    @Override
    protected int idDe(Factura f) {
        return f.getId();
    }

    @Override
    protected void fijarId(Factura f, int id) {
        f.setId(id);
    }

    @Override
    protected void marcarBaja(Factura f) {
        throw new UnsupportedOperationException("Las facturas no se dan de baja (historico contable).");
    }

    public Factura buscarPorNumero(String numero) {
        if (numero == null) {
            return null;
        }
        for (Factura f : items) {
            if (f.getNumero().equalsIgnoreCase(numero.trim())) {
                return f;
            }
        }
        return null;
    }

    /**
     * True si ya existe una factura con ese numero para ese mismo tercero.
     */
    public boolean existe(String numero, String terceroTipo, int terceroId) {
        for (Factura f : items) {
            if (f.getNumero().equalsIgnoreCase(numero.trim()) && f.getTerceroTipo().equals(terceroTipo)
                    && f.getTerceroId() == terceroId) {
                return true;
            }
        }
        return false;
    }

    // Sobrecarga: por tipo (VENTA o COMPRA)
    public List<Factura> buscar(TipoFactura tipo) {
        List<Factura> resultado = new ArrayList<>();
        for (Factura f : items) {
            if (f.getTipo() == tipo) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    // Sobrecarga: por estado
    public List<Factura> buscar(EstadoFactura estado) {
        List<Factura> resultado = new ArrayList<>();
        for (Factura f : items) {
            if (f.getEstado() == estado) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    /**
     * Facturas de un tercero (por ejemplo, las facturas de venta de un
     * cliente).
     */
    public List<Factura> buscarPorTercero(String terceroTipo, int terceroId) {
        List<Factura> resultado = new ArrayList<>();
        for (Factura f : items) {
            if (f.getTerceroTipo().equals(terceroTipo) && f.getTerceroId() == terceroId) {
                resultado.add(f);
            }
        }
        return resultado;
    }
}
