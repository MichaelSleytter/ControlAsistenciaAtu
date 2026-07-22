package com.atu.asistencias.reporte.dto;

public record RankingOrientadorResponse(
        Long orientadorId,
        String nombreCompleto,
        String zonaNombre,
        String estadoCodigo,
        long cantidad
) {
}
