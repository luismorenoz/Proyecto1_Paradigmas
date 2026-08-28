import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Parser del mini-lenguaje de reglas:
 *
 *   <regla>        ::= <identificador> <operador> <numero>
 *   <operador>     ::= ">" | "<" | ">=" | "<="
 *   <identificador> ::= "TEMP_ALTA" | "LLUVIA_INTENSA" | "VIENTO_FUERTE" | "BATERIA_BAJA"
 */
public class ReglaParser {

    private static final Set<String> IDENTIFICADORES_VALIDOS = Set.of(
        "TEMP_ALTA", "LLUVIA_INTENSA", "VIENTO_FUERTE", "BATERIA_BAJA"
    );

    private static final Set<String> OPERADORES_VALIDOS = Set.of(
        ">", "<", ">=", "<="
    );

    /**
     * Lee reglas.txt linea por linea. Las lineas validas se agregan a la lista
     * resultante; las invalidas se reportan por consola y se ignoran.
     */
    public static List<LineaRegla> leerReglas(String rutaArchivo) {
        List<LineaRegla> reglas = new ArrayList<>();
        int numeroLinea = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                if (linea.trim().isEmpty()) continue;

                LineaRegla parseada = parsearLinea(linea);
                if (parseada != null) {
                    reglas.add(parseada);
                } else {
                    System.out.println("LINEA INVALIDA (linea " + numeroLinea + "): \"" + linea + "\"");
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: no se pudo leer " + rutaArchivo);
            System.exit(1);
        }

        return reglas;
    }

    /**
     * Valida una sola linea contra la gramatica. Devuelve null si no es valida
     * (en vez de lanzar excepcion), para que el llamador decida como reportarlo.
     */
    public static LineaRegla parsearLinea(String linea) {
        String[] tokens = linea.trim().split("\\s+");

        // <regla> ::= <identificador> <operador> <numero>  -> siempre 3 tokens
        if (tokens.length != 3) {
            return null;
        }

        String identificador = tokens[0];
        String operador = tokens[1];
        String numeroStr = tokens[2];

        if (!IDENTIFICADORES_VALIDOS.contains(identificador)) {
            return null;
        }

        if (!OPERADORES_VALIDOS.contains(operador)) {
            return null;
        }

        double numero;
        try {
            numero = Double.parseDouble(numeroStr);
        } catch (NumberFormatException e) {
            return null;
        }

        return new LineaRegla(identificador, operador, numero);
    }
}
