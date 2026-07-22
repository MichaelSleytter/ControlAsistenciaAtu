package com.atu.asistencias.reporte.dto;

public record ReporteZonaResponse(
        Long zonaId,
        String zonaNombre,
        long totalOrientadores,
        long asistencias,
        long tardanzas,
        long faltas,
        long faltasJustificadas,
        long descansosMedicos,
        long totalRegistros,
        double porcentajeAsistencia
) {
}
