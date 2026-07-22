package com.atu.asistencias.estadoasistencia;

import com.atu.asistencias.estadoasistencia.dto.EstadoAsistenciaRequest;
import com.atu.asistencias.estadoasistencia.dto.EstadoAsistenciaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estados-asistencia")
@RequiredArgsConstructor
public class EstadoAsistenciaController {

    private final EstadoAsistenciaService estadoAsistenciaService;

    @GetMapping
    public List<EstadoAsistenciaResponse> listar(@RequestParam(defaultValue = "false") boolean incluirInactivos) {
        List<EstadoAsistencia> estados = incluirInactivos
                ? estadoAsistenciaService.listarTodos()
                : estadoAsistenciaService.listarActivos();
        return estados.stream().map(EstadoAsistenciaResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public EstadoAsistenciaResponse crear(@Valid @RequestBody EstadoAsistenciaRequest request) {
        return EstadoAsistenciaResponse.from(estadoAsistenciaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EstadoAsistenciaResponse actualizar(@PathVariable Long id, @Valid @RequestBody EstadoAsistenciaRequest request) {
        return EstadoAsistenciaResponse.from(estadoAsistenciaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public EstadoAsistenciaResponse cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return EstadoAsistenciaResponse.from(estadoAsistenciaService.cambiarEstado(id, activo));
    }
}
