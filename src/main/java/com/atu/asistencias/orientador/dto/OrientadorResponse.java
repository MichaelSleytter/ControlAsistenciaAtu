package com.atu.asistencias.orientador.dto;

import com.atu.asistencias.orientador.EstadoOrientador;
import com.atu.asistencias.orientador.Orientador;

import java.time.LocalDate;

public record OrientadorResponse(
        Long id,
        String nombres,
        String apellidos,
        String dni,
        String codigoInterno,
        Long zonaId,
        String zonaNombre,
        Long supervisorId,
        String supervisorNombre,
        Long turnoId,
        String turnoNombre,
        LocalDate fechaIngreso,
        LocalDate fechaCese,
        EstadoOrientador estado
) {

    public static OrientadorResponse from(Orientador o) {
        return new OrientadorResponse(
                o.getId(),
                o.getNombres(),
                o.getApellidos(),
                o.getDni(),
                o.getCodigoInterno(),
                o.getZona().getId(),
                o.getZona().getNombre(),
                o.getSupervisor() != null ? o.getSupervisor().getId() : null,
                o.getSupervisor() != null ? o.getSupervisor().getNombreCompleto() : null,
                o.getTurno() != null ? o.getTurno().getId() : null,
                o.getTurno() != null ? o.getTurno().getNombre() : null,
                o.getFechaIngreso(),
                o.getFechaCese(),
                o.getEstado()
        );
    }
}
