@echo off
REM ============================================================
REM PolyFlow - Proyecto 1 (EIF400 - Paradigmas de Programacion)
REM Pipeline poliglota: BASIC-256 -> FORTRAN -> JAVA -> C
REM
REM Idea central: cada etapa NO conoce a las demas. Solo conoce
REM un "contrato" (nombre y formato de archivo CSV/TXT). Este
REM script es el unico que sabe el orden y se encarga de mover
REM cada salida hacia donde la siguiente etapa la espera.
REM
REM Todas las etapas leen y escriben en su DIRECTORIO ACTUAL
REM (no reciben rutas por parametro, excepto la etapa C). Por eso
REM este script usa "cd /d" hacia data\ antes de invocar cada
REM programa.
REM ============================================================

setlocal enabledelayedexpansion

REM %~dp0 = carpeta donde esta este .bat (con backslash final)
set "ROOT=%~dp0"
set "DATA=%ROOT%data"
set "B256=%ROOT%Basic-256"
set "FORTRAN=%ROOT%FORTRAN"
set "JAVA=%ROOT%JAVA"
set "CDIR=%ROOT%C"

echo ============================================
echo   PIPELINE POLIGLOTA - PolyFlow
echo ============================================
echo.

REM --- Preparar carpeta de trabajo compartida ---
if not exist "%DATA%" mkdir "%DATA%"
del /q "%DATA%\*.csv" >nul 2>&1
del /q "%DATA%\*.txt" >nul 2>&1

REM ------------------------------------------------------------
REM ETAPA 1 - BASIC-256: limpieza y validacion
REM Entrada : estaciones_raw.csv
REM Salida  : datos_normalizados.csv, errores.csv
REM ------------------------------------------------------------
echo [BASIC-256] Procesando datos...

copy /y "%B256%\estaciones_raw.csv" "%DATA%\estaciones_raw.csv" >nul
if errorlevel 1 (
    echo   ERROR: no se encontro estaciones_raw.csv en Basic-256\
    goto :error
)

REM -s / --silent: corre sin ninguna ventana grafica, imprime los
REM "print" del .kbs directo en esta consola, y el codigo de salida
REM SI refleja si el script tuvo error (a diferencia de -r, que abre
REM el IDE y se queda esperando que lo cierres a mano).
set "BASIC256_EXE=C:\Program Files\BASIC256\basic256.exe"

if not exist "%BASIC256_EXE%" (
    echo   ERROR: no se encontro basic256.exe en "%BASIC256_EXE%"
    echo   Ajusta la variable BASIC256_EXE al inicio de esta etapa si tu instalacion quedo en otra ruta.
    goto :error
)

REM NOTA: -s con un .kbs de ruta absoluta hace que BASIC-256 trabaje
REM en la carpeta del .kbs (Basic-256\), NO en el cwd del proceso que
REM lo lanza. Por eso aqui NO usamos pushd hacia data\: dejamos que
REM escriba donde quiere y copiamos su salida a data\ despues, igual
REM que hacemos con las entradas de las demas etapas.
"%BASIC256_EXE%" -s "%B256%\Etapa1_Basic256_MorenoL.kbs"
set "RC=%errorlevel%"

if not "%RC%"=="0" (
    echo   ERROR: BASIC-256 termino con codigo %RC%
    goto :error
)
if not exist "%B256%\datos_normalizados.csv" (
    echo   ERROR: no se genero datos_normalizados.csv en Basic-256\
    goto :error
)

copy /y "%B256%\datos_normalizados.csv" "%DATA%\datos_normalizados.csv" >nul
copy /y "%B256%\errores.csv" "%DATA%\errores.csv" >nul 2>&1
echo [BASIC-256] Procesando datos... OK
echo.

REM ------------------------------------------------------------
REM ETAPA 2 - FORTRAN: procesamiento numerico
REM Entrada : datos_normalizados.csv
REM Salida  : metricas.csv
REM ------------------------------------------------------------
echo [FORTRAN] Calculando metricas...

pushd "%DATA%"
"%FORTRAN%\etapa2.exe"
set "RC=%errorlevel%"
popd

if not "%RC%"=="0" (
    echo   ERROR: etapa2.exe termino con codigo %RC%
    goto :error
)
if not exist "%DATA%\metricas.csv" (
    echo   ERROR: no se genero metricas.csv
    goto :error
)
echo [FORTRAN] Calculando metricas... OK
echo.

REM ------------------------------------------------------------
REM ETAPA 3 - JAVA: motor de reglas (POO + polimorfismo)
REM Entrada : metricas.csv, reglas.txt
REM Salida  : alertas.csv
REM ------------------------------------------------------------
echo [JAVA] Evaluando reglas...

REM Compilamos los .java con el JDK de esta maquina en vez de usar los
REM .class ya compilados: asi evitamos UnsupportedClassVersionError si
REM quien escribio el codigo usa una version de Java distinta a la tuya.
REM (Los .class del repo quedan igual, javac los sobreescribe con una
REM version compatible con tu JRE local.)
REM No usamos "%JAVA%\*.java" directo: cmd.exe no expande el
REM wildcard, y con rutas que tienen espacios (como esta, por
REM "2026 II CICLO") javac lo recibe como un unico nombre de
REM archivo literal e invalido. Con el for armamos la lista de
REM archivos ya expandida y la pasamos TODA junta a un solo
REM javac (no uno por uno: Main.java depende de las demas clases
REM del paquete, y necesitan compilarse en la misma invocacion
REM para que javac resuelva esas referencias cruzadas).
set "JAVA_SOURCES="
for %%F in ("%JAVA%\*.java") do set "JAVA_SOURCES=!JAVA_SOURCES! "%%F""

REM Tu maquina tiene mas de un JDK instalado: "javac" resuelve a la
REM version 21, pero "java" resuelve a la version 8 (confirmado con
REM java -version / javac -version). --release 8 le pide al javac 21
REM que genere bytecode compatible con Java 8 (class file version 52),
REM en vez de su version 65 por defecto, para que el "java" viejo si
REM lo pueda ejecutar.
javac --release 8 -d "%JAVA%" !JAVA_SOURCES!
if not "%errorlevel%"=="0" (
    echo   ERROR: fallo la compilacion de los .java ^(javac^)
    goto :error
)

copy /y "%JAVA%\reglas.txt" "%DATA%\reglas.txt" >nul

pushd "%DATA%"
java -cp "%JAVA%" Main
set "RC=%errorlevel%"
popd

if not "%RC%"=="0" (
    echo   ERROR: Main.java termino con codigo %RC%
    goto :error
)
if not exist "%DATA%\alertas.csv" (
    echo   ERROR: no se genero alertas.csv
    goto :error
)
echo [JAVA] Evaluando reglas... OK
echo.

REM ------------------------------------------------------------
REM ETAPA 4 - C: verificacion de integridad (checksum)
REM Entrada : alertas.csv
REM Salida  : resultado_final.txt
REM ------------------------------------------------------------
echo [C] Calculando firma...

"%CDIR%\etapa4.exe" "%DATA%\alertas.csv" "%DATA%\resultado_final.txt"
set "RC=%errorlevel%"

if not "%RC%"=="0" (
    echo   ERROR: etapa4.exe termino con codigo %RC%
    goto :error
)
echo [C] Calculando firma... OK
echo.

echo ============================================
echo   PIPELINE COMPLETADO
echo ============================================
echo Resultado final:
echo   %DATA%\resultado_final.txt
echo.
type "%DATA%\resultado_final.txt"
goto :fin

:error
echo.
echo ============================================
echo   PIPELINE DETENIDO POR ERROR
echo ============================================
exit /b 1

:fin
endlocal
