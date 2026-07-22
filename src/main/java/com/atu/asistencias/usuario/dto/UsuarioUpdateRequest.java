package com.atu.asistencias.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(
        @NotBlank @Size(max = 150) String nombreCompleto,
        @Email @Size(max = 150) String email,
        @NotNull Long zonaId
) {
}
