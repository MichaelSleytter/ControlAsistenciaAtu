package com.atu.asistencias.export;

import com.atu.asistencias.asistencia.Asistencia;
import com.atu.asistencias.asistencia.AsistenciaRepository;
import com.atu.asistencias.estadoasistencia.EstadoAsistencia;
import com.atu.asistencias.estadoasistencia.EstadoAsistenciaRepository;
import com.atu.asistencias.orientador.EstadoOrientador;
import com.atu.asistencias.orientador.Orientador;
import com.atu.asistencias.orientador.OrientadorRepository;
import com.atu.asistencias.security.AuthenticatedUser;
import com.atu.asistencias.usuario.RolUsuario;
import com.atu.asistencias.usuario.Usuario;
import com.atu.asistencias.zona.Zona;
import com.atu.asistencias.zona.ZonaService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utilidad manual (no es un test JUnit) para generar un archivo .xlsx de muestra
 * y verificarlo visualmente en Excel. Ejecutar con:
 *   mvn -o test-compile
 *   java -cp target/test-classes;target/classes;&lt;classpath-de-dependencias&gt; com.atu.asistencias.export.SampleGenerator
 */
public final class SampleGenerator {

    private SampleGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Zona zonaCentro = new Zona();
        zonaCentro.setId(1L);
        zonaCentro.setNombre("Zona Centro");

        Usuario supervisor = new Usuario();
        supervisor.setId(100L);
        supervisor.setNombreCompleto("Braian Ramirez");

        Map<String, EstadoAsistencia> estados = crearCatalogoEstados();

        List<Orientador> orientadores = List.of(
                crearOrientador(1L, "Diana Carolina", "Alza Torres", zonaCentro, supervisor),
                crearOrientador(2L, "Luz Angelica", "Barco Chirinos", zonaCentro, supervisor),
                crearOrientador(3L, "Luis Alberto", "Chirre Mathey", zonaCentro, supervisor),
                crearOrientador(4L, "Ana Maria", "Guanilo Rojas", zonaCentro, supervisor)
        );

        LocalDate primerDia = LocalDate.of(2026, 7, 1);
        LocalDate ultimoDia = LocalDate.of(2026, 7, 31);
        List<Asistencia> asistencias = generarAsistencias(orientadores, estados, primerDia, ultimoDia);
        List<Long> ids = orientadores.stream().map(Orientador::getId).toList();

        OrientadorRepository orientadorRepository = mock(OrientadorRepository.class);
        when(orientadorRepository.buscar(anyLong(), any(), any())).thenReturn(orientadores);

        AsistenciaRepository asistenciaRepository = mock(AsistenciaRepository.class);
        when(asistenciaRepository.findByOrientadorIdInAndFechaBetween(any(), any(), any()))
                .thenReturn(asistencias.stream()
                        .filter(a -> ids.contains(a.getOrientador().getId())
                                && !a.getFecha().isBefore(primerDia) && !a.getFecha().isAfter(ultimoDia))
                        .toList());

        EstadoAsistenciaRepository estadoAsistenciaRepository = mock(EstadoAsistenciaRepository.class);
        when(estadoAsistenciaRepository.findAllByActivoTrueOrderByOrden()).thenReturn(List.copyOf(estados.values()));

        ZonaService zonaService = mock(ZonaService.class);
        when(zonaService.obtener(1L)).thenReturn(zonaCentro);

        ExcelExportService service = new ExcelExportService(
                orientadorRepository, asistenciaRepository, estadoAsistenciaRepository, zonaService);

        AuthenticatedUser supervisorAuth = new AuthenticatedUser(100L, "braian", RolUsuario.SUPERVISOR, 1L);
        ExcelExportService.ExcelFile archivo = service.exportarPlanillaMensual(supervisorAuth, null, 2026, 7);

        Path destino = Path.of("muestra-exportacion.xlsx");
        Files.write(destino, archivo.contenido());
        System.out.println("Archivo generado en: " + destino.toAbsolutePath());
    }

    private static Orientador crearOrientador(Long id, String nombres, String apellidos, Zona zona, Usuario supervisor) {
        Orientador o = new Orientador();
        o.setId(id);
        o.setNombres(nombres);
        o.setApellidos(apellidos);
        o.setZona(zona);
        o.setSupervisor(supervisor);
        o.setEstado(EstadoOrientador.ACTIVO);
        return o;
    }

    private static Map<String, EstadoAsistencia> crearCatalogoEstados() {
        Map<String, EstadoAsistencia> mapa = new LinkedHashMap<>();
        agregarEstado(mapa, "A", "Asistió", "#FFFFFF");
        agregarEstado(mapa, "T", "Tardanza", "#F4B942");
        agregarEstado(mapa, "F", "Falta", "#E2483D");
        agregarEstado(mapa, "FJ", "Falta Justificada", "#E8926A");
        agregarEstado(mapa, "D", "Descanso", "#5AA552");
        agregarEstado(mapa, "DC", "Descanso Compensatorio", "#3A7FD5");
        agregarEstado(mapa, "DM", "Descanso Médico", "#4BC4D9");
        agregarEstado(mapa, "DF", "Descanso Feriado", "#C9B07A");
        agregarEstado(mapa, "V", "Vacaciones", "#8768B0");
        agregarEstado(mapa, "O", "Onomástico", "#9AA19C");
        agregarEstado(mapa, "R", "Renunció", "#8A8F8A");
        return mapa;
    }

    private static void agregarEstado(Map<String, EstadoAsistencia> mapa, String codigo, String nombre, String colorHex) {
        EstadoAsistencia estado = new EstadoAsistencia();
        estado.setCodigo(codigo);
        estado.setNombre(nombre);
        estado.setColorHex(colorHex);
        mapa.put(codigo, estado);
    }

    private static List<Asistencia> generarAsistencias(List<Orientador> orientadores, Map<String, EstadoAsistencia> estados,
                                                         LocalDate primerDia, LocalDate ultimoDia) {
        List<Asistencia> asistencias = new ArrayList<>();
        String[] patronPorOrientador = {"A", "D", "DM", "F"};
        int index = 0;
        for (Orientador o : orientadores) {
            String codigoEspecial = patronPorOrientador[index++ % patronPorOrientador.length];
            for (LocalDate dia = primerDia; !dia.isAfter(ultimoDia); dia = dia.plusDays(1)) {
                String codigo = "A";
                if (dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    codigo = "D";
                } else if (dia.getDayOfMonth() % 11 == 0) {
                    codigo = codigoEspecial;
                } else if (dia.getDayOfMonth() == 5 + index) {
                    codigo = "T";
                }
                Asistencia a = new Asistencia();
                a.setOrientador(o);
                a.setFecha(dia);
                a.setEstado(estados.get(codigo));
                asistencias.add(a);
            }
        }
        return asistencias;
    }
}
