package com.atu.asistencias.usuario;

import com.atu.asistencias.usuario.dto.ResetPasswordResponse;
import com.atu.asistencias.usuario.dto.UsuarioRequest;
import com.atu.asistencias.usuario.dto.UsuarioResponse;
import com.atu.asistencias.usuario.dto.UsuarioUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioResponse> listarSupervisores() {
        return usuarioService.listarSupervisores();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.crearSupervisor(request);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.actualizarSupervisor(id, request);
    }

    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return usuarioService.cambiarEstado(id, activo);
    }

    @PostMapping("/{id}/reset-password")
    public ResetPasswordResponse resetearPassword(@PathVariable Long id) {
        return new ResetPasswordResponse(usuarioService.resetearPassword(id));
    }
}
