package com.atu.asistencias.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.zona WHERE u.role = :role ORDER BY u.nombreCompleto")
    List<Usuario> findAllByRoleOrderByNombreCompleto(@Param("role") RolUsuario role);
}
