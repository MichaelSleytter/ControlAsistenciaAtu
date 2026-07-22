package com.atu.asistencias.asistencia;

import com.atu.asistencias.asistencia.dto.AsistenciaCeldaResponse;
import com.atu.asistencias.asistencia.dto.AsistenciaGridResponse;
import com.atu.asistencias.asistencia.dto.AsistenciaUpsertRequest;
import com.atu.asistencias.common.exception.BadRequestException;
import com.atu.asistencias.common.exception.ConflictException;
import com.atu.asistencias.estadoasistencia.EstadoAsistencia;
import com.atu.asistencias.estadoasistencia.EstadoAsistenciaRepository;
import com.atu.asistencias.orientador.Orientador;
import com.atu.asistencias.orientador.OrientadorRepository;
import com.atu.asistencias.security.AuthenticatedUser;
import com.atu.asistencias.usuario.RolUsuario;
import com.atu.asistencias.usuario.Usuario;
import com.atu.asistencias.usuario.UsuarioRepository;
import com.atu.asistencias.zona.Zona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsistenciaServiceTest {

    @Mock
    private AsistenciaRepository asistenciaRepository;
    @Mock
    private AsistenciaHistorialRepository asistenciaHistorialRepository;
    @Mock
    private OrientadorRepository orientadorRepository;
    @Mock
    private EstadoAsistenciaRepository estadoAsistenciaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AsistenciaService asistenciaService;

    private Zona zonaCentro;
    private Zona zonaSur;
    private Orientador orientadorDeCentro;
    private AuthenticatedUser supervisorCentro;
    private AuthenticatedUser admin;

    @BeforeEach
    void setUp() {
        zonaCentro = new Zona();
        zonaCentro.setId(1L);
        zonaCentro.setNombre("Zona Centro");

        zonaSur = new Zona();
        zonaSur.setId(2L);
        zonaSur.setNombre("Zona Sur");

        orientadorDeCentro = new Orientador();
        orientadorDeCentro.setId(10L);
        orientadorDeCentro.setZona(zonaCentro);
        orientadorDeCentro.setNombres("Ana");
        orientadorDeCentro.setApellidos("Torres");

        supervisorCentro = new AuthenticatedUser(100L, "braian", RolUsuario.SUPERVISOR, zonaCentro.getId());
        admin = new AuthenticatedUser(1L, "admin", RolUsuario.ADMIN, null);
    }

    @Test
    void registrar_lanzaAccessDenied_cuandoOrientadorNoPerteneceALaZonaDelSupervisor() {
        orientadorDeCentro.setZona(zonaSur);
        when(orientadorRepository.findById(10L)).thenReturn(Optional.of(orientadorDeCentro));

        AsistenciaUpsertRequest request = new AsistenciaUpsertRequest(10L, LocalDate.of(2026, 7, 1), 5L, null, null);

        assertThatThrownBy(() -> asistenciaService.registrar(request, supervisorCentro))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(estadoAsistenciaRepository, asistenciaHistorialRepository);
    }

    @Test
    void registrar_creaAsistenciaNueva_cuandoElSupervisorPerteneceALaZonaDelOrientador() {
        when(orientadorRepository.findById(10L)).thenReturn(Optional.of(orientadorDeCentro));
        when(asistenciaRepository.findByOrientadorIdAndFecha(10L, LocalDate.of(2026, 7, 1))).thenReturn(Optional.empty());

        EstadoAsistencia estadoAsistio = new EstadoAsistencia();
        estadoAsistio.setId(1L);
        estadoAsistio.setCodigo("A");
        estadoAsistio.setColorHex("#FFFFFF");
        when(estadoAsistenciaRepository.findById(1L)).thenReturn(Optional.of(estadoAsistio));

        Usuario supervisorUsuario = new Usuario();
        supervisorUsuario.setId(100L);
        when(usuarioRepository.getReferenceById(100L)).thenReturn(supervisorUsuario);

        when(asistenciaRepository.save(any(Asistencia.class))).thenAnswer(invocation -> {
            Asistencia a = invocation.getArgument(0);
            a.setVersion(0L);
            return a;
        });

        AsistenciaUpsertRequest request = new AsistenciaUpsertRequest(10L, LocalDate.of(2026, 7, 1), 1L, null, null);
        AsistenciaCeldaResponse response = asistenciaService.registrar(request, supervisorCentro);

        assertThat(response.estadoCodigo()).isEqualTo("A");
        verify(asistenciaHistorialRepository).save(argThat(h -> h.getAccion() == AccionHistorial.CREAR
                && h.getEstadoAnteriorCodigo() == null
                && "A".equals(h.getEstadoNuevoCodigo())));
    }

    @Test
    void registrar_lanzaConflict_cuandoLaVersionEnviadaNoCoincideConLaActual() {
        when(orientadorRepository.findById(10L)).thenReturn(Optional.of(orientadorDeCentro));

        EstadoAsistencia estadoActual = new EstadoAsistencia();
        estadoActual.setCodigo("A");
        Asistencia existente = new Asistencia();
        existente.setEstado(estadoActual);
        existente.setVersion(3L);
        when(asistenciaRepository.findByOrientadorIdAndFecha(10L, LocalDate.of(2026, 7, 1)))
                .thenReturn(Optional.of(existente));

        EstadoAsistencia estadoFalta = new EstadoAsistencia();
        estadoFalta.setId(3L);
        estadoFalta.setCodigo("F");
        when(estadoAsistenciaRepository.findById(3L)).thenReturn(Optional.of(estadoFalta));

        AsistenciaUpsertRequest request = new AsistenciaUpsertRequest(10L, LocalDate.of(2026, 7, 1), 3L, "llegó tarde", 1L);

        assertThatThrownBy(() -> asistenciaService.registrar(request, supervisorCentro))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void registrar_lanzaBadRequest_cuandoElEstadoRequiereObservacionYNoSeEnvia() {
        when(orientadorRepository.findById(10L)).thenReturn(Optional.of(orientadorDeCentro));
        when(asistenciaRepository.findByOrientadorIdAndFecha(10L, LocalDate.of(2026, 7, 1))).thenReturn(Optional.empty());

        EstadoAsistencia estadoFalta = new EstadoAsistencia();
        estadoFalta.setId(3L);
        estadoFalta.setCodigo("F");
        estadoFalta.setNombre("Falta");
        estadoFalta.setRequiereObservacion(true);
        when(estadoAsistenciaRepository.findById(3L)).thenReturn(Optional.of(estadoFalta));

        AsistenciaUpsertRequest request = new AsistenciaUpsertRequest(10L, LocalDate.of(2026, 7, 1), 3L, "  ", null);

        assertThatThrownBy(() -> asistenciaService.registrar(request, supervisorCentro))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void obtenerGrilla_calculaTotalesContandoLosCodigosFijosDelMes() {
        EstadoAsistencia tardanza = new EstadoAsistencia();
        tardanza.setCodigo("T");
        EstadoAsistencia falta = new EstadoAsistencia();
        falta.setCodigo("F");

        Asistencia a1 = new Asistencia();
        a1.setOrientador(orientadorDeCentro);
        a1.setFecha(LocalDate.of(2026, 7, 2));
        a1.setEstado(tardanza);

        Asistencia a2 = new Asistencia();
        a2.setOrientador(orientadorDeCentro);
        a2.setFecha(LocalDate.of(2026, 7, 3));
        a2.setEstado(falta);

        when(orientadorRepository.buscar(eq(1L), any(), isNull())).thenReturn(List.of(orientadorDeCentro));
        when(asistenciaRepository.findByOrientadorIdInAndFechaBetween(eq(List.of(10L)), any(), any()))
                .thenReturn(List.of(a1, a2));

        AsistenciaGridResponse grilla = asistenciaService.obtenerGrilla(supervisorCentro, null, 2026, 7, false);

        assertThat(grilla.filas()).hasSize(1);
        AsistenciaGridResponse.Totales totales = grilla.filas().get(0).totales();
        assertThat(totales.tardanzas()).isEqualTo(1);
        assertThat(totales.faltas()).isEqualTo(1);
    }
}
