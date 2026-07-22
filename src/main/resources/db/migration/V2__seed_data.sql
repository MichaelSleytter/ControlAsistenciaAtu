INSERT INTO zonas (nombre, descripcion) VALUES
    ('Zona Centro', 'Zona Centro'),
    ('Zona Sur', 'Zona Sur'),
    ('Zona Norte', 'Zona Norte');

INSERT INTO turnos (nombre, hora_inicio, hora_fin) VALUES
    ('Mañana', '07:00:00', '14:00:00'),
    ('Tarde', '14:00:00', '21:00:00'),
    ('Noche', '21:00:00', '06:00:00');

-- Colores aproximados a partir de las imagenes de referencia del Excel actual.
-- Ajustar con el valor exacto muestreado del archivo original antes de construir
-- la plantilla de exportacion (ver seccion "Exportacion a Excel" del documento
-- de arquitectura).
INSERT INTO estados_asistencia (codigo, nombre, color_hex, requiere_observacion, orden) VALUES
    ('A',  'Asistió',                '#FFFFFF', FALSE, 1),
    ('T',  'Tardanza',                '#F4B942', FALSE, 2),
    ('F',  'Falta',                    '#E2483D', TRUE,  3),
    ('FJ', 'Falta Justificada',         '#E8926A', TRUE,  4),
    ('D',  'Descanso',                  '#5AA552', FALSE, 5),
    ('DC', 'Descanso Compensatorio',     '#3A7FD5', FALSE, 6),
    ('DM', 'Descanso Médico',            '#4BC4D9', TRUE,  7),
    ('DF', 'Descanso Feriado',            '#C9B07A', FALSE, 8),
    ('V',  'Vacaciones',                   '#8768B0', FALSE, 9),
    ('O',  'Onomástico',                    '#9AA19C', FALSE, 10),
    ('R',  'Renunció',                       '#8A8F8A', TRUE,  11);

-- Usuario administrador inicial. Contraseña temporal: Admin123!
-- debe_cambiar_password obliga a definir una nueva password en el primer login.
INSERT INTO usuarios (username, password_hash, nombre_completo, email, role, zona_id, debe_cambiar_password) VALUES
    ('admin', '$2b$12$4qx58TW4jhUCnp68Kz69QetNBncH6slaiQK4L5KAXiw6bZt90OcJG', 'Administrador General', NULL, 'ADMIN', NULL, TRUE);
