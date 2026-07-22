package com.atu.asistencias.estadoasistencia.dto;

import com.atu.asistencias.estadoasistencia.EstadoAsistencia;

public record EstadoAsistenciaResponse(
        Long id,
        String codigo,
        String nombre,
        String colorHex,
        boolean requiereObservacion,
        int orden,
        boolean activo
) {

    public static EstadoAsistenciaResponse from(EstadoAsistencia e) {
        return new EstadoAsistenciaResponse(
                e.getId(), e.getCodigo(), e.getNombre(), e.getColorHex(),
                e.isRequiereObservacion(), e.getOrden(), e.isActivo());
    }
}
