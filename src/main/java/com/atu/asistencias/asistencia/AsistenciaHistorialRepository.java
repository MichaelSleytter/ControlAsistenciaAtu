package com.atu.asistencias.asistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AsistenciaHistorialRepository extends JpaRepository<AsistenciaHistorial, Long> {

    List<AsistenciaHistorial> findByOrientadorIdAndFechaOrderByFechaHoraDesc(Long orientadorId, LocalDate fecha);
}
