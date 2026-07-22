package com.atu.asistencias.asistencia.dto;

import com.atu.asistencias.asistencia.AsistenciaHistorial;

import java.time.Instant;
import java.time.LocalDate;

public record AsistenciaHistorialResponse(
        Long id,
        Long orientadorId,
        LocalDate fecha,
        String usuarioNombre,
        String accion,
        String estadoAnteriorCodigo,
        String estadoNuevoCodigo,
        Instant fechaHora
) {

    public static AsistenciaHistorialResponse from(AsistenciaHistorial h) {
        return new AsistenciaHistorialResponse(
                h.getId(),
                h.getOrientador().getId(),
                h.getFecha(),
                h.getUsuario().getNombreCompleto(),
                h.getAccion().name(),
                h.getEstadoAnteriorCodigo(),
                h.getEstadoNuevoCodigo(),
                h.getFechaHora()
        );
    }
}
