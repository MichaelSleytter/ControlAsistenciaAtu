package com.atu.asistencias.reporte.dto;

public record ReporteSupervisorResponse(
        Long supervisorId,
        String supervisorNombre,
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
