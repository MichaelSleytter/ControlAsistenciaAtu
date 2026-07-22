package com.atu.asistencias.orientador;

import com.atu.asistencias.turno.Turno;
import com.atu.asistencias.usuario.Usuario;
import com.atu.asistencias.zona.Zona;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "orientadores")
@Getter
@Setter
@NoArgsConstructor
public class Orientador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombres;

    @Column(nullable = false, length = 120)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(name = "codigo_interno", length = 40)
    private String codigoInterno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private Usuario supervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id")
    private Turno turno;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_cese")
    private LocalDate fechaCese;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrientador estado = EstadoOrientador.ACTIVO;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private Instant updatedAt;
}
