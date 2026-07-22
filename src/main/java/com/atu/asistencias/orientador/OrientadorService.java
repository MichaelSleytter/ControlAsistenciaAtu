package com.atu.asistencias.orientador;

import com.atu.asistencias.common.exception.BadRequestException;
import com.atu.asistencias.common.exception.NotFoundException;
import com.atu.asistencias.orientador.dto.OrientadorRequest;
import com.atu.asistencias.orientador.dto.OrientadorResponse;
import com.atu.asistencias.security.AuthenticatedUser;
import com.atu.asistencias.turno.Turno;
import com.atu.asistencias.turno.TurnoRepository;
import com.atu.asistencias.usuario.Usuario;
import com.atu.asistencias.usuario.UsuarioRepository;
import com.atu.asistencias.zona.Zona;
import com.atu.asistencias.zona.ZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Todos los metodos publicos devuelven DTOs, construidos dentro de la propia
 * transaccion: con open-in-view deshabilitado, mapear asociaciones lazy
 * (zona, supervisor, turno) despues de que el metodo retorna lanza
 * LazyInitializationException.
 */
@Service
@RequiredArgsConstructor
public class OrientadorService {

    private final OrientadorRepository orientadorRepository;
    private final ZonaService zonaService;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;

    @Transactional(readOnly = true)
    public List<OrientadorResponse> listar(AuthenticatedUser actor, Long zonaIdFiltro, EstadoOrientador estado, String texto) {
        Long zonaEfectiva = resolverZonaDeConsulta(actor, zonaIdFiltro);
        String textoNormalizado = (texto == null || texto.isBlank()) ? null : texto.trim().toLowerCase();
        return orientadorRepository.buscar(zonaEfectiva, estado, textoNormalizado).stream()
                .map(OrientadorResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrientadorResponse obtener(Long id, AuthenticatedUser actor) {
        return OrientadorResponse.from(obtenerEntidad(id, actor));
    }

    @Transactional
    public OrientadorResponse crear(OrientadorRequest request, AuthenticatedUser actor) {
        if (orientadorRepository.existsByDni(request.dni())) {
            throw new BadRequestException("Ya existe un orientador con ese DNI");
        }

        Long zonaId = actor.esAdmin() ? request.zonaId() : actor.zonaId();
        if (zonaId == null) {
            throw new BadRequestException("La zona es obligatoria");
        }
        Zona zona = zonaService.obtener(zonaId);

        Orientador orientador = new Orientador();
        orientador.setNombres(request.nombres());
        orientador.setApellidos(request.apellidos());
        orientador.setDni(request.dni());
        orientador.setCodigoInterno(request.codigoInterno());
        orientador.setZona(zona);
        orientador.setSupervisor(resolverSupervisor(request.supervisorId(), actor));
        orientador.setTurno(resolverTurno(request.turnoId()));
        orientador.setFechaIngreso(request.fechaIngreso());
        return OrientadorResponse.from(orientadorRepository.save(orientador));
    }

    @Transactional
    public OrientadorResponse actualizar(Long id, OrientadorRequest request, AuthenticatedUser actor) {
        Orientador orientador = obtenerEntidad(id, actor);

        if (!orientador.getDni().equals(request.dni()) && orientadorRepository.existsByDni(request.dni())) {
            throw new BadRequestException("Ya existe un orientador con ese DNI");
        }

        if (actor.esAdmin() && request.zonaId() != null) {
            orientador.setZona(zonaService.obtener(request.zonaId()));
        }

        orientador.setNombres(request.nombres());
        orientador.setApellidos(request.apellidos());
        orientador.setDni(request.dni());
        orientador.setCodigoInterno(request.codigoInterno());
        orientador.setSupervisor(resolverSupervisor(request.supervisorId(), actor));
        orientador.setTurno(resolverTurno(request.turnoId()));
        orientador.setFechaIngreso(request.fechaIngreso());
        return OrientadorResponse.from(orientadorRepository.save(orientador));
    }

    @Transactional
    public OrientadorResponse cambiarEstado(Long id, AuthenticatedUser actor, EstadoOrientador estado, LocalDate fechaCese) {
        Orientador orientador = obtenerEntidad(id, actor);
        orientador.setEstado(estado);
        orientador.setFechaCese(estado == EstadoOrientador.INACTIVO ? fechaCese : null);
        return OrientadorResponse.from(orientadorRepository.save(orientador));
    }

    private Orientador obtenerEntidad(Long id, AuthenticatedUser actor) {
        Orientador orientador = orientadorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Orientador no encontrado: " + id));
        validarAccesoZona(actor, orientador.getZona().getId());
        return orientador;
    }

    private Long resolverZonaDeConsulta(AuthenticatedUser actor, Long zonaIdFiltro) {
        if (actor.esAdmin()) {
            return zonaIdFiltro;
        }
        return actor.zonaId();
    }

    private void validarAccesoZona(AuthenticatedUser actor, Long zonaId) {
        if (!actor.esAdmin() && !actor.perteneceAZona(zonaId)) {
            throw new AccessDeniedException("El orientador no pertenece a tu zona");
        }
    }

    private Usuario resolverSupervisor(Long supervisorId, AuthenticatedUser actor) {
        if (!actor.esAdmin()) {
            return usuarioRepository.findById(actor.usuarioId()).orElse(null);
        }
        if (supervisorId == null) {
            return null;
        }
        return usuarioRepository.findById(supervisorId)
                .orElseThrow(() -> new BadRequestException("Supervisor no encontrado: " + supervisorId));
    }

    private Turno resolverTurno(Long turnoId) {
        if (turnoId == null) {
            return null;
        }
        return turnoRepository.findById(turnoId)
                .orElseThrow(() -> new BadRequestException("Turno no encontrado: " + turnoId));
    }
}
