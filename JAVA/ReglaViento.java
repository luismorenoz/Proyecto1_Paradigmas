public class ReglaViento extends Regla {

    public ReglaViento(String identificador, String operador, double umbral) {
        super(identificador, operador, umbral);
    }

    @Override
    public boolean evaluar(EstacionMetricas m) {
        return comparar(m.getVientoMax());
    }

    @Override
    public double obtenerValor(EstacionMetricas m) {
        return m.getVientoMax();
    }
}
