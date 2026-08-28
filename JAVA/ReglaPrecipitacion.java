public class ReglaPrecipitacion extends Regla {

    public ReglaPrecipitacion(String identificador, String operador, double umbral) {
        super(identificador, operador, umbral);
    }

    @Override
    public boolean evaluar(EstacionMetricas m) {
        return comparar(m.getPrecipTotal());
    }

    @Override
    public double obtenerValor(EstacionMetricas m) {
        return m.getPrecipTotal();
    }
}
