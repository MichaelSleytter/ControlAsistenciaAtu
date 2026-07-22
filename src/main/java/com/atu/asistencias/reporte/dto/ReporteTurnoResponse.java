package com.atu.asistencias.reporte.dto;

public record ReporteTurnoResponse(
        Long turnoId,
        String turnoNombre,
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
