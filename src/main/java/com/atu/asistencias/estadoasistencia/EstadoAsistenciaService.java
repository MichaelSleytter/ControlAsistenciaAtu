package com.atu.asistencias.estadoasistencia;

import com.atu.asistencias.common.exception.BadRequestException;
import com.atu.asistencias.common.exception.NotFoundException;
import com.atu.asistencias.estadoasistencia.dto.EstadoAsistenciaRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadoAsistenciaService {

    private final EstadoAsistenciaRepository estadoAsistenciaRepository;

    @Transactional(readOnly = true)
    public List<EstadoAsistencia> listarActivos() {
        return estadoAsistenciaRepository.findAllByActivoTrueOrderByOrden();
    }

    @Transactional(readOnly = true)
    public List<EstadoAsistencia> listarTodos() {
        return estadoAsistenciaRepository.findAllByOrderByOrden();
    }

    @Transactional(readOnly = true)
    public EstadoAsistencia obtener(Long id) {
        return estadoAsistenciaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Estado de asistencia no encontrado: " + id));
    }

    @Transactional
    public EstadoAsistencia crear(EstadoAsistenciaRequest request) {
        if (estadoAsistenciaRepository.existsByCodigoIgnoreCase(request.codigo())) {
            throw new BadRequestException("Ya existe un estado con ese código");
        }
        EstadoAsistencia estado = new EstadoAsistencia();
        aplicar(estado, request);
        return estadoAsistenciaRepository.save(estado);
    }

    @Transactional
    public EstadoAsistencia actualizar(Long id, EstadoAsistenciaRequest request) {
        EstadoAsistencia estado = obtener(id);
        if (!estado.getCodigo().equalsIgnoreCase(request.codigo())
                && estadoAsistenciaRepository.existsByCodigoIgnoreCase(request.codigo())) {
            throw new BadRequestException("Ya existe un estado con ese código");
        }
        aplicar(estado, request);
        return estadoAsistenciaRepository.save(estado);
    }

    @Transactional
    public EstadoAsistencia cambiarEstado(Long id, boolean activo) {
        EstadoAsistencia estado = obtener(id);
        estado.setActivo(activo);
        return estadoAsistenciaRepository.save(estado);
    }

    private void aplicar(EstadoAsistencia estado, EstadoAsistenciaRequest request) {
        estado.setCodigo(request.codigo().toUpperCase());
        estado.setNombre(request.nombre());
        estado.setColorHex(request.colorHex());
        estado.setRequiereObservacion(request.requiereObservacion());
        estado.setOrden(request.orden());
    }
}
