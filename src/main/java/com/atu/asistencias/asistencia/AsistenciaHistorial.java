package com.atu.asistencias.asistencia;

import com.atu.asistencias.orientador.Orientador;
import com.atu.asistencias.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "asistencias_historial")
@Getter
@Setter
@NoArgsConstructor
public class AsistenciaHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orientador_id", nullable = false)
    private Orientador orientador;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccionHistorial accion;

    @Column(name = "estado_anterior_codigo", length = 10)
    private String estadoAnteriorCodigo;

    @Column(name = "estado_nuevo_codigo", length = 10)
    private String estadoNuevoCodigo;

    @Column(name = "fecha_hora", nullable = false, updatable = false, insertable = false)
    private Instant fechaHora;
}
