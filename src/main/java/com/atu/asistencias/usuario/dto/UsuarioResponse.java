package com.atu.asistencias.usuario.dto;

import com.atu.asistencias.usuario.RolUsuario;
import com.atu.asistencias.usuario.Usuario;

import java.time.Instant;

public record UsuarioResponse(
        Long id,
        String username,
        String nombreCompleto,
        String email,
        RolUsuario role,
        Long zonaId,
        String zonaNombre,
        boolean activo,
        Instant ultimoLoginAt
) {

    public static UsuarioResponse from(Usuario usuario) {
        Long zonaId = usuario.getZona() != null ? usuario.getZona().getId() : null;
        String zonaNombre = usuario.getZona() != null ? usuario.getZona().getNombre() : null;
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRole(),
                zonaId,
                zonaNombre,
                usuario.isActivo(),
                usuario.getUltimoLoginAt()
        );
    }
}
