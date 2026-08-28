import java.util.Locale;

/**
 * Representa una linea de reglas.txt ya parseada y validada contra la gramatica:
 *   <regla> ::= <identificador> <operador> <numero>
 *
 * Todavia no sabe evaluar nada -- es solo el resultado del parser.
 * En el siguiente paso, ReglaFactory usara esto para construir el objeto
 * Regla (polimorfico) correspondiente.
 */
public class LineaRegla {

    private String identificador;
    private String operador;
    private double numero;

    public LineaRegla(String identificador, String operador, double numero) {
        this.identificador = identificador;
        this.operador = operador;
        this.numero = numero;
    }

    public String getIdentificador() { return identificador; }
    public String getOperador() { return operador; }
    public double getNumero() { return numero; }

    @Override
    public String toString() {
        return String.format(Locale.US, "%s %s %.2f", identificador, operador, numero);
    }
}
