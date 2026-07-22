package com.atu.asistencias.asistencia.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AsistenciaUpsertRequest(
        @NotNull Long orientadorId,
        @NotNull LocalDate fecha,
        Long estadoId,
        String observacion,
        Long version
) {
}
