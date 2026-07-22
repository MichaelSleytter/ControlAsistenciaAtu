package com.atu.asistencias.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 60) String username,
        @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password,
        @NotBlank @Size(max = 150) String nombreCompleto,
        @Email @Size(max = 150) String email,
        @NotNull(message = "La zona es obligatoria para un supervisor") Long zonaId
) {
}
