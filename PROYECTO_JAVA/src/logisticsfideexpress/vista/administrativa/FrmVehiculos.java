package logisticsfideexpress.vista.administrativa;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import logisticsfideexpress.controlador.GestorLogistico;
import logisticsfideexpress.modelo.Transportista;
import logisticsfideexpress.modelo.Vehiculo;
import logisticsfideexpress.modelo.VehiculoAfiliado;
import logisticsfideexpress.modelo.VehiculoPropio;
import logisticsfideexpress.modelo.enums.EstadoVehiculo;
import logisticsfideexpress.vista.componentes.BotonRedondeado;
import logisticsfideexpress.vista.componentes.Estilo;
import logisticsfideexpress.vista.componentes.Formulario;
import logisticsfideexpress.vista.componentes.FrmMantenimiento;

/** RF10 a RF13: registrar vehiculos propios y afiliados, modificar, cambiar estado y consultar. */
public class FrmVehiculos extends FrmMantenimiento {

    private static final String PROPIO = "Propio";
    private static final String AFILIADO = "Afiliado";
    private static final String TODOS = "Todos";

    private final GestorLogistico gestor;
    private final DefaultTableModel modelo = modelo("ID", "Tipo", "Placa", "Marca", "Capacidad (kg)",
            "Estado", "Detalle");
    private final JTable tabla = new JTable(modelo);
    private final JTextField txtBuscar = Estilo.campo();
    private final JComboBox<Object> cmbFiltro = new JComboBox<>();
    private final JComboBox<String> cmbTipo = new JComboBox<>(new String[]{PROPIO, AFILIADO});
    private final JTextField txtPlaca = Estilo.campo();
    private final JTextField txtMarca = Estilo.campo();
    private final JTextField txtCapacidad = Estilo.campo();
    private final JComboBox<EstadoVehiculo> cmbEstado = new JComboBox<>(EstadoVehiculo.values());
    private final JTextField txtKm = Estilo.campo();
    private final JTextField txtFecha = Estilo.campo();
    private final JComboBox<Transportista> cmbTransportista = new JComboBox<>();
    private final JTextField txtTarifa = Estilo.campo();
    private final CardLayout cartas = new CardLayout();
    private final JPanel panelTipo = new JPanel(cartas);
    private final BotonRedondeado btnRegistrar = Estilo.botonPrimario("Registrar vehiculo");
    private final BotonRedondeado btnModificar = Estilo.botonSecundario("Guardar cambios");
    private final BotonRedondeado btnNuevo = Estilo.botonSecundario("Nuevo / limpiar");
    private int idSeleccionado = 0;

    public FrmVehiculos(GestorLogistico gestor) {
        super("Gestion de vehiculos", 1280, 780);
        this.gestor = gestor;

        Formulario propio = new Formulario();
        propio.agregar("Kilometraje (opcional)", txtKm);
        propio.agregar("Fecha de compra AAAA-MM-DD (opcional)", txtFecha);
        Formulario afiliado = new Formulario();
        afiliado.agregar("Transportista", cmbTransportista);
        afiliado.agregar("Tarifa acordada", txtTarifa);
        panelTipo.setOpaque(false);
        panelTipo.add(propio, PROPIO);
        panelTipo.add(afiliado, AFILIADO);

        cmbEstado.setFont(logisticsfideexpress.vista.componentes.Tema.NORMAL);
        cmbTipo.setFont(logisticsfideexpress.vista.componentes.Tema.NORMAL);
        cmbTransportista.setFont(logisticsfideexpress.vista.componentes.Tema.NORMAL);

        Formulario f = new Formulario();
        f.agregar("Tipo de vehiculo", cmbTipo);
        f.agregar("Placa", txtPlaca);
        f.agregar("Marca", txtMarca);
        f.agregar("Capacidad (kg)", txtCapacidad);
        f.agregarSolo(panelTipo, 0);
        f.agregar("Estado (solo al modificar)", cmbEstado);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 8));
        botones.setOpaque(false);
        botones.add(btnRegistrar);
        botones.add(btnModificar);
        botones.add(btnNuevo);

        cmbFiltro.addItem(TODOS);
        for (EstadoVehiculo e : EstadoVehiculo.values()) {
            cmbFiltro.addItem(e);
        }
        cmbFiltro.setFont(logisticsfideexpress.vista.componentes.Tema.NORMAL);
        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(Estilo.etiqueta("Buscar (placa o marca):  "));
        barra.add(izq, BorderLayout.WEST);
        barra.add(txtBuscar, BorderLayout.CENTER);
        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        der.setOpaque(false);
        der.add(Estilo.etiqueta("Estado:"));
        der.add(cmbFiltro);
        barra.add(der, BorderLayout.EAST);

        montar("Datos del vehiculo", f, botones, barra, tabla);

        cmbTipo.addActionListener(e -> cartas.show(panelTipo, (String) cmbTipo.getSelectedItem()));
        btnRegistrar.addActionListener(e -> registrar());
        btnModificar.addActionListener(e -> modificar());
        btnNuevo.addActionListener(e -> limpiar());
        cmbFiltro.addActionListener(e -> refrescar());
        Estilo.alEscribir(txtBuscar, this::refrescar);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionar();
            }
        });
        cargarTransportistas();
        limpiar();
        refrescar();
    }

    private void cargarTransportistas() {
        try {
            cmbTransportista.removeAllItems();
            for (Transportista t : gestor.getTransportistas().listar()) {
                cmbTransportista.addItem(t);
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void refrescar() {
        try {
            String t = txtBuscar.getText().trim();
            List<Vehiculo> lista = t.isEmpty() ? gestor.getVehiculos().listar() : gestor.getVehiculos().listar(t);
            Object filtro = cmbFiltro.getSelectedItem();
            modelo.setRowCount(0);
            for (Vehiculo v : lista) {
                if (filtro instanceof EstadoVehiculo && v.getEstado() != filtro) {
                    continue;
                }
                modelo.addRow(new Object[]{v.getId(), v.getTipo(), v.getPlaca(), v.getMarca(),
                    v.getCapacidadKg(), v.getEstado(), detalle(v)});
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private String detalle(Vehiculo v) {
        if (v instanceof VehiculoPropio) {
            VehiculoPropio p = (VehiculoPropio) v;
            return "Km: " + p.getKilometraje() + (p.getFechaCompra() != null ? "  |  Compra: " + p.getFechaCompra() : "");
        }
        if (v instanceof VehiculoAfiliado) {
            VehiculoAfiliado a = (VehiculoAfiliado) v;
            String razon = a.getTransportista() != null ? a.getTransportista().getRazonSocial() : "-";
            return razon + "  |  Tarifa: " + a.getTarifaAcordada();
        }
        return "";
    }

    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        idSeleccionado = (int) modelo.getValueAt(fila, 0);
        txtPlaca.setText(String.valueOf(modelo.getValueAt(fila, 2)));
        txtMarca.setText(String.valueOf(modelo.getValueAt(fila, 3)));
        txtCapacidad.setText(String.valueOf(modelo.getValueAt(fila, 4)));
        cmbEstado.setSelectedItem(modelo.getValueAt(fila, 5));
        modoEdicion(true);
    }

    private void limpiar() {
        idSeleccionado = 0;
        tabla.clearSelection();
        for (JTextField t : new JTextField[]{txtPlaca, txtMarca, txtCapacidad, txtKm, txtFecha, txtTarifa}) {
            t.setText("");
        }
        cmbEstado.setSelectedItem(EstadoVehiculo.DISPONIBLE);
        modoEdicion(false);
    }

    /** En edicion solo se cambian marca, capacidad y estado (regla del gestor). */
    private void modoEdicion(boolean edicion) {
        cmbTipo.setEnabled(!edicion);
        txtPlaca.setEnabled(!edicion);
        txtKm.setEnabled(!edicion);
        txtFecha.setEnabled(!edicion);
        cmbTransportista.setEnabled(!edicion);
        txtTarifa.setEnabled(!edicion);
        cmbEstado.setEnabled(edicion);
        btnRegistrar.setEnabled(!edicion);
        btnModificar.setEnabled(edicion);
    }

    private void registrar() {
        try {
            if (PROPIO.equals(cmbTipo.getSelectedItem())) {
                gestor.getVehiculos().registrarPropio(txtPlaca.getText(), txtMarca.getText(),
                        txtCapacidad.getText(), txtKm.getText(), txtFecha.getText());
            } else {
                Transportista t = (Transportista) cmbTransportista.getSelectedItem();
                if (t == null) {
                    error("Primero registra un transportista para poder afiliar un vehiculo.");
                    return;
                }
                gestor.getVehiculos().registrarAfiliado(t.getId(), txtPlaca.getText(), txtMarca.getText(),
                        txtCapacidad.getText(), txtTarifa.getText());
            }
            info("Vehiculo registrado correctamente.");
            limpiar();
            cargarTransportistas();
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void modificar() {
        try {
            gestor.getVehiculos().modificar(idSeleccionado, txtMarca.getText(), txtCapacidad.getText(),
                    (EstadoVehiculo) cmbEstado.getSelectedItem());
            info("Vehiculo actualizado.");
            limpiar();
            refrescar();
        } catch (Exception ex) {
            error(ex);
        }
    }
}
