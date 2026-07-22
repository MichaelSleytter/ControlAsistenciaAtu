package com.atu.asistencias.auth.dto;

import com.atu.asistencias.usuario.RolUsuario;
import com.atu.asistencias.usuario.Usuario;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UsuarioAuthInfo usuario
) {

    public static LoginResponse of(String accessToken, String refreshToken, Usuario usuario) {
        return new LoginResponse(accessToken, refreshToken, "Bearer", UsuarioAuthInfo.from(usuario));
    }

    public record UsuarioAuthInfo(
            Long id,
            String username,
            String nombreCompleto,
            RolUsuario role,
            Long zonaId,
            String zonaNombre,
            boolean debeCambiarPassword
    ) {

        public static UsuarioAuthInfo from(Usuario usuario) {
            Long zonaId = usuario.getZona() != null ? usuario.getZona().getId() : null;
            String zonaNombre = usuario.getZona() != null ? usuario.getZona().getNombre() : null;
            return new UsuarioAuthInfo(
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getNombreCompleto(),
                    usuario.getRole(),
                    zonaId,
                    zonaNombre,
                    usuario.isDebeCambiarPassword()
            );
        }
    }
}
