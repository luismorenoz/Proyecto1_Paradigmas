public class ReglaTemperatura extends Regla {

    public ReglaTemperatura(String identificador, String operador, double umbral) {
        super(identificador, operador, umbral);
    }

    @Override
    public boolean evaluar(EstacionMetricas m) {
        return comparar(m.getTempMax());
    }

    @Override
    public double obtenerValor(EstacionMetricas m) {
        return m.getTempMax();
    }
}
