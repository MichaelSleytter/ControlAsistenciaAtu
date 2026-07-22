package com.atu.asistencias.security;

import com.atu.asistencias.usuario.RolUsuario;

public record AuthenticatedUser(Long usuarioId, String username, RolUsuario role, Long zonaId) {

    public boolean esAdmin() {
        return role == RolUsuario.ADMIN;
    }

    public boolean perteneceAZona(Long otraZonaId) {
        return zonaId != null && zonaId.equals(otraZonaId);
    }
}
