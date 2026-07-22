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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelExportServiceTest {

    @Mock
    private OrientadorRepository orientadorRepository;
    @Mock
    private AsistenciaRepository asistenciaRepository;
    @Mock
    private EstadoAsistenciaRepository estadoAsistenciaRepository;
    @Mock
    private ZonaService zonaService;

    @InjectMocks
    private ExcelExportService excelExportService;

    private Zona zonaCentro;
    private Orientador orientador;
    private AuthenticatedUser supervisorCentro;

    @BeforeEach
    void setUp() {
        zonaCentro = new Zona();
        zonaCentro.setId(1L);
        zonaCentro.setNombre("Zona Centro");

        Usuario supervisor = new Usuario();
        supervisor.setId(100L);
        supervisor.setNombreCompleto("Braian Ramirez");

        orientador = new Orientador();
        orientador.setId(10L);
        orientador.setNombres("Ana");
        orientador.setApellidos("Torres");
        orientador.setZona(zonaCentro);
        orientador.setSupervisor(supervisor);

        supervisorCentro = new AuthenticatedUser(100L, "braian", RolUsuario.SUPERVISOR, 1L);
    }

    @Test
    void generaUnaPlanillaConTitulo_leyendaYTotalesCorrectos() throws Exception {
        when(zonaService.obtener(1L)).thenReturn(zonaCentro);
        when(orientadorRepository.buscar(1L, EstadoOrientador.ACTIVO, null)).thenReturn(List.of(orientador));

        EstadoAsistencia falta = new EstadoAsistencia();
        falta.setCodigo("F");
        falta.setNombre("Falta");
        falta.setColorHex("#E2483D");

        EstadoAsistencia tardanza = new EstadoAsistencia();
        tardanza.setCodigo("T");
        tardanza.setNombre("Tardanza");
        tardanza.setColorHex("#F4B942");

        Asistencia a1 = new Asistencia();
        a1.setOrientador(orientador);
        a1.setFecha(LocalDate.of(2026, 7, 3));
        a1.setEstado(falta);

        Asistencia a2 = new Asistencia();
        a2.setOrientador(orientador);
        a2.setFecha(LocalDate.of(2026, 7, 6));
        a2.setEstado(tardanza);

        when(asistenciaRepository.findByOrientadorIdInAndFechaBetween(List.of(10L),
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31)))
                .thenReturn(List.of(a1, a2));

        when(estadoAsistenciaRepository.findAllByActivoTrueOrderByOrden()).thenReturn(List.of(falta, tardanza));

        ExcelExportService.ExcelFile archivo = excelExportService.exportarPlanillaMensual(supervisorCentro, null, 2026, 7);

        assertThat(archivo.nombreArchivo()).isEqualTo("asistencia-zona-centro-2026-07.xlsx");

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(archivo.contenido()))) {
            Sheet hoja = workbook.getSheet("GENERAL");
            assertThat(hoja).isNotNull();

            String titulo = hoja.getRow(0).getCell(0).getStringCellValue();
            assertThat(titulo).contains("ZONA CENTRO");

            String supervisorValor = hoja.getRow(1).getCell(2).getStringCellValue();
            assertThat(supervisorValor).isEqualTo("Braian Ramirez");

            String nombreOrientador = hoja.getRow(6).getCell(1).getStringCellValue();
            assertThat(nombreOrientador).isEqualTo("TORRES ANA");

            // dia 3 de julio 2026 -> columna 2 + (3-1) = 4
            String codigoDia3 = hoja.getRow(6).getCell(4).getStringCellValue();
            assertThat(codigoDia3).isEqualTo("F");

            Row filaOrientador = hoja.getRow(6);
            int numDias = LocalDate.of(2026, 7, 31).getDayOfMonth();
            int colTotalesInicio = 2 + numDias;
            assertThat(filaOrientador.getCell(colTotalesInicio).getNumericCellValue()).isEqualTo(1.0); // tardanzas
            assertThat(filaOrientador.getCell(colTotalesInicio + 1).getNumericCellValue()).isEqualTo(1.0); // faltas

            assertThat(hoja.getNumMergedRegions()).isGreaterThan(0);

            // La leyenda arranca en ROW_DATOS_INICIO(6) + numOrientadores(1) + 2 = 9
            String leyendaTitulo = hoja.getRow(9).getCell(0).getStringCellValue();
            assertThat(leyendaTitulo).isEqualTo("LEYENDA");
            assertThat(hoja.getRow(10).getCell(0).getStringCellValue()).isEqualTo("F");
            assertThat(hoja.getRow(10).getCell(1).getStringCellValue()).isEqualTo("= Falta");
        }
    }
}
