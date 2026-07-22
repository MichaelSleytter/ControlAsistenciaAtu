package com.atu.asistencias.orientador;

import com.atu.asistencias.orientador.dto.OrientadorRequest;
import com.atu.asistencias.orientador.dto.OrientadorResponse;
import com.atu.asistencias.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orientadores")
@RequiredArgsConstructor
public class OrientadorController {

    private final OrientadorService orientadorService;

    @GetMapping
    public List<OrientadorResponse> listar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) EstadoOrientador estado,
            @RequestParam(required = false) String q) {
        return orientadorService.listar(principal.toAuthenticatedUser(), zonaId, estado, q);
    }

    @GetMapping("/{id}")
    public OrientadorResponse obtener(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        return orientadorService.obtener(id, principal.toAuthenticatedUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrientadorResponse crear(@AuthenticationPrincipal CustomUserDetails principal,
                                     @Valid @RequestBody OrientadorRequest request) {
        return orientadorService.crear(request, principal.toAuthenticatedUser());
    }

    @PutMapping("/{id}")
    public OrientadorResponse actualizar(@AuthenticationPrincipal CustomUserDetails principal,
                                          @PathVariable Long id,
                                          @Valid @RequestBody OrientadorRequest request) {
        return orientadorService.actualizar(id, request, principal.toAuthenticatedUser());
    }

    @PatchMapping("/{id}/estado")
    public OrientadorResponse cambiarEstado(@AuthenticationPrincipal CustomUserDetails principal,
                                             @PathVariable Long id,
                                             @RequestParam EstadoOrientador estado,
                                             @RequestParam(required = false) LocalDate fechaCese) {
        return orientadorService.cambiarEstado(id, principal.toAuthenticatedUser(), estado, fechaCese);
    }
}
