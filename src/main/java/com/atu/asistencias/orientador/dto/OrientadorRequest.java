package com.atu.asistencias.orientador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record OrientadorRequest(
        @NotBlank @Size(max = 120) String nombres,
        @NotBlank @Size(max = 120) String apellidos,
        @NotBlank @Size(max = 20) String dni,
        @Size(max = 40) String codigoInterno,
        Long zonaId,
        Long supervisorId,
        Long turnoId,
        @NotNull @PastOrPresent LocalDate fechaIngreso
) {
}
