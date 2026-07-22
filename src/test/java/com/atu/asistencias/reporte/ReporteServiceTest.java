package com.atu.asistencias.reporte;

import com.atu.asistencias.asistencia.Asistencia;
import com.atu.asistencias.asistencia.AsistenciaRepository;
import com.atu.asistencias.estadoasistencia.EstadoAsistencia;
import com.atu.asistencias.orientador.EstadoOrientador;
import com.atu.asistencias.orientador.Orientador;
import com.atu.asistencias.orientador.OrientadorRepository;
import com.atu.asistencias.reporte.dto.RankingOrientadorResponse;
import com.atu.asistencias.reporte.dto.ReporteZonaResponse;
import com.atu.asistencias.security.AuthenticatedUser;
import com.atu.asistencias.usuario.RolUsuario;
import com.atu.asistencias.zona.Zona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private OrientadorRepository orientadorRepository;
    @Mock
    private AsistenciaRepository asistenciaRepository;

    @InjectMocks
    private ReporteService reporteService;

    private Zona zonaCentro;
    private Zona zonaSur;
    private Orientador orientador1;
    private Orientador orientador2;
    private Orientador orientador3;

    @BeforeEach
    void setUp() {
        zonaCentro = new Zona();
        zonaCentro.setId(1L);
        zonaCentro.setNombre("Zona Centro");

        zonaSur = new Zona();
        zonaSur.setId(2L);
        zonaSur.setNombre("Zona Sur");

        orientador1 = new Orientador();
        orientador1.setId(10L);
        orientador1.setNombres("Ana");
        orientador1.setApellidos("Torres");
        orientador1.setZona(zonaCentro);

        orientador2 = new Orientador();
        orientador2.setId(20L);
        orientador2.setNombres("Luis");
        orientador2.setApellidos("Perez");
        orientador2.setZona(zonaCentro);

        orientador3 = new Orientador();
        orientador3.setId(30L);
        orientador3.setNombres("Maria");
        orientador3.setApellidos("Lopez");
        orientador3.setZona(zonaSur);
    }

    private Asistencia asistencia(Orientador o, LocalDate fecha, String codigo) {
        EstadoAsistencia estado = new EstadoAsistencia();
        estado.setCodigo(codigo);
        Asistencia a = new Asistencia();
        a.setOrientador(o);
        a.setFecha(fecha);
        a.setEstado(estado);
        return a;
    }

    @Test
    void reportePorZona_agrupaYSumaCorrectamentePorZona_paraElAdmin() {
        AuthenticatedUser admin = new AuthenticatedUser(1L, "admin", RolUsuario.ADMIN, null);

        when(orientadorRepository.buscar(isNull(), eq(EstadoOrientador.ACTIVO), isNull()))
                .thenReturn(List.of(orientador1, orientador2, orientador3));

        when(asistenciaRepository.findByOrientadorIdInAndFechaBetween(any(), any(), any())).thenReturn(List.of(
                asistencia(orientador1, LocalDate.of(2026, 7, 1), "F"),
                asistencia(orientador1, LocalDate.of(2026, 7, 2), "A"),
                asistencia(orientador2, LocalDate.of(2026, 7, 1), "T"),
                asistencia(orientador3, LocalDate.of(2026, 7, 1), "A")
        ));

        List<ReporteZonaResponse> reporte = reporteService.reportePorZona(admin, null, 2026, 7);

        assertThat(reporte).hasSize(2);
        ReporteZonaResponse centro = reporte.stream().filter(r -> r.zonaNombre().equals("Zona Centro")).findFirst().orElseThrow();
        assertThat(centro.totalOrientadores()).isEqualTo(2);
        assertThat(centro.faltas()).isEqualTo(1);
        assertThat(centro.tardanzas()).isEqualTo(1);
        assertThat(centro.asistencias()).isEqualTo(1);

        ReporteZonaResponse sur = reporte.stream().filter(r -> r.zonaNombre().equals("Zona Sur")).findFirst().orElseThrow();
        assertThat(sur.totalOrientadores()).isEqualTo(1);
        assertThat(sur.asistencias()).isEqualTo(1);
    }

    @Test
    void reportePorZona_ignoraElFiltroDeZona_ySoloUsaLaPropiaDelSupervisor() {
        AuthenticatedUser supervisorCentro = new AuthenticatedUser(100L, "braian", RolUsuario.SUPERVISOR, 1L);

        when(orientadorRepository.buscar(eq(1L), eq(EstadoOrientador.ACTIVO), isNull()))
                .thenReturn(List.of(orientador1, orientador2));
        when(asistenciaRepository.findByOrientadorIdInAndFechaBetween(any(), any(), any())).thenReturn(List.of());

        // El supervisor intenta pedir zonaId=2 (Zona Sur); el servicio debe ignorarlo.
        List<ReporteZonaResponse> reporte = reporteService.reportePorZona(supervisorCentro, 2L, 2026, 7);

        assertThat(reporte).hasSize(1);
        assertThat(reporte.get(0).zonaNombre()).isEqualTo("Zona Centro");
    }

    @Test
    void ranking_ordenaDescendenteYRespetaElLimite() {
        AuthenticatedUser admin = new AuthenticatedUser(1L, "admin", RolUsuario.ADMIN, null);

        when(orientadorRepository.buscar(isNull(), eq(EstadoOrientador.ACTIVO), isNull()))
                .thenReturn(List.of(orientador1, orientador2, orientador3));

        when(asistenciaRepository.findByOrientadorIdInAndFechaBetween(any(), any(), any())).thenReturn(List.of(
                asistencia(orientador1, LocalDate.of(2026, 7, 1), "F"),
                asistencia(orientador1, LocalDate.of(2026, 7, 2), "F"),
                asistencia(orientador2, LocalDate.of(2026, 7, 1), "F"),
                asistencia(orientador3, LocalDate.of(2026, 7, 1), "A")
        ));

        List<RankingOrientadorResponse> ranking = reporteService.ranking(admin, null, 2026, 7, "F", 1);

        assertThat(ranking).hasSize(1);
        assertThat(ranking.get(0).orientadorId()).isEqualTo(10L);
        assertThat(ranking.get(0).cantidad()).isEqualTo(2);
    }
}
