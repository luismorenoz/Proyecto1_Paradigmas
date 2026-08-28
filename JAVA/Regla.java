import java.util.Locale;

/**
 * Clase abstracta base de la jerarquia de reglas.
 * Cada subclase concreta sabe evaluar un tipo distinto de condicion,
 * pero todas se usan de la misma forma: regla.evaluar(metrica).
 * Ahi esta el polimorfismo: el codigo que llama a evaluar() no necesita
 * saber que subclase tiene enfrente.
 */
public abstract class Regla {

    protected String identificador;
    protected String operador;
    protected double umbral;

    public Regla(String identificador, String operador, double umbral) {
        this.identificador = identificador;
        this.operador = operador;
        this.umbral = umbral;
    }

    /** Cada subclase decide que valor de la estacion comparar. */
    public abstract boolean evaluar(EstacionMetricas m);

    /** El valor medido que se comparo, usado luego para reportar la alerta. */
    public abstract double obtenerValor(EstacionMetricas m);

    /**
     * Logica de comparacion segun el operador, compartida por todas las
     * subclases (no tiene sentido repetirla en cada una).
     */
    protected boolean comparar(double valor) {
        switch (operador) {
            case ">":  return valor > umbral;
            case "<":  return valor < umbral;
            case ">=": return valor >= umbral;
            case "<=": return valor <= umbral;
            default:   return false;
        }
    }

    public String getIdentificador() { return identificador; }
    public String getOperador() { return operador; }
    public double getUmbral() { return umbral; }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s [%s] %s %.2f",
            getClass().getSimpleName(), identificador, operador, umbral);
    }
}
