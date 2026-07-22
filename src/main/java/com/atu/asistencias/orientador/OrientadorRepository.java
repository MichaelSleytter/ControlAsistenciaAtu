package com.atu.asistencias.orientador;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrientadorRepository extends JpaRepository<Orientador, Long> {

    boolean existsByDni(String dni);

    Optional<Orientador> findByDni(String dni);

    @Query("""
            SELECT o FROM Orientador o
            LEFT JOIN FETCH o.zona
            LEFT JOIN FETCH o.supervisor
            LEFT JOIN FETCH o.turno
            WHERE (:zonaId IS NULL OR o.zona.id = :zonaId)
              AND (:estado IS NULL OR o.estado = :estado)
              AND (:texto IS NULL OR LOWER(o.nombres) LIKE %:texto% OR LOWER(o.apellidos) LIKE %:texto% OR o.dni LIKE %:texto%)
            ORDER BY o.apellidos, o.nombres
            """)
    List<Orientador> buscar(
            @Param("zonaId") Long zonaId,
            @Param("estado") EstadoOrientador estado,
            @Param("texto") String texto);
}
