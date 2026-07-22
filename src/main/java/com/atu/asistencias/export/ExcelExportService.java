package com.atu.asistencias.export;

import com.atu.asistencias.asistencia.Asistencia;
import com.atu.asistencias.asistencia.AsistenciaRepository;
import com.atu.asistencias.common.exception.BadRequestException;
import com.atu.asistencias.estadoasistencia.EstadoAsistencia;
import com.atu.asistencias.estadoasistencia.EstadoAsistenciaRepository;
import com.atu.asistencias.orientador.EstadoOrientador;
import com.atu.asistencias.orientador.Orientador;
import com.atu.asistencias.orientador.OrientadorRepository;
import com.atu.asistencias.security.AuthenticatedUser;
import com.atu.asistencias.usuario.Usuario;
import com.atu.asistencias.zona.Zona;
import com.atu.asistencias.zona.ZonaService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private static final int COL_NUMERO = 0;
    private static final int COL_NOMBRE = 1;
    private static final int COL_PRIMER_DIA = 2;
    private static final int ROW_TITULO = 0;
    private static final int ROW_SUPERVISOR = 1;
    private static final int ROW_MES = 2;
    private static final int ROW_SEMANA = 3;
    private static final int ROW_DIA = 4;
    private static final int ROW_DIA_SEMANA = 5;
    private static final int ROW_DATOS_INICIO = 6;

    private static final Locale LOCALE_ES = Locale.forLanguageTag("es-PE");

    private final OrientadorRepository orientadorRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final EstadoAsistenciaRepository estadoAsistenciaRepository;
    private final ZonaService zonaService;

    @Transactional(readOnly = true)
    public ExcelFile exportarPlanillaMensual(AuthenticatedUser actor, Long zonaIdParam, int anio, int mes) {
        Long zonaId = actor.esAdmin() ? zonaIdParam : actor.zonaId();
        if (zonaId == null) {
            throw new BadRequestException("La zona es obligatoria para exportar la planilla");
        }
        Zona zona = zonaService.obtener(zonaId);

        List<Orientador> orientadores = orientadorRepository.buscar(zonaId, EstadoOrientador.ACTIVO, null);
        LocalDate primerDia = LocalDate.of(anio, mes, 1);
        LocalDate ultimoDia = primerDia.withDayOfMonth(primerDia.lengthOfMonth());

        Map<Long, Map<LocalDate, Asistencia>> asistenciasPorOrientador = cargarAsistencias(orientadores, primerDia, ultimoDia);
        List<EstadoAsistencia> catalogoEstados = estadoAsistenciaRepository.findAllByActivoTrueOrderByOrden();
        String nombreSupervisor = orientadores.stream()
                .map(Orientador::getSupervisor)
                .filter(Objects::nonNull)
                .map(Usuario::getNombreCompleto)
                .findFirst()
                .orElse("(sin asignar)");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet hoja = workbook.createSheet("GENERAL");
            ExcelStyles styles = new ExcelStyles(workbook);

            int ultimaColumna = construirEncabezado(hoja, styles, zona, nombreSupervisor, primerDia, ultimoDia);
            construirFilasOrientadores(hoja, styles, orientadores, asistenciasPorOrientador, primerDia, ultimoDia);
            construirLeyenda(hoja, styles, catalogoEstados, ROW_DATOS_INICIO + orientadores.size() + 2);
            ajustarColumnas(hoja, ultimaColumna, ultimoDia.getDayOfMonth());
            hoja.createFreezePane(COL_PRIMER_DIA, ROW_DATOS_INICIO);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            String nombreArchivo = "asistencia-%s-%d-%02d.xlsx".formatted(
                    zona.getNombre().toLowerCase(LOCALE_ES).replace(" ", "-"), anio, mes);
            return new ExcelFile(nombreArchivo, out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel", e);
        }
    }

    private Map<Long, Map<LocalDate, Asistencia>> cargarAsistencias(List<Orientador> orientadores, LocalDate desde, LocalDate hasta) {
        Map<Long, Map<LocalDate, Asistencia>> resultado = new HashMap<>();
        if (orientadores.isEmpty()) {
            return resultado;
        }
        List<Long> ids = orientadores.stream().map(Orientador::getId).toList();
        for (Asistencia a : asistenciaRepository.findByOrientadorIdInAndFechaBetween(ids, desde, hasta)) {
            resultado.computeIfAbsent(a.getOrientador().getId(), k -> new HashMap<>()).put(a.getFecha(), a);
        }
        return resultado;
    }

    private int construirEncabezado(XSSFSheet hoja, ExcelStyles styles, Zona zona, String nombreSupervisor,
                                     LocalDate primerDia, LocalDate ultimoDia) {
        int numDias = ultimoDia.getDayOfMonth();
        int colTotalesInicio = COL_PRIMER_DIA + numDias;
        int ultimaColumna = colTotalesInicio + 3;

        String titulo = "LISTA DE ASISTENCIA - %s".formatted(zona.getNombre().toUpperCase(LOCALE_ES));
        mergeYEscribir(hoja, styles.titulo(), ROW_TITULO, ROW_TITULO, 0, ultimaColumna, titulo);

        mergeYEscribir(hoja, styles.etiqueta(), ROW_SUPERVISOR, ROW_SUPERVISOR, COL_NUMERO, COL_NOMBRE, "SUPERVISOR:");
        mergeYEscribir(hoja, styles.valor(), ROW_SUPERVISOR, ROW_SUPERVISOR, COL_PRIMER_DIA, ultimaColumna, nombreSupervisor);

        String nombreMes = primerDia.getMonth().getDisplayName(TextStyle.FULL, LOCALE_ES).toUpperCase(LOCALE_ES)
                + " " + primerDia.getYear();
        mergeYEscribir(hoja, styles.etiqueta(), ROW_MES, ROW_MES, COL_NUMERO, COL_NOMBRE, "MES:");
        mergeYEscribir(hoja, styles.valor(), ROW_MES, ROW_MES, COL_PRIMER_DIA, ultimaColumna, nombreMes);

        mergeYEscribir(hoja, styles.etiqueta(), ROW_SEMANA, ROW_SEMANA, COL_NUMERO, COL_NOMBRE, "SEMANA:");
        mergeYEscribir(hoja, styles.etiqueta(), ROW_DIA, ROW_DIA, COL_NUMERO, COL_NOMBRE, "DÍA:");

        escribirCelda(hoja, styles.encabezadoColumna(), ROW_DIA_SEMANA, COL_NUMERO, "N°");
        escribirCelda(hoja, styles.encabezadoColumna(), ROW_DIA_SEMANA, COL_NOMBRE, "ORIENTADOR/A");

        construirBandasSemana(hoja, styles, primerDia, ultimoDia);

        for (LocalDate dia = primerDia; !dia.isAfter(ultimoDia); dia = dia.plusDays(1)) {
            int col = COL_PRIMER_DIA + dia.getDayOfMonth() - 1;
            escribirCelda(hoja, styles.diaNumero(), ROW_DIA, col, (double) dia.getDayOfMonth());
            escribirCelda(hoja, styles.encabezadoColumna(), ROW_DIA_SEMANA, col, letraDia(dia.getDayOfWeek()));
        }

        String[] totalesTitulos = {"Tardanzas", "Faltas", "Faltas\nJustificadas", "Descansos\nMédicos"};
        for (int i = 0; i < totalesTitulos.length; i++) {
            int col = colTotalesInicio + i;
            mergeYEscribir(hoja, styles.totalEncabezado(), ROW_SEMANA, ROW_DIA_SEMANA, col, col, totalesTitulos[i]);
        }

        return ultimaColumna;
    }

    private void construirBandasSemana(XSSFSheet hoja, ExcelStyles styles, LocalDate primerDia, LocalDate ultimoDia) {
        int numeroSemana = 0;
        LocalDate inicioBanda = primerDia;
        for (LocalDate dia = primerDia; ; dia = dia.plusDays(1)) {
            boolean esFinDeMes = dia.isAfter(ultimoDia);
            boolean esInicioDeSemana = !esFinDeMes && dia.getDayOfWeek() == DayOfWeek.MONDAY && !dia.equals(primerDia);
            if (esInicioDeSemana || esFinDeMes) {
                numeroSemana++;
                int colInicio = COL_PRIMER_DIA + inicioBanda.getDayOfMonth() - 1;
                int colFin = COL_PRIMER_DIA + dia.minusDays(1).getDayOfMonth() - 1;
                mergeYEscribir(hoja, styles.bandaSemana(), ROW_SEMANA, ROW_SEMANA, colInicio, colFin, "SEM" + numeroSemana);
                inicioBanda = dia;
            }
            if (esFinDeMes) {
                break;
            }
        }
    }

    private void construirFilasOrientadores(XSSFSheet hoja, ExcelStyles styles, List<Orientador> orientadores,
                                             Map<Long, Map<LocalDate, Asistencia>> asistenciasPorOrientador,
                                             LocalDate primerDia, LocalDate ultimoDia) {
        int fila = ROW_DATOS_INICIO;
        int numDias = ultimoDia.getDayOfMonth();
        int colTotalesInicio = COL_PRIMER_DIA + numDias;

        for (int i = 0; i < orientadores.size(); i++) {
            Orientador orientador = orientadores.get(i);
            Map<LocalDate, Asistencia> celdas = asistenciasPorOrientador.getOrDefault(orientador.getId(), Map.of());

            escribirCelda(hoja, styles.numeroFila(), fila, COL_NUMERO, (double) (i + 1));
            String nombreCompleto = "%s %s".formatted(orientador.getApellidos(), orientador.getNombres()).toUpperCase(LOCALE_ES);
            escribirCelda(hoja, styles.nombreOrientador(), fila, COL_NOMBRE, nombreCompleto);

            long tardanzas = 0, faltas = 0, faltasJustificadas = 0, descansosMedicos = 0;
            for (LocalDate dia = primerDia; !dia.isAfter(ultimoDia); dia = dia.plusDays(1)) {
                int col = COL_PRIMER_DIA + dia.getDayOfMonth() - 1;
                Asistencia asistencia = celdas.get(dia);
                if (asistencia == null) {
                    escribirCelda(hoja, styles.celdaVacia(), fila, col, "");
                    continue;
                }
                String codigo = asistencia.getEstado().getCodigo();
                escribirCelda(hoja, styles.celdaEstado(asistencia.getEstado().getColorHex()), fila, col, codigo);
                switch (codigo) {
                    case "T" -> tardanzas++;
                    case "F" -> faltas++;
                    case "FJ" -> faltasJustificadas++;
                    case "DM" -> descansosMedicos++;
                    default -> { }
                }
            }

            escribirCelda(hoja, styles.totalValor(), fila, colTotalesInicio, (double) tardanzas);
            escribirCelda(hoja, styles.totalValor(), fila, colTotalesInicio + 1, (double) faltas);
            escribirCelda(hoja, styles.totalValor(), fila, colTotalesInicio + 2, (double) faltasJustificadas);
            escribirCelda(hoja, styles.totalValor(), fila, colTotalesInicio + 3, (double) descansosMedicos);

            fila++;
        }
    }

    private void construirLeyenda(XSSFSheet hoja, ExcelStyles styles, List<EstadoAsistencia> catalogoEstados, int filaInicio) {
        mergeYEscribir(hoja, styles.leyendaTitulo(), filaInicio, filaInicio, COL_NUMERO, COL_NOMBRE, "LEYENDA");
        int fila = filaInicio + 1;
        for (EstadoAsistencia estado : catalogoEstados) {
            XSSFCellStyle estilo = styles.celdaEstado(estado.getColorHex());
            escribirCelda(hoja, estilo, fila, COL_NUMERO, estado.getCodigo());
            escribirCelda(hoja, estilo, fila, COL_NOMBRE, "= " + estado.getNombre());
            fila++;
        }
    }

    private void ajustarColumnas(XSSFSheet hoja, int ultimaColumna, int numDias) {
        hoja.setColumnWidth(COL_NUMERO, 6 * 256);
        hoja.setColumnWidth(COL_NOMBRE, 32 * 256);
        for (int col = COL_PRIMER_DIA; col < COL_PRIMER_DIA + numDias; col++) {
            hoja.setColumnWidth(col, 4 * 256);
        }
        for (int col = COL_PRIMER_DIA + numDias; col <= ultimaColumna; col++) {
            hoja.setColumnWidth(col, 11 * 256);
        }
    }

    private String letraDia(DayOfWeek diaSemana) {
        return switch (diaSemana) {
            case MONDAY -> "L";
            case TUESDAY, WEDNESDAY -> "M";
            case THURSDAY -> "J";
            case FRIDAY -> "V";
            case SATURDAY -> "S";
            case SUNDAY -> "D";
        };
    }

    private void mergeYEscribir(XSSFSheet hoja, XSSFCellStyle style, int filaInicio, int filaFin,
                                int colInicio, int colFin, String valor) {
        if (filaInicio != filaFin || colInicio != colFin) {
            hoja.addMergedRegion(new CellRangeAddress(filaInicio, filaFin, colInicio, colFin));
        }
        for (int f = filaInicio; f <= filaFin; f++) {
            for (int c = colInicio; c <= colFin; c++) {
                escribirCelda(hoja, style, f, c, f == filaInicio && c == colInicio ? valor : null);
            }
        }
    }

    private void escribirCelda(XSSFSheet hoja, XSSFCellStyle style, int fila, int columna, String valor) {
        Cell celda = obtenerOCrearCelda(hoja, fila, columna);
        celda.setCellStyle(style);
        if (valor != null) {
            celda.setCellValue(valor);
        }
    }

    private void escribirCelda(XSSFSheet hoja, XSSFCellStyle style, int fila, int columna, double valor) {
        Cell celda = obtenerOCrearCelda(hoja, fila, columna);
        celda.setCellStyle(style);
        celda.setCellValue(valor);
    }

    private Cell obtenerOCrearCelda(XSSFSheet hoja, int fila, int columna) {
        Row row = hoja.getRow(fila);
        if (row == null) {
            row = hoja.createRow(fila);
        }
        Cell celda = row.getCell(columna);
        if (celda == null) {
            celda = row.createCell(columna);
        }
        return celda;
    }

    public record ExcelFile(String nombreArchivo, byte[] contenido) {
    }
}
