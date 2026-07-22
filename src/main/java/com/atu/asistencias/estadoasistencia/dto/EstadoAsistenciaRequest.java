package com.atu.asistencias.estadoasistencia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EstadoAsistenciaRequest(
        @NotBlank @Size(max = 10) String codigo,
        @NotBlank @Size(max = 80) String nombre,
        @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe ser un hexadecimal tipo #RRGGBB")
        String colorHex,
        boolean requiereObservacion,
        int orden
) {
}
