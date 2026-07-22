package com.atu.asistencias.reporte;

import com.atu.asistencias.asistencia.Asistencia;
import com.atu.asistencias.asistencia.AsistenciaRepository;
import com.atu.asistencias.asistencia.ContadorEstados;
import com.atu.asistencias.orientador.EstadoOrientador;
import com.atu.asistencias.orientador.Orientador;
import com.atu.asistencias.orientador.OrientadorRepository;
import com.atu.asistencias.reporte.dto.RankingOrientadorResponse;
import com.atu.asistencias.reporte.dto.ReporteSupervisorResponse;
import com.atu.asistencias.reporte.dto.ReporteTurnoResponse;
import com.atu.asistencias.reporte.dto.ReporteZonaResponse;
import com.atu.asistencias.security.AuthenticatedUser;
import com.atu.asistencias.turno.Turno;
import com.atu.asistencias.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agrega asistencias del mes por distintas dimensiones (zona, supervisor,
 * turno) o arma un ranking de orientadores por un estado especifico.
 * La agregacion se hace en memoria: a esta escala (cientos de orientadores
 * por mes) es mas simple y mantenible que varias consultas GROUP BY, y evita
 * duplicar el conteo por codigo que ya usa la grilla.
 */
@Service
@RequiredArgsConstructor
public class ReporteService {

    private final OrientadorRepository orientadorRepository;
    private final AsistenciaRepository asistenciaRepository;

    @Transactional(readOnly = true)
    public List<ReporteZonaResponse> reportePorZona(AuthenticatedUser actor, Long zonaIdFiltro, int anio, int mes) {
        DatosDelMes datos = cargarDatosDelMes(actor, zonaIdFiltro, anio, mes);
        Map<Long, List<Orientador>> porZona = datos.orientadores().stream()
                .collect(Collectors.groupingBy(o -> o.getZona().getId()));

        List<ReporteZonaResponse> resultado = new ArrayList<>();
        for (List<Orientador> deLaZona : porZona.values()) {
            ContadorEstados contador = acumular(deLaZona, datos.asistenciasPorOrientador());
            resultado.add(new ReporteZonaResponse(
                    deLaZona.get(0).getZona().getId(),
                    deLaZona.get(0).getZona().getNombre(),
                    deLaZona.size(),
                    contador.getAsistio(),
                    contador.getTardanzas(),
                    contador.getFaltas(),
                    contador.getFaltasJustificadas(),
                    contador.getDescansosMedicos(),
                    contador.getTotal(),
                    contador.getPorcentajeAsistencia()));
        }
        resultado.sort(Comparator.comparing(ReporteZonaResponse::zonaNombre));
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<ReporteSupervisorResponse> reportePorSupervisor(AuthenticatedUser actor, Long zonaIdFiltro, int anio, int mes) {
        DatosDelMes datos = cargarDatosDelMes(actor, zonaIdFiltro, anio, mes);
        Map<String, List<Orientador>> porSupervisor = datos.orientadores().stream()
                .collect(Collectors.groupingBy(o -> o.getSupervisor() != null ? "S" + o.getSupervisor().getId() : "SIN_ASIGNAR"));

        List<ReporteSupervisorResponse> resultado = new ArrayList<>();
        for (List<Orientador> grupo : porSupervisor.values()) {
            ContadorEstados contador = acumular(grupo, datos.asistenciasPorOrientador());
            Usuario supervisor = grupo.get(0).getSupervisor();
            resultado.add(new ReporteSupervisorResponse(
                    supervisor != null ? supervisor.getId() : null,
                    supervisor != null ? supervisor.getNombreCompleto() : "Sin asignar",
                    grupo.size(),
                    contador.getAsistio(),
                    contador.getTardanzas(),
                    contador.getFaltas(),
                    contador.getFaltasJustificadas(),
                    contador.getDescansosMedicos(),
                    contador.getTotal(),
                    contador.getPorcentajeAsistencia()));
        }
        resultado.sort(Comparator.comparing(ReporteSupervisorResponse::supervisorNombre));
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<ReporteTurnoResponse> reportePorTurno(AuthenticatedUser actor, Long zonaIdFiltro, int anio, int mes) {
        DatosDelMes datos = cargarDatosDelMes(actor, zonaIdFiltro, anio, mes);
        Map<String, List<Orientador>> porTurno = datos.orientadores().stream()
                .collect(Collectors.groupingBy(o -> o.getTurno() != null ? "T" + o.getTurno().getId() : "SIN_TURNO"));

        List<ReporteTurnoResponse> resultado = new ArrayList<>();
        for (List<Orientador> grupo : porTurno.values()) {
            ContadorEstados contador = acumular(grupo, datos.asistenciasPorOrientador());
            Turno turno = grupo.get(0).getTurno();
            resultado.add(new ReporteTurnoResponse(
                    turno != null ? turno.getId() : null,
                    turno != null ? turno.getNombre() : "Sin turno",
                    grupo.size(),
                    contador.getAsistio(),
                    contador.getTardanzas(),
                    contador.getFaltas(),
                    contador.getFaltasJustificadas(),
                    contador.getDescansosMedicos(),
                    contador.getTotal(),
                    contador.getPorcentajeAsistencia()));
        }
        resultado.sort(Comparator.comparing(ReporteTurnoResponse::turnoNombre));
        return resultado;
    }

    @Transactional(readOnly = true)
    public List<RankingOrientadorResponse> ranking(AuthenticatedUser actor, Long zonaIdFiltro, int anio, int mes,
                                                     String criterio, int limite) {
        DatosDelMes datos = cargarDatosDelMes(actor, zonaIdFiltro, anio, mes);
        String criterioNormalizado = criterio.toUpperCase();

        List<RankingOrientadorResponse> resultado = new ArrayList<>();
        for (Orientador o : datos.orientadores()) {
            long cantidad = datos.asistenciasPorOrientador().getOrDefault(o.getId(), List.of()).stream()
                    .filter(a -> a.getEstado().getCodigo().equalsIgnoreCase(criterioNormalizado))
                    .count();
            if (cantidad > 0) {
                resultado.add(new RankingOrientadorResponse(
                        o.getId(),
                        o.getApellidos() + " " + o.getNombres(),
                        o.getZona().getNombre(),
                        criterioNormalizado,
                        cantidad));
            }
        }
        resultado.sort(Comparator.comparingLong(RankingOrientadorResponse::cantidad).reversed());
        return resultado.stream().limit(Math.max(1, limite)).toList();
    }

    private ContadorEstados acumular(List<Orientador> orientadores, Map<Long, List<Asistencia>> asistenciasPorOrientador) {
        ContadorEstados contador = new ContadorEstados();
        for (Orientador o : orientadores) {
            for (Asistencia a : asistenciasPorOrientador.getOrDefault(o.getId(), List.of())) {
                contador.contar(a.getEstado().getCodigo());
            }
        }
        return contador;
    }

    private DatosDelMes cargarDatosDelMes(AuthenticatedUser actor, Long zonaIdFiltro, int anio, int mes) {
        Long zonaEfectiva = actor.esAdmin() ? zonaIdFiltro : actor.zonaId();
        List<Orientador> orientadores = orientadorRepository.buscar(zonaEfectiva, EstadoOrientador.ACTIVO, null);
        if (orientadores.isEmpty()) {
            return new DatosDelMes(List.of(), Map.of());
        }

        LocalDate desde = LocalDate.of(anio, mes, 1);
        LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());
        List<Long> ids = orientadores.stream().map(Orientador::getId).toList();
        Map<Long, List<Asistencia>> porOrientador = asistenciaRepository.findByOrientadorIdInAndFechaBetween(ids, desde, hasta)
                .stream()
                .collect(Collectors.groupingBy(a -> a.getOrientador().getId()));

        return new DatosDelMes(orientadores, porOrientador);
    }

    private record DatosDelMes(List<Orientador> orientadores, Map<Long, List<Asistencia>> asistenciasPorOrientador) {
    }
}
