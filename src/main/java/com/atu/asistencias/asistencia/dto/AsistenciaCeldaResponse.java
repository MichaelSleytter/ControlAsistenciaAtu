package com.atu.asistencias.asistencia.dto;

import com.atu.asistencias.asistencia.Asistencia;

import java.time.LocalDate;

public record AsistenciaCeldaResponse(
        Long orientadorId,
        LocalDate fecha,
        String estadoCodigo,
        String estadoColorHex,
        String observacion,
        Long version
) {

    public static AsistenciaCeldaResponse from(Asistencia a) {
        return new AsistenciaCeldaResponse(
                a.getOrientador().getId(),
                a.getFecha(),
                a.getEstado().getCodigo(),
                a.getEstado().getColorHex(),
                a.getObservacion(),
                a.getVersion()
        );
    }

    public static AsistenciaCeldaResponse celdaVacia(Long orientadorId, LocalDate fecha) {
        return new AsistenciaCeldaResponse(orientadorId, fecha, null, null, null, null);
    }
}
