program etapa2
    implicit none

    integer, parameter :: NUM_ESTACIONES = 3
    character(len=20) :: nombres_estaciones(NUM_ESTACIONES)
    integer :: contador(NUM_ESTACIONES)

    ! --- Acumuladores por estacion ---
    real :: suma_temp(NUM_ESTACIONES), temp_max(NUM_ESTACIONES), temp_min(NUM_ESTACIONES)
    real :: suma_precip(NUM_ESTACIONES)
    real :: suma_viento(NUM_ESTACIONES), viento_max(NUM_ESTACIONES)
    real :: suma_bateria(NUM_ESTACIONES)

    ! --- Metricas finales (promedios) ---
    real :: temp_prom(NUM_ESTACIONES), viento_prom(NUM_ESTACIONES), bateria_prom(NUM_ESTACIONES)

    character(len=200) :: linea
    character(len=20) :: id_str, estacion
    real :: temp, precip, viento, bateria
    integer :: ios, i, idx, unidad

    ! --- Inicializacion ---
    nombres_estaciones(1) = 'COTO'
    nombres_estaciones(2) = 'GOLFITO'
    nombres_estaciones(3) = 'CORREDORES'
    contador = 0
    unidad = 10

    suma_temp = 0.0
    temp_max = -999.0     ! centinela: cualquier temp real (15-45) le gana
    temp_min = 999.0      ! centinela: cualquier temp real (15-45) le gana
    suma_precip = 0.0
    suma_viento = 0.0
    viento_max = -999.0   ! centinela: viento es >= 0, asi que le gana
    suma_bateria = 0.0

    ! --- Abrir archivo de entrada ---
    open(unit=unidad, file='datos_normalizados.csv', status='old', &
         action='read', iostat=ios)

    if (ios /= 0) then
        print *, 'ERROR: no se pudo abrir datos_normalizados.csv'
        print *, 'Verifica que el archivo este en la misma carpeta que el ejecutable.'
        stop
    end if

    ! --- Saltar encabezado ---
    read(unidad, '(A)', iostat=ios) linea

    ! --- Leer registro por registro ---
    do
        read(unidad, '(A)', iostat=ios) linea
        if (ios /= 0) exit          ! fin de archivo
        if (len_trim(linea) == 0) cycle   ! por si hay lineas en blanco

        call parsear_linea(linea, id_str, estacion, temp, precip, viento, bateria)

        idx = indice_estacion(estacion, nombres_estaciones, NUM_ESTACIONES)

        if (idx > 0) then
            contador(idx) = contador(idx) + 1

            suma_temp(idx) = suma_temp(idx) + temp
            if (temp > temp_max(idx)) temp_max(idx) = temp
            if (temp < temp_min(idx)) temp_min(idx) = temp

            suma_precip(idx) = suma_precip(idx) + precip

            suma_viento(idx) = suma_viento(idx) + viento
            if (viento > viento_max(idx)) viento_max(idx) = viento

            suma_bateria(idx) = suma_bateria(idx) + bateria
        else
            print *, 'AVISO: estacion no reconocida -> ', trim(estacion)
        end if
    end do

    close(unidad)

    ! --- Calcular promedios (solo si hubo registros, para evitar division por 0) ---
    do i = 1, NUM_ESTACIONES
        if (contador(i) > 0) then
            temp_prom(i) = suma_temp(i) / real(contador(i))
            viento_prom(i) = suma_viento(i) / real(contador(i))
            bateria_prom(i) = suma_bateria(i) / real(contador(i))
        else
            temp_prom(i) = 0.0
            viento_prom(i) = 0.0
            bateria_prom(i) = 0.0
        end if
    end do

    ! --- Tabla final de metricas por estacion ---
    print '(A)', '--- Metricas por estacion ---'
    print '(A10,A10,A10,A10,A12,A10,A10,A10)', &
        'ESTACION', 'TEMP_PROM', 'TEMP_MAX', 'TEMP_MIN', &
        'PRECIP_TOT', 'VIEN_PROM', 'VIEN_MAX', 'BAT_PROM'

    do i = 1, NUM_ESTACIONES
        print '(A10,F10.2,F10.2,F10.2,F12.2,F10.2,F10.2,F10.2)', &
            trim(nombres_estaciones(i)), temp_prom(i), temp_max(i), temp_min(i), &
            suma_precip(i), viento_prom(i), viento_max(i), bateria_prom(i)
    end do

    ! --- Escribir metricas.csv ---
    open(unit=20, file='metricas.csv', status='replace', action='write', iostat=ios)

    if (ios /= 0) then
        print *, 'ERROR: no se pudo crear metricas.csv'
        stop
    end if

    write(20, '(A)') 'ESTACION,TEMP_PROM,TEMP_MAX,TEMP_MIN,PRECIP_TOTAL,VIENTO_PROM,VIENTO_MAX,BATERIA_PROM'

    do i = 1, NUM_ESTACIONES
        write(20, '(A,7(",",F0.2))') &
            trim(nombres_estaciones(i)), temp_prom(i), temp_max(i), temp_min(i), &
            suma_precip(i), viento_prom(i), viento_max(i), bateria_prom(i)
    end do

    close(20)

    print *, ''
    print *, 'metricas.csv generado correctamente.'

contains

    ! Parte una linea "id,estacion,temp,precip,viento,bateria" por comas
    subroutine parsear_linea(linea, id_str, estacion, temp, precip, viento, bateria)
        character(len=*), intent(in)  :: linea
        character(len=20), intent(out) :: id_str, estacion
        real, intent(out) :: temp, precip, viento, bateria

        character(len=200) :: resto
        integer :: p

        resto = linea

        p = index(resto, ',')
        id_str = adjustl(resto(1:p-1))
        resto = resto(p+1:)

        p = index(resto, ',')
        estacion = adjustl(resto(1:p-1))
        resto = resto(p+1:)

        p = index(resto, ',')
        read(resto(1:p-1), *) temp
        resto = resto(p+1:)

        p = index(resto, ',')
        read(resto(1:p-1), *) precip
        resto = resto(p+1:)

        p = index(resto, ',')
        read(resto(1:p-1), *) viento
        resto = resto(p+1:)

        read(resto, *) bateria

    end subroutine parsear_linea

    ! Devuelve el indice de la estacion en el arreglo, o 0 si no la encuentra
    function indice_estacion(estacion, nombres, n) result(idx)
        character(len=*), intent(in) :: estacion
        character(len=*), intent(in) :: nombres(:)
        integer, intent(in) :: n
        integer :: idx, i

        idx = 0
        do i = 1, n
            if (trim(estacion) == trim(nombres(i))) then
                idx = i
                return
            end if
        end do
    end function indice_estacion

end program etapa2
