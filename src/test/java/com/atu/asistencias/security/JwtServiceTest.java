package com.atu.asistencias.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "unit-test-secret-key-value-with-more-than-32-characters",
                15,
                7);
    }

    @Test
    void generaYValidaUnAccessTokenCorrectamente() {
        String token = jwtService.generateAccessToken("braian");

        assertThat(jwtService.isValid(token, "access")).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("braian");
    }

    @Test
    void unAccessTokenNoEsValidoComoRefreshToken() {
        String token = jwtService.generateAccessToken("braian");

        assertThat(jwtService.isValid(token, "refresh")).isFalse();
    }

    @Test
    void unTokenManipuladoNoEsValido() {
        String token = jwtService.generateAccessToken("braian");
        String manipulado = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.isValid(manipulado, "access")).isFalse();
    }

    @Test
    void generaYValidaUnRefreshTokenCorrectamente() {
        String token = jwtService.generateRefreshToken("braian");

        assertThat(jwtService.isValid(token, "refresh")).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("braian");
    }
}
