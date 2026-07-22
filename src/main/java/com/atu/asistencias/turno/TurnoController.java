package com.atu.asistencias.turno;

import com.atu.asistencias.turno.dto.TurnoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoRepository turnoRepository;

    @GetMapping
    public List<TurnoResponse> listar() {
        return turnoRepository.findAllByActivoTrueOrderByNombre().stream().map(TurnoResponse::from).toList();
    }
}
