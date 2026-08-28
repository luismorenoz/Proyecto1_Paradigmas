import java.util.Locale;

/**
 * Representa una alerta activa: una regla que se cumplio para una estacion.
 * Es el resultado final del motor de reglas, listo para escribirse en
 * alertas.csv y pasarse a la Etapa 4 (C).
 */
public class Alerta {

    private String estacion;
    private String regla;
    private double valorMedido;
    private double umbral;

    public Alerta(String estacion, String regla, double valorMedido, double umbral) {
        this.estacion = estacion;
        this.regla = regla;
        this.valorMedido = valorMedido;
        this.umbral = umbral;
    }

    /** Linea lista para escribir en el CSV (sin encabezado). */
    public String toCsvLine() {
        return String.format(Locale.US, "%s,%s,%.2f,%.2f", estacion, regla, valorMedido, umbral);
    }

    @Override
    public String toString() {
        return toCsvLine();
    }
}
