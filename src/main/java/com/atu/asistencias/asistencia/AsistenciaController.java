package com.atu.asistencias.asistencia;

import com.atu.asistencias.asistencia.dto.AsistenciaCeldaResponse;
import com.atu.asistencias.asistencia.dto.AsistenciaGridResponse;
import com.atu.asistencias.asistencia.dto.AsistenciaHistorialResponse;
import com.atu.asistencias.asistencia.dto.AsistenciaUpsertRequest;
import com.atu.asistencias.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
@RequiredArgsConstructor
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    @GetMapping("/grilla")
    public AsistenciaGridResponse obtenerGrilla(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Long zonaId,
            @RequestParam int anio,
            @RequestParam int mes,
            @RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return asistenciaService.obtenerGrilla(principal.toAuthenticatedUser(), zonaId, anio, mes, incluirInactivos);
    }

    @PutMapping
    public AsistenciaCeldaResponse registrar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody AsistenciaUpsertRequest request) {
        return asistenciaService.registrar(request, principal.toAuthenticatedUser());
    }

    @GetMapping("/historial")
    public List<AsistenciaHistorialResponse> historial(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam Long orientadorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return asistenciaService.historialDeCelda(orientadorId, fecha, principal.toAuthenticatedUser());
    }
}
