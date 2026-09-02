/*
 * Etapa 4 - Verificacion de integridad (reemplaza MIPS por C puro)
 * Proyecto 1 - Pipeline Poliglota
 *
 * Entrada real (contrato confirmado con la salida de Java, ver Alerta.java):
 *   alertas.csv
 *   ESTACION,REGLA,VALOR_MEDIDO,UMBRAL
 *
 *   REGLA debe ser uno de:
 *     TEMP_ALTA, LLUVIA_INTENSA, VIENTO_FUERTE, BATERIA_BAJA
 *
 *   Java ya filtra: alertas.csv solo trae las alertas que SI dispararon,
 *   en el orden estacion -> regla en que Main.java las evaluo.
 *
 * Formula de verificacion (definida en el enunciado del proyecto):
 *   checksum = checksum + valor
 *   checksum = checksum XOR posicion
 *
 * Uso:
 *   ./etapa4 alertas.csv resultado_final.txt
 *   (si no se dan argumentos, usa alertas.csv y resultado_final.txt por defecto)
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    const char *tipo;
    int valor;
} MapaAlerta;

static const MapaAlerta TABLA_VALORES[] = {
    {"TEMP_ALTA",      10},
    {"LLUVIA_INTENSA", 20},
    {"VIENTO_FUERTE",  30},
    {"BATERIA_BAJA",   40}
};
#define NUM_TIPOS (int)(sizeof(TABLA_VALORES) / sizeof(MapaAlerta))

/* Busca el valor numerico asociado a un tipo de alerta. -1 si no existe. */
int valor_de_alerta(const char *tipo) {
    for (int i = 0; i < NUM_TIPOS; i++) {
        if (strcmp(TABLA_VALORES[i].tipo, tipo) == 0) {
            return TABLA_VALORES[i].valor;
        }
    }
    return -1;
}

/* Quita \r, \n y espacios al final de una linea leida con fgets. */
void limpiar_linea(char *s) {
    size_t len = strlen(s);
    while (len > 0 && (s[len - 1] == '\n' || s[len - 1] == '\r' || s[len - 1] == ' ')) {
        s[len - 1] = '\0';
        len--;
    }
}

int main(int argc, char *argv[]) {
    const char *archivo_entrada = (argc > 1) ? argv[1] : "alertas.csv";
    const char *archivo_salida  = (argc > 2) ? argv[2] : "resultado_final.txt";

    FILE *in = fopen(archivo_entrada, "r");
    if (!in) {
        fprintf(stderr, "[C - Etapa 4] ERROR: no se pudo abrir '%s'\n", archivo_entrada);
        return 1;
    }

    char linea[256];

    /* Saltar encabezado */
    if (fgets(linea, sizeof(linea), in) == NULL) {
        fprintf(stderr, "[C - Etapa 4] ERROR: archivo vacio\n");
        fclose(in);
        return 1;
    }

    printf("[C - Etapa 4] Verificando integridad...\n");

    int posicion = 0;
    unsigned int checksum = 0;
    int total_alertas = 0;
    int errores_formato = 0;

    while (fgets(linea, sizeof(linea), in) != NULL) {
        limpiar_linea(linea);
        if (strlen(linea) == 0) continue;

        char copia[256];
        strncpy(copia, linea, sizeof(copia) - 1);
        copia[sizeof(copia) - 1] = '\0';

        char *estacion      = strtok(copia, ",");
        char *regla         = strtok(NULL, ",");
        char *valor_medido  = strtok(NULL, ",");
        char *umbral        = strtok(NULL, ",");

        if (estacion == NULL || regla == NULL || valor_medido == NULL || umbral == NULL) {
            fprintf(stderr, "  ADVERTENCIA: linea mal formada -> '%s'\n", linea);
            errores_formato++;
            continue;
        }

        posicion++;

        int valor = valor_de_alerta(regla);
        if (valor < 0) {
            fprintf(stderr, "  ADVERTENCIA: regla desconocida '%s' (linea %d)\n", regla, posicion);
            errores_formato++;
            continue;
        }

        checksum = checksum + (unsigned int)valor;
        checksum = checksum ^ (unsigned int)posicion;

        printf("  [%d] %-12s %-16s medido=%-8s umbral=%-8s valor=%-3d checksum parcial=%u\n",
               posicion, estacion, regla, valor_medido, umbral, valor, checksum);

        total_alertas++;
    }

    fclose(in);

    FILE *out = fopen(archivo_salida, "w");
    if (!out) {
        fprintf(stderr, "[C - Etapa 4] ERROR: no se pudo crear '%s'\n", archivo_salida);
        return 1;
    }

    fprintf(out, "VERIFICACION DE INTEGRIDAD - ETAPA 4 (C)\n");
    fprintf(out, "Archivo procesado: %s\n", archivo_entrada);
    fprintf(out, "Alertas procesadas: %d\n", total_alertas);
    fprintf(out, "Lineas con error de formato: %d\n", errores_formato);
    fprintf(out, "CHECKSUM FINAL: %u\n", checksum);
    fprintf(out, "ESTADO: %s\n", (errores_formato == 0) ? "OK" : "OK_CON_ADVERTENCIAS");
    fclose(out);

    printf("[C - Etapa 4] Calculando firma... OK\n");
    printf("[C - Etapa 4] Checksum final = %u\n", checksum);
    printf("[C - Etapa 4] Resultado guardado en '%s'\n", archivo_salida);

    return 0;
}
