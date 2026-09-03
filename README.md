# PolyFlow — Pipeline Poliglota

**Proyecto 1 · EIF400 Paradigmas de Programacion**

Sistema donde cuatro lenguajes distintos colaboran, cada uno resolviendo una etapa de un mismo problema. La salida de cada etapa es la entrada de la siguiente: no son cuatro programas independientes, son un solo sistema construido con varios lenguajes.

```
estaciones_raw.csv
       |
       v
  [BASIC-256]  Limpieza y validacion
       |
       v
datos_normalizados.csv
       |
       v
   [FORTRAN]   Procesamiento numerico
       |
       v
  metricas.csv
       |
       v
    [JAVA]     Motor de reglas (POO + polimorfismo)
       |
       v
  alertas.csv
       |
       v
      [C]      Verificacion de integridad (checksum)
       |
       v
resultado_final.txt
```

La etapa 4 esta escrita en C en lugar de ensamblador MIPS (variante autorizada del proyecto), manteniendo el mismo objetivo: un nivel de abstraccion cercano al hardware para el calculo final de verificacion.

## Estructura del repositorio

```
Proyecto1_Paradigmas/
├── Basic-256/     Etapa 1 - codigo fuente .kbs + datos de entrada
├── FORTRAN/       Etapa 2 - codigo fuente .f90
├── JAVA/          Etapa 3 - codigo fuente .java (motor de reglas)
├── C/             Etapa 4 - codigo fuente .c
├── data/          Carpeta de trabajo compartida (se regenera en cada corrida, no se versiona)
└── run_pipeline.bat   Orquestador: ejecuta las 4 etapas en orden
```

## Contratos de datos entre etapas

Ningun programa conoce el codigo interno de la etapa anterior. Cada uno solo conoce el formato de archivo acordado (el "contrato"). Esto es lo que permite que lenguajes tan distintos colaboren sin acoplarse entre si.

**estaciones_raw.csv** (entrada del sistema)

```
ID,ESTACION,TEMPERATURA,PRECIPITACION,VIENTO,BATERIA
```

**datos_normalizados.csv** (BASIC-256 → FORTRAN)

```
ID,ESTACION,TEMPERATURA,PRECIPITACION,VIENTO,BATERIA
```

Mismo formato que la entrada, pero solo con los registros que pasaron la validacion. Los registros descartados quedan en `errores.csv` junto con el motivo (`CAMPO_FALTANTE`, `TEMPERATURA_INVALIDA`, `PRECIPITACION_NEGATIVA`, `VIENTO_NEGATIVO`, `BATERIA_FUERA_RANGO`, `VALOR_NO_NUMERICO`).

**metricas.csv** (FORTRAN → JAVA)

```
ESTACION,TEMP_PROM,TEMP_MAX,TEMP_MIN,PRECIP_TOTAL,VIENTO_PROM,VIENTO_MAX,BATERIA_PROM
```

**reglas.txt** (entrada auxiliar de la etapa Java, mini-lenguaje independiente)

```
<regla>         ::= <identificador> <operador> <numero>
<operador>      ::= ">" | "<" | ">=" | "<="
<identificador> ::= "TEMP_ALTA" | "LLUVIA_INTENSA" | "VIENTO_FUERTE" | "BATERIA_BAJA"
```

**alertas.csv** (JAVA → C)

```
ESTACION,REGLA,VALOR_MEDIDO,UMBRAL
```

Solo contiene las alertas que efectivamente dispararon (Java ya filtra).

**resultado_final.txt** (salida del sistema, generada por C)

Checksum de verificacion calculado sobre `alertas.csv`:

```
checksum = checksum + valor
checksum = checksum XOR posicion
```

donde `valor` viene de la tabla `TEMP_ALTA=10, LLUVIA_INTENSA=20, VIENTO_FUERTE=30, BATERIA_BAJA=40` y `posicion` es el numero de fila procesada (1, 2, 3...).

## Como ejecutar el pipeline completo

### Requisitos

| Herramienta | Uso | Notas |
|---|---|---|
| BASIC-256 | Etapa 1 | Se busca en `C:\Program Files\BASIC256\basic256.exe`. Si tu instalacion quedo en otra ruta, edita la variable `BASIC256_EXE` al inicio del bloque de la Etapa 1 en `run_pipeline.bat`. |
| gfortran (o equivalente) | Compilar `etapa2.f90` si no existe `etapa2.exe` | El `.exe` ya viene compilado en el repo; solo hace falta recompilar si se modifica el `.f90`. |
| JDK (no solo JRE) | Etapa 3 | Se necesita `javac`, no solo `java`. El script compila los `.java` en cada corrida. |
| GCC (o equivalente) | Compilar `etapa4.c` si no existe `etapa4.exe` | El `.exe` ya viene compilado en el repo. |

### Ejecucion

Desde la raiz del repositorio (`Proyecto1_Paradigmas\`), en CMD:

```
run_pipeline.bat
```

Salida esperada:

```
[BASIC-256] Procesando datos... OK
[FORTRAN] Calculando metricas... OK
[JAVA] Evaluando reglas... OK
[C] Calculando firma... OK

PIPELINE COMPLETADO
```

El resultado final queda en `data\resultado_final.txt`. Todos los archivos intermedios de cada corrida quedan en `data\` (no se versionan en git; se regeneran cada vez que se corre el script).

### Si el pipeline se detiene con un error

El script valida el codigo de salida y la existencia del archivo esperado despues de cada etapa. Si una etapa falla, el pipeline se detiene ahi mismo con `PIPELINE DETENIDO POR ERROR` y **no** ejecuta las etapas siguientes con datos incompletos o de una corrida anterior. El mensaje de error indica en cual etapa ocurrio el problema.

## Por que cada lenguaje resuelve su etapa

Ver `Documentacion_Pipeline.docx` para el detalle completo de la justificacion de paradigmas por etapa (por que BASIC-256 para limpieza imperativa, por que Java para el motor de reglas con herencia/polimorfismo, etc.).

## Notas conocidas / limitaciones

- **FORTRAN tiene las estaciones hardcodeadas.** `etapa2.f90` solo reconoce `COTO`, `GOLFITO` y `CORREDORES` (arreglo fijo de tamano 3). Un registro de una estacion distinta se descarta con un `AVISO` por consola, sin aparecer en `metricas.csv` ni generar error visible en el pipeline. Si se agregan estaciones nuevas al dataset de entrada, hay que actualizar `NUM_ESTACIONES` y el arreglo `nombres_estaciones` en el codigo fuente.
- **Cada JDK puede compilar bytecode incompatible con el `java` instalado localmente.** `run_pipeline.bat` compila con `javac --release 8` para maximizar compatibilidad; si se usa una API mas reciente que Java 8 en el codigo fuente de la etapa Java, la compilacion fallara con un mensaje claro senalando la linea.
