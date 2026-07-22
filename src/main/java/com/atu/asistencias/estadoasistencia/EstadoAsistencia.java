package com.atu.asistencias.estadoasistencia;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estados_asistencia")
@Getter
@Setter
@NoArgsConstructor
public class EstadoAsistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "color_hex", nullable = false, length = 7)
    private String colorHex;

    @Column(name = "requiere_observacion", nullable = false)
    private boolean requiereObservacion = false;

    @Column(nullable = false)
    private int orden = 0;

    @Column(nullable = false)
    private boolean activo = true;
}
