package com.atu.asistencias.zona;

import com.atu.asistencias.zona.dto.ZonaRequest;
import com.atu.asistencias.zona.dto.ZonaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
public class ZonaController {

    private final ZonaService zonaService;

    @GetMapping
    public List<ZonaResponse> listar(@RequestParam(defaultValue = "false") boolean incluirInactivas) {
        List<Zona> zonas = incluirInactivas ? zonaService.listarTodas() : zonaService.listarActivas();
        return zonas.stream().map(ZonaResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ZonaResponse crear(@Valid @RequestBody ZonaRequest request) {
        return ZonaResponse.from(zonaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ZonaResponse actualizar(@PathVariable Long id, @Valid @RequestBody ZonaRequest request) {
        return ZonaResponse.from(zonaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ZonaResponse cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return ZonaResponse.from(zonaService.cambiarEstado(id, activo));
    }
}
