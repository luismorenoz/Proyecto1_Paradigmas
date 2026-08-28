import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {
        List<EstacionMetricas> estaciones = leerMetricas("metricas.csv");

        System.out.println("--- Estaciones leidas de metricas.csv ---");
        for (EstacionMetricas e : estaciones) {
            System.out.println(e);
        }

        System.out.println();
        System.out.println("--- Reglas leidas de reglas.txt ---");
        List<LineaRegla> reglasParseadas = ReglaParser.leerReglas("reglas.txt");
        for (LineaRegla r : reglasParseadas) {
            System.out.println(r);
        }
        System.out.println(reglasParseadas.size() + " regla(s) valida(s) cargada(s).");

        System.out.println();
        System.out.println("--- Reglas construidas (objetos polimorficos) ---");
        List<Regla> reglas = new ArrayList<>();
        for (LineaRegla lr : reglasParseadas) {
            reglas.add(ReglaFactory.crear(lr));
        }
        for (Regla r : reglas) {
            System.out.println(r);
        }

        System.out.println();
        System.out.println("--- Evaluacion (regla.evaluar(estacion)) ---");
        List<Alerta> alertas = new ArrayList<>();
        for (EstacionMetricas est : estaciones) {
            for (Regla r : reglas) {
                boolean cumple = r.evaluar(est);
                System.out.printf(Locale.US, "%-12s %-20s valor=%.2f umbral=%.2f -> %s%n",
                    est.getEstacion(), r.getIdentificador(),
                    r.obtenerValor(est), r.getUmbral(),
                    cumple ? "ALERTA" : "ok");

                if (cumple) {
                    alertas.add(new Alerta(est.getEstacion(), r.getIdentificador(),
                        r.obtenerValor(est), r.getUmbral()));
                }
            }
        }

        escribirAlertas("alertas.csv", alertas);
        System.out.println();
        System.out.println(alertas.size() + " alerta(s) escrita(s) en alertas.csv");
    }

    /** Escribe alertas.csv con encabezado, solo con las alertas que dispararon. */
    private static void escribirAlertas(String rutaArchivo, List<Alerta> alertas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaArchivo))) {
            pw.println("ESTACION,REGLA,VALOR_MEDIDO,UMBRAL");
            for (Alerta a : alertas) {
                pw.println(a.toCsvLine());
            }
        } catch (IOException e) {
            System.out.println("ERROR: no se pudo escribir " + rutaArchivo);
            System.exit(1);
        }
    }

    /**
     * Lee metricas.csv (generado por la Etapa 2 en Fortran) y construye
     * la lista de objetos EstacionMetricas.
     */
    private static List<EstacionMetricas> leerMetricas(String rutaArchivo) {
        List<EstacionMetricas> estaciones = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea = br.readLine(); // saltar encabezado

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] campos = linea.split(",");
                if (campos.length != 8) {
                    System.out.println("AVISO: linea con formato inesperado -> " + linea);
                    continue;
                }

                String estacion = campos[0].trim();
                double tempProm = Double.parseDouble(campos[1].trim());
                double tempMax = Double.parseDouble(campos[2].trim());
                double tempMin = Double.parseDouble(campos[3].trim());
                double precipTotal = Double.parseDouble(campos[4].trim());
                double vientoProm = Double.parseDouble(campos[5].trim());
                double vientoMax = Double.parseDouble(campos[6].trim());
                double bateriaProm = Double.parseDouble(campos[7].trim());

                estaciones.add(new EstacionMetricas(estacion, tempProm, tempMax, tempMin,
                        precipTotal, vientoProm, vientoMax, bateriaProm));
            }

        } catch (IOException e) {
            System.out.println("ERROR: no se pudo leer " + rutaArchivo);
            System.out.println("Verifica que el archivo este en la misma carpeta que el .class");
            System.exit(1);
        } catch (NumberFormatException e) {
            System.out.println("ERROR: valor numerico invalido en metricas.csv -> " + e.getMessage());
            System.exit(1);
        }

        return estaciones;
    }
}