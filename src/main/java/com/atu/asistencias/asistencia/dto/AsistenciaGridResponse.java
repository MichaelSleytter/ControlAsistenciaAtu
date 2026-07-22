package com.atu.asistencias.asistencia.dto;

import java.util.List;
import java.util.Map;

public record AsistenciaGridResponse(
        int anio,
        int mes,
        List<OrientadorFila> filas
) {

    public record OrientadorFila(
            Long orientadorId,
            String nombres,
            String apellidos,
            Map<String, String> celdas,
            Totales totales
    ) {
    }

    public record Totales(
            long tardanzas,
            long faltas,
            long faltasJustificadas,
            long descansosMedicos
    ) {
    }
}
