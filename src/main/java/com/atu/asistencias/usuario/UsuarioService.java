package com.atu.asistencias.usuario;

import com.atu.asistencias.common.exception.BadRequestException;
import com.atu.asistencias.common.exception.NotFoundException;
import com.atu.asistencias.usuario.dto.UsuarioRequest;
import com.atu.asistencias.usuario.dto.UsuarioResponse;
import com.atu.asistencias.usuario.dto.UsuarioUpdateRequest;
import com.atu.asistencias.zona.Zona;
import com.atu.asistencias.zona.ZonaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

/**
 * Todos los metodos publicos devuelven DTOs, construidos dentro de la propia
 * transaccion: con open-in-view deshabilitado, mapear asociaciones lazy
 * (usuario.getZona()) despues de que el metodo retorna lanza
 * LazyInitializationException.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final ZonaService zonaService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarSupervisores() {
        return usuarioRepository.findAllByRoleOrderByNombreCompleto(RolUsuario.SUPERVISOR)
                .stream().map(UsuarioResponse::from).toList();
    }

    @Transactional
    public UsuarioResponse crearSupervisor(UsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Ya existe un usuario con ese username");
        }
        Zona zona = zonaService.obtener(request.zonaId());

        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setEmail(request.email());
        usuario.setRole(RolUsuario.SUPERVISOR);
        usuario.setZona(zona);
        usuario.setDebeCambiarPassword(true);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizarSupervisor(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = obtenerEntidad(id);
        Zona zona = zonaService.obtener(request.zonaId());

        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setEmail(request.email());
        usuario.setZona(zona);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo) {
        Usuario usuario = obtenerEntidad(id);
        usuario.setActivo(activo);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public String resetearPassword(Long id) {
        Usuario usuario = obtenerEntidad(id);
        String temporal = generarPasswordTemporal();
        usuario.setPasswordHash(passwordEncoder.encode(temporal));
        usuario.setDebeCambiarPassword(true);
        usuarioRepository.save(usuario);
        return temporal;
    }

    private Usuario obtenerEntidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
    }

    private String generarPasswordTemporal() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
