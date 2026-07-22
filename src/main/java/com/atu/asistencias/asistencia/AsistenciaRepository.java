package com.atu.asistencias.asistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    Optional<Asistencia> findByOrientadorIdAndFecha(Long orientadorId, LocalDate fecha);

    List<Asistencia> findByOrientadorIdInAndFechaBetween(List<Long> orientadorIds, LocalDate desde, LocalDate hasta);

    void deleteByOrientadorIdAndFecha(Long orientadorId, LocalDate fecha);
}
