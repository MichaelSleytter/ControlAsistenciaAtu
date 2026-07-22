package com.atu.asistencias.turno.dto;

import com.atu.asistencias.turno.Turno;

import java.time.LocalTime;

public record TurnoResponse(
        Long id,
        String nombre,
        LocalTime horaInicio,
        LocalTime horaFin
) {

    public static TurnoResponse from(Turno turno) {
        return new TurnoResponse(turno.getId(), turno.getNombre(), turno.getHoraInicio(), turno.getHoraFin());
    }
}
