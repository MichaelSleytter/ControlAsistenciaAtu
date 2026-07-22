package com.atu.asistencias.orientador;

import com.atu.asistencias.common.exception.BadRequestException;
import com.atu.asistencias.orientador.dto.OrientadorRequest;
import com.atu.asistencias.security.AuthenticatedUser;
import com.atu.asistencias.turno.TurnoRepository;
import com.atu.asistencias.usuario.RolUsuario;
import com.atu.asistencias.usuario.Usuario;
import com.atu.asistencias.usuario.UsuarioRepository;
import com.atu.asistencias.zona.Zona;
import com.atu.asistencias.zona.ZonaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrientadorServiceTest {

    @Mock
    private OrientadorRepository orientadorRepository;
    @Mock
    private ZonaService zonaService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TurnoRepository turnoRepository;

    @InjectMocks
    private OrientadorService orientadorService;

    private Zona zonaCentro;
    private Zona zonaSur;
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

        supervisorCentro = new AuthenticatedUser(100L, "braian", RolUsuario.SUPERVISOR, 1L);
        admin = new AuthenticatedUser(1L, "admin", RolUsuario.ADMIN, null);
    }

    @Test
    void obtener_lanzaAccessDenied_cuandoElSupervisorConsultaUnOrientadorDeOtraZona() {
        Orientador orientadorDeSur = new Orientador();
        orientadorDeSur.setId(20L);
        orientadorDeSur.setZona(zonaSur);
        when(orientadorRepository.findById(20L)).thenReturn(Optional.of(orientadorDeSur));

        assertThatThrownBy(() -> orientadorService.obtener(20L, supervisorCentro))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void obtener_permiteAlAdminConsultarCualquierZona() {
        Orientador orientadorDeSur = new Orientador();
        orientadorDeSur.setId(20L);
        orientadorDeSur.setZona(zonaSur);
        when(orientadorRepository.findById(20L)).thenReturn(Optional.of(orientadorDeSur));

        var resultado = orientadorService.obtener(20L, admin);

        assertThat(resultado.id()).isEqualTo(20L);
    }

    @Test
    void crear_forzaLaZonaDelSupervisorAutenticado_ignorandoLaZonaEnviadaEnElRequest() {
        when(orientadorRepository.existsByDni("12345678")).thenReturn(false);
        when(zonaService.obtener(1L)).thenReturn(zonaCentro);
        Usuario supervisorUsuario = new Usuario();
        supervisorUsuario.setId(100L);
        when(usuarioRepository.findById(100L)).thenReturn(Optional.of(supervisorUsuario));
        when(orientadorRepository.save(org.mockito.ArgumentMatchers.any(Orientador.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // El supervisor intenta (maliciosamente o por error) enviar zonaId=2 (Zona Sur)
        OrientadorRequest request = new OrientadorRequest(
                "Ana", "Torres", "12345678", null, 2L, null, null, LocalDate.of(2026, 1, 1));

        var creado = orientadorService.crear(request, supervisorCentro);

        assertThat(creado.zonaId()).isEqualTo(1L);
    }

    @Test
    void crear_exigeZonaExplicita_cuandoElActorEsAdmin() {
        when(orientadorRepository.existsByDni("87654321")).thenReturn(false);

        OrientadorRequest request = new OrientadorRequest(
                "Luis", "Perez", "87654321", null, null, null, null, LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> orientadorService.crear(request, admin))
                .isInstanceOf(BadRequestException.class);
    }
}
