package com.atu.asistencias.auth;

import com.atu.asistencias.auth.dto.LoginResponse;
import com.atu.asistencias.auth.dto.TokenResponse;
import com.atu.asistencias.security.CustomUserDetails;
import com.atu.asistencias.security.JwtService;
import com.atu.asistencias.usuario.Usuario;
import com.atu.asistencias.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Orquesta el login manteniendo la sesion de Hibernate abierta hasta construir
 * la respuesta: con open-in-view deshabilitado, mapear asociaciones lazy (p.ej.
 * usuario.getZona()) fuera de una transaccion lanza LazyInitializationException.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public LoginResponse login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        Usuario usuario = ((CustomUserDetails) authentication.getPrincipal()).getUsuario();
        usuario.setUltimoLoginAt(Instant.now());
        usuarioRepository.save(usuario);

        String accessToken = jwtService.generateAccessToken(usuario.getUsername());
        String refreshToken = jwtService.generateRefreshToken(usuario.getUsername());
        return LoginResponse.of(accessToken, refreshToken, usuario);
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        if (!jwtService.isValid(refreshToken, "refresh")) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }
        String username = jwtService.extractUsername(refreshToken);
        Usuario usuario = usuarioRepository.findByUsername(username)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new BadCredentialsException("Usuario no válido"));

        return new TokenResponse(jwtService.generateAccessToken(usuario.getUsername()));
    }
}
