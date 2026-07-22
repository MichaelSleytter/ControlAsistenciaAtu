package com.atu.asistencias.zona.dto;

import com.atu.asistencias.zona.Zona;

public record ZonaResponse(
        Long id,
        String nombre,
        String descripcion,
        boolean activo
) {

    public static ZonaResponse from(Zona zona) {
        return new ZonaResponse(zona.getId(), zona.getNombre(), zona.getDescripcion(), zona.isActivo());
    }
}
