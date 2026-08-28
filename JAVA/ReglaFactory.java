/**
 * Fabrica que decide, segun el identificador de la regla, que subclase
 * concreta de Regla instanciar. Es el puente entre el parser (texto)
 * y la jerarquia de clases (objetos polimorficos).
 */
public class ReglaFactory {

    public static Regla crear(LineaRegla lr) {
        String id = lr.getIdentificador();
        String operador = lr.getOperador();
        double umbral = lr.getNumero();

        switch (id) {
            case "TEMP_ALTA":
                return new ReglaTemperatura(id, operador, umbral);
            case "LLUVIA_INTENSA":
                return new ReglaPrecipitacion(id, operador, umbral);
            case "VIENTO_FUERTE":
                return new ReglaViento(id, operador, umbral);
            case "BATERIA_BAJA":
                return new ReglaBateria(id, operador, umbral);
            default:
                // No deberia pasar: ReglaParser ya valido el identificador
                // contra la misma lista de identificadores permitidos.
                return null;
        }
    }
}
