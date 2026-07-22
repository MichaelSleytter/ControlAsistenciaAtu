package com.atu.asistencias.zona;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ZonaRepository extends JpaRepository<Zona, Long> {

    List<Zona> findAllByActivoTrueOrderByNombre();

    List<Zona> findAllByOrderByNombre();

    Optional<Zona> findByNombreIgnoreCase(String nombre);
}
