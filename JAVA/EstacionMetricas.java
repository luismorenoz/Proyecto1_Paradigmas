import java.util.Locale;

/**
 * Representa las metricas de una estacion, tal como vienen en metricas.csv
 * (la salida de la Etapa 2 en Fortran).
 */
public class EstacionMetricas {

    private String estacion;
    private double tempProm;
    private double tempMax;
    private double tempMin;
    private double precipTotal;
    private double vientoProm;
    private double vientoMax;
    private double bateriaProm;

    public EstacionMetricas(String estacion, double tempProm, double tempMax, double tempMin,
                             double precipTotal, double vientoProm, double vientoMax,
                             double bateriaProm) {
        this.estacion = estacion;
        this.tempProm = tempProm;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.precipTotal = precipTotal;
        this.vientoProm = vientoProm;
        this.vientoMax = vientoMax;
        this.bateriaProm = bateriaProm;
    }

    public String getEstacion() { return estacion; }
    public double getTempProm() { return tempProm; }
    public double getTempMax() { return tempMax; }
    public double getTempMin() { return tempMin; }
    public double getPrecipTotal() { return precipTotal; }
    public double getVientoProm() { return vientoProm; }
    public double getVientoMax() { return vientoMax; }
    public double getBateriaProm() { return bateriaProm; }

    @Override
    public String toString() {
        return String.format(Locale.US,
            "%-12s tempProm=%.2f tempMax=%.2f tempMin=%.2f precipTotal=%.2f " +
            "vientoProm=%.2f vientoMax=%.2f bateriaProm=%.2f",
            estacion, tempProm, tempMax, tempMin, precipTotal, vientoProm, vientoMax, bateriaProm
        );
    }
}