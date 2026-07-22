package com.atu.asistencias.estadoasistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstadoAsistenciaRepository extends JpaRepository<EstadoAsistencia, Long> {

    List<EstadoAsistencia> findAllByActivoTrueOrderByOrden();

    List<EstadoAsistencia> findAllByOrderByOrden();

    Optional<EstadoAsistencia> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);
}
