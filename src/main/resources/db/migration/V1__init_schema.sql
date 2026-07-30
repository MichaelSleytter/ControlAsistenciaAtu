-- Funcion compartida para mantener updated_at al dia (Postgres no tiene el
-- "ON UPDATE CURRENT_TIMESTAMP" de MySQL; se resuelve con un trigger).
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE zonas (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(80)  NOT NULL,
    descripcion     VARCHAR(255) NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_zonas_nombre UNIQUE (nombre)
);

CREATE TABLE usuarios (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username          VARCHAR(60)  NOT NULL,
    password_hash     VARCHAR(100) NOT NULL,
    nombre_completo   VARCHAR(150) NOT NULL,
    email             VARCHAR(150) NULL,
    role              VARCHAR(20)  NOT NULL,
    zona_id           BIGINT       NULL,
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    debe_cambiar_password BOOLEAN  NOT NULL DEFAULT FALSE,
    ultimo_login_at   TIMESTAMPTZ  NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_usuarios_username UNIQUE (username),
    CONSTRAINT fk_usuarios_zona FOREIGN KEY (zona_id) REFERENCES zonas (id),
    CONSTRAINT chk_usuarios_role CHECK (role IN ('ADMIN', 'SUPERVISOR'))
);

CREATE TRIGGER trg_usuarios_updated_at BEFORE UPDATE ON usuarios
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX ix_usuarios_zona ON usuarios (zona_id, activo);

CREATE TABLE turnos (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre       VARCHAR(60) NOT NULL,
    hora_inicio  TIME NULL,
    hora_fin     TIME NULL,
    activo       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_turnos_nombre UNIQUE (nombre)
);

CREATE TABLE orientadores (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombres         VARCHAR(120) NOT NULL,
    apellidos       VARCHAR(120) NOT NULL,
    dni             VARCHAR(20)  NOT NULL,
    codigo_interno  VARCHAR(40)  NULL,
    zona_id         BIGINT       NOT NULL,
    supervisor_id   BIGINT       NULL,
    turno_id        BIGINT       NULL,
    fecha_ingreso   DATE         NOT NULL,
    fecha_cese      DATE         NULL,
    estado          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_orientadores_dni UNIQUE (dni),
    CONSTRAINT fk_orientadores_zona FOREIGN KEY (zona_id) REFERENCES zonas (id),
    CONSTRAINT fk_orientadores_supervisor FOREIGN KEY (supervisor_id) REFERENCES usuarios (id),
    CONSTRAINT fk_orientadores_turno FOREIGN KEY (turno_id) REFERENCES turnos (id),
    CONSTRAINT chk_orientadores_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TRIGGER trg_orientadores_updated_at BEFORE UPDATE ON orientadores
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX ix_orientadores_zona_estado ON orientadores (zona_id, estado);

CREATE TABLE estados_asistencia (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo                 VARCHAR(10)  NOT NULL,
    nombre                 VARCHAR(80)  NOT NULL,
    color_hex              VARCHAR(7)   NOT NULL,
    requiere_observacion   BOOLEAN      NOT NULL DEFAULT FALSE,
    orden                  INT          NOT NULL DEFAULT 0,
    activo                 BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_estados_codigo UNIQUE (codigo)
);

CREATE TABLE asistencias (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    orientador_id   BIGINT   NOT NULL,
    fecha           DATE     NOT NULL,
    estado_id       BIGINT   NOT NULL,
    observacion     VARCHAR(500) NULL,
    registrado_por  BIGINT   NOT NULL,
    version         BIGINT   NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_asistencia_dia UNIQUE (orientador_id, fecha),
    CONSTRAINT fk_asistencias_orientador FOREIGN KEY (orientador_id) REFERENCES orientadores (id),
    CONSTRAINT fk_asistencias_estado FOREIGN KEY (estado_id) REFERENCES estados_asistencia (id),
    CONSTRAINT fk_asistencias_usuario FOREIGN KEY (registrado_por) REFERENCES usuarios (id)
);

CREATE TRIGGER trg_asistencias_updated_at BEFORE UPDATE ON asistencias
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX ix_asistencias_fecha ON asistencias (fecha);

CREATE TABLE asistencias_historial (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    orientador_id         BIGINT      NOT NULL,
    fecha                 DATE        NOT NULL,
    usuario_id            BIGINT      NOT NULL,
    accion                VARCHAR(20) NOT NULL,
    estado_anterior_codigo VARCHAR(10) NULL,
    estado_nuevo_codigo    VARCHAR(10) NULL,
    fecha_hora            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_historial_orientador FOREIGN KEY (orientador_id) REFERENCES orientadores (id),
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT chk_historial_accion CHECK (accion IN ('CREAR', 'EDITAR', 'ELIMINAR'))
);

CREATE INDEX ix_historial_orientador_fecha ON asistencias_historial (orientador_id, fecha);
