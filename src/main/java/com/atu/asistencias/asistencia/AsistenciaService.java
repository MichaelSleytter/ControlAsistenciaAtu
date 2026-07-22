package com.atu.asistencias.asistencia;

import com.atu.asistencias.asistencia.dto.AsistenciaCeldaResponse;
import com.atu.asistencias.asistencia.dto.AsistenciaGridResponse;
import com.atu.asistencias.asistencia.dto.AsistenciaHistorialResponse;
import com.atu.asistencias.asistencia.dto.AsistenciaUpsertRequest;
import com.atu.asistencias.common.exception.BadRequestException;
import com.atu.asistencias.common.exception.ConflictException;
import com.atu.asistencias.common.exception.NotFoundException;
import com.atu.asistencias.estadoasistencia.EstadoAsistencia;
import com.atu.asistencias.estadoasistencia.EstadoAsistenciaRepository;
import com.atu.asistencias.orientador.EstadoOrientador;
import com.atu.asistencias.orientador.Orientador;
import com.atu.asistencias.orientador.OrientadorRepository;
import com.atu.asistencias.security.AuthenticatedUser;
import com.atu.asistencias.usuario.Usuario;
import com.atu.asistencias.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final AsistenciaHistorialRepository asistenciaHistorialRepository;
    private final OrientadorRepository orientadorRepository;
    private final EstadoAsistenciaRepository estadoAsistenciaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public AsistenciaGridResponse obtenerGrilla(AuthenticatedUser actor, Long zonaId, int anio, int mes, boolean incluirInactivos) {
        Long zonaEfectiva = actor.esAdmin() ? zonaId : actor.zonaId();
        if (zonaEfectiva == null) {
            throw new BadRequestException("La zona es obligatoria para consultar la grilla");
        }

        EstadoOrientador estadoFiltro = incluirInactivos ? null : EstadoOrientador.ACTIVO;
        List<Orientador> orientadores = orientadorRepository.buscar(zonaEfectiva, estadoFiltro, null);
        if (orientadores.isEmpty()) {
            return new AsistenciaGridResponse(anio, mes, List.of());
        }

        LocalDate desde = LocalDate.of(anio, mes, 1);
        LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());
        List<Long> ids = orientadores.stream().map(Orientador::getId).toList();

        Map<Long, Map<LocalDate, Asistencia>> porOrientador = new HashMap<>();
        for (Asistencia a : asistenciaRepository.findByOrientadorIdInAndFechaBetween(ids, desde, hasta)) {
            porOrientador.computeIfAbsent(a.getOrientador().getId(), k -> new HashMap<>()).put(a.getFecha(), a);
        }

        List<AsistenciaGridResponse.OrientadorFila> filas = new ArrayList<>();
        for (Orientador o : orientadores) {
            Map<LocalDate, Asistencia> asistenciasOrientador = porOrientador.getOrDefault(o.getId(), Map.of());
            Map<String, String> celdas = new LinkedHashMap<>();
            long tardanzas = 0, faltas = 0, faltasJustificadas = 0, descansosMedicos = 0;

            for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
                Asistencia a = asistenciasOrientador.get(dia);
                if (a == null) {
                    continue;
                }
                String codigo = a.getEstado().getCodigo();
                celdas.put(dia.toString(), codigo);
                switch (codigo) {
                    case "T" -> tardanzas++;
                    case "F" -> faltas++;
                    case "FJ" -> faltasJustificadas++;
                    case "DM" -> descansosMedicos++;
                    default -> { }
                }
            }

            filas.add(new AsistenciaGridResponse.OrientadorFila(
                    o.getId(), o.getNombres(), o.getApellidos(), celdas,
                    new AsistenciaGridResponse.Totales(tardanzas, faltas, faltasJustificadas, descansosMedicos)));
        }

        return new AsistenciaGridResponse(anio, mes, filas);
    }

    @Transactional
    public AsistenciaCeldaResponse registrar(AsistenciaUpsertRequest request, AuthenticatedUser actor) {
        Orientador orientador = orientadorRepository.findById(request.orientadorId())
                .orElseThrow(() -> new NotFoundException("Orientador no encontrado: " + request.orientadorId()));
        validarAccesoZona(actor, orientador.getZona().getId());

        Optional<Asistencia> existente = asistenciaRepository.findByOrientadorIdAndFecha(request.orientadorId(), request.fecha());

        if (request.estadoId() == null) {
            return eliminarCelda(request, orientador, existente, actor);
        }

        EstadoAsistencia estado = estadoAsistenciaRepository.findById(request.estadoId())
                .orElseThrow(() -> new BadRequestException("Estado de asistencia no válido"));
        if (estado.isRequiereObservacion() && (request.observacion() == null || request.observacion().isBlank())) {
            throw new BadRequestException("El estado '" + estado.getNombre() + "' requiere una observación");
        }

        Usuario actorUsuario = usuarioRepository.getReferenceById(actor.usuarioId());

        if (existente.isPresent()) {
            Asistencia a = existente.get();
            verificarVersion(request.version(), a.getVersion());
            String estadoAnterior = a.getEstado().getCodigo();
            a.setEstado(estado);
            a.setObservacion(request.observacion());
            a.setRegistradoPor(actorUsuario);
            Asistencia guardada = asistenciaRepository.save(a);
            registrarHistorial(orientador, request.fecha(), actor, AccionHistorial.EDITAR, estadoAnterior, estado.getCodigo());
            return AsistenciaCeldaResponse.from(guardada);
        }

        Asistencia nueva = new Asistencia();
        nueva.setOrientador(orientador);
        nueva.setFecha(request.fecha());
        nueva.setEstado(estado);
        nueva.setObservacion(request.observacion());
        nueva.setRegistradoPor(actorUsuario);
        Asistencia guardada = asistenciaRepository.save(nueva);
        registrarHistorial(orientador, request.fecha(), actor, AccionHistorial.CREAR, null, estado.getCodigo());
        return AsistenciaCeldaResponse.from(guardada);
    }

    @Transactional(readOnly = true)
    public List<AsistenciaHistorialResponse> historialDeCelda(Long orientadorId, LocalDate fecha, AuthenticatedUser actor) {
        Orientador orientador = orientadorRepository.findById(orientadorId)
                .orElseThrow(() -> new NotFoundException("Orientador no encontrado: " + orientadorId));
        validarAccesoZona(actor, orientador.getZona().getId());
        return asistenciaHistorialRepository.findByOrientadorIdAndFechaOrderByFechaHoraDesc(orientadorId, fecha).stream()
                .map(AsistenciaHistorialResponse::from)
                .toList();
    }

    private AsistenciaCeldaResponse eliminarCelda(AsistenciaUpsertRequest request, Orientador orientador,
                                                   Optional<Asistencia> existente, AuthenticatedUser actor) {
        if (existente.isPresent()) {
            Asistencia a = existente.get();
            verificarVersion(request.version(), a.getVersion());
            registrarHistorial(orientador, request.fecha(), actor, AccionHistorial.ELIMINAR, a.getEstado().getCodigo(), null);
            asistenciaRepository.delete(a);
        }
        return AsistenciaCeldaResponse.celdaVacia(request.orientadorId(), request.fecha());
    }

    private void verificarVersion(Long versionSolicitada, Long versionActual) {
        if (versionSolicitada != null && !versionSolicitada.equals(versionActual)) {
            throw new ConflictException("Este dato fue modificado por otro usuario, recarga la celda antes de guardar.");
        }
    }

    private void registrarHistorial(Orientador orientador, LocalDate fecha, AuthenticatedUser actor,
                                     AccionHistorial accion, String anterior, String nuevo) {
        AsistenciaHistorial historial = new AsistenciaHistorial();
        historial.setOrientador(orientador);
        historial.setFecha(fecha);
        historial.setUsuario(usuarioRepository.getReferenceById(actor.usuarioId()));
        historial.setAccion(accion);
        historial.setEstadoAnteriorCodigo(anterior);
        historial.setEstadoNuevoCodigo(nuevo);
        asistenciaHistorialRepository.save(historial);
    }

    private void validarAccesoZona(AuthenticatedUser actor, Long zonaId) {
        if (!actor.esAdmin() && !actor.perteneceAZona(zonaId)) {
            throw new AccessDeniedException("El orientador no pertenece a tu zona");
        }
    }
}
