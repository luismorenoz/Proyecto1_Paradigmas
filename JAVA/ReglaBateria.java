public class ReglaBateria extends Regla {

    public ReglaBateria(String identificador, String operador, double umbral) {
        super(identificador, operador, umbral);
    }

    @Override
    public boolean evaluar(EstacionMetricas m) {
        return comparar(m.getBateriaProm());
    }

    @Override
    public double obtenerValor(EstacionMetricas m) {
        return m.getBateriaProm();
    }
}
