package logisticsfideexpress.vista.componentes;

/** Elemento de un JComboBox: guarda el objeto y el texto que se muestra. */
public final class Opcion<T> {

    private final T valor;
    private final String texto;

    public Opcion(T valor, String texto) {
        this.valor = valor;
        this.texto = texto;
    }

    public T getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return texto;
    }
}
