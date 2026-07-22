package com.atu.asistencias.zona;

import com.atu.asistencias.common.exception.BadRequestException;
import com.atu.asistencias.common.exception.NotFoundException;
import com.atu.asistencias.zona.dto.ZonaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZonaService {

    private final ZonaRepository zonaRepository;

    @Transactional(readOnly = true)
    public List<Zona> listarActivas() {
        return zonaRepository.findAllByActivoTrueOrderByNombre();
    }

    @Transactional(readOnly = true)
    public List<Zona> listarTodas() {
        return zonaRepository.findAllByOrderByNombre();
    }

    @Transactional(readOnly = true)
    public Zona obtener(Long id) {
        return zonaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Zona no encontrada: " + id));
    }

    @Transactional
    public Zona crear(ZonaRequest request) {
        zonaRepository.findByNombreIgnoreCase(request.nombre()).ifPresent(z -> {
            throw new BadRequestException("Ya existe una zona con ese nombre");
        });
        Zona zona = new Zona();
        zona.setNombre(request.nombre());
        zona.setDescripcion(request.descripcion());
        return zonaRepository.save(zona);
    }

    @Transactional
    public Zona actualizar(Long id, ZonaRequest request) {
        Zona zona = obtener(id);
        zona.setNombre(request.nombre());
        zona.setDescripcion(request.descripcion());
        return zonaRepository.save(zona);
    }

    @Transactional
    public Zona cambiarEstado(Long id, boolean activo) {
        Zona zona = obtener(id);
        zona.setActivo(activo);
        return zonaRepository.save(zona);
    }
}
