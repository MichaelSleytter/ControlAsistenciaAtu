package com.atu.asistencias.reporte;

import com.atu.asistencias.reporte.dto.RankingOrientadorResponse;
import com.atu.asistencias.reporte.dto.ReporteSupervisorResponse;
import com.atu.asistencias.reporte.dto.ReporteTurnoResponse;
import com.atu.asistencias.reporte.dto.ReporteZonaResponse;
import com.atu.asistencias.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ReporteService reporteService;
    private final ReporteExcelExporter excelExporter;

    @GetMapping("/zonas")
    public List<ReporteZonaResponse> porZona(@AuthenticationPrincipal CustomUserDetails principal,
                                              @RequestParam(required = false) Long zonaId,
                                              @RequestParam int anio, @RequestParam int mes) {
        return reporteService.reportePorZona(principal.toAuthenticatedUser(), zonaId, anio, mes);
    }

    @GetMapping("/zonas/export.xlsx")
    public ResponseEntity<byte[]> porZonaExcel(@AuthenticationPrincipal CustomUserDetails principal,
                                                @RequestParam(required = false) Long zonaId,
                                                @RequestParam int anio, @RequestParam int mes) {
        List<ReporteZonaResponse> filas = reporteService.reportePorZona(principal.toAuthenticatedUser(), zonaId, anio, mes);
        List<String> encabezados = List.of("Zona", "Orientadores", "Asistencias", "Tardanzas", "Faltas",
                "Faltas Justificadas", "Descansos Médicos", "% Asistencia");
        List<List<String>> filasTexto = new ArrayList<>();
        for (ReporteZonaResponse r : filas) {
            filasTexto.add(List.of(r.zonaNombre(), String.valueOf(r.totalOrientadores()), String.valueOf(r.asistencias()),
                    String.valueOf(r.tardanzas()), String.valueOf(r.faltas()), String.valueOf(r.faltasJustificadas()),
                    String.valueOf(r.descansosMedicos()), r.porcentajeAsistencia() + "%"));
        }
        byte[] archivo = excelExporter.exportar("Reporte por Zona - %d/%d".formatted(mes, anio), encabezados, filasTexto);
        return archivoExcel(archivo, "reporte-zonas-%d-%02d.xlsx".formatted(anio, mes));
    }

    @GetMapping("/supervisores")
    public List<ReporteSupervisorResponse> porSupervisor(@AuthenticationPrincipal CustomUserDetails principal,
                                                          @RequestParam(required = false) Long zonaId,
                                                          @RequestParam int anio, @RequestParam int mes) {
        return reporteService.reportePorSupervisor(principal.toAuthenticatedUser(), zonaId, anio, mes);
    }

    @GetMapping("/supervisores/export.xlsx")
    public ResponseEntity<byte[]> porSupervisorExcel(@AuthenticationPrincipal CustomUserDetails principal,
                                                      @RequestParam(required = false) Long zonaId,
                                                      @RequestParam int anio, @RequestParam int mes) {
        List<ReporteSupervisorResponse> filas = reporteService.reportePorSupervisor(principal.toAuthenticatedUser(), zonaId, anio, mes);
        List<String> encabezados = List.of("Supervisor", "Orientadores", "Asistencias", "Tardanzas", "Faltas",
                "Faltas Justificadas", "Descansos Médicos", "% Asistencia");
        List<List<String>> filasTexto = new ArrayList<>();
        for (ReporteSupervisorResponse r : filas) {
            filasTexto.add(List.of(r.supervisorNombre(), String.valueOf(r.totalOrientadores()), String.valueOf(r.asistencias()),
                    String.valueOf(r.tardanzas()), String.valueOf(r.faltas()), String.valueOf(r.faltasJustificadas()),
                    String.valueOf(r.descansosMedicos()), r.porcentajeAsistencia() + "%"));
        }
        byte[] archivo = excelExporter.exportar("Reporte por Supervisor - %d/%d".formatted(mes, anio), encabezados, filasTexto);
        return archivoExcel(archivo, "reporte-supervisores-%d-%02d.xlsx".formatted(anio, mes));
    }

    @GetMapping("/turnos")
    public List<ReporteTurnoResponse> porTurno(@AuthenticationPrincipal CustomUserDetails principal,
                                                @RequestParam(required = false) Long zonaId,
                                                @RequestParam int anio, @RequestParam int mes) {
        return reporteService.reportePorTurno(principal.toAuthenticatedUser(), zonaId, anio, mes);
    }

    @GetMapping("/turnos/export.xlsx")
    public ResponseEntity<byte[]> porTurnoExcel(@AuthenticationPrincipal CustomUserDetails principal,
                                                 @RequestParam(required = false) Long zonaId,
                                                 @RequestParam int anio, @RequestParam int mes) {
        List<ReporteTurnoResponse> filas = reporteService.reportePorTurno(principal.toAuthenticatedUser(), zonaId, anio, mes);
        List<String> encabezados = List.of("Turno", "Orientadores", "Asistencias", "Tardanzas", "Faltas",
                "Faltas Justificadas", "Descansos Médicos", "% Asistencia");
        List<List<String>> filasTexto = new ArrayList<>();
        for (ReporteTurnoResponse r : filas) {
            filasTexto.add(List.of(r.turnoNombre(), String.valueOf(r.totalOrientadores()), String.valueOf(r.asistencias()),
                    String.valueOf(r.tardanzas()), String.valueOf(r.faltas()), String.valueOf(r.faltasJustificadas()),
                    String.valueOf(r.descansosMedicos()), r.porcentajeAsistencia() + "%"));
        }
        byte[] archivo = excelExporter.exportar("Reporte por Turno - %d/%d".formatted(mes, anio), encabezados, filasTexto);
        return archivoExcel(archivo, "reporte-turnos-%d-%02d.xlsx".formatted(anio, mes));
    }

    @GetMapping("/ranking")
    public List<RankingOrientadorResponse> ranking(@AuthenticationPrincipal CustomUserDetails principal,
                                                    @RequestParam(required = false) Long zonaId,
                                                    @RequestParam int anio, @RequestParam int mes,
                                                    @RequestParam String criterio,
                                                    @RequestParam(defaultValue = "10") int limite) {
        return reporteService.ranking(principal.toAuthenticatedUser(), zonaId, anio, mes, criterio, limite);
    }

    @GetMapping("/ranking/export.xlsx")
    public ResponseEntity<byte[]> rankingExcel(@AuthenticationPrincipal CustomUserDetails principal,
                                                @RequestParam(required = false) Long zonaId,
                                                @RequestParam int anio, @RequestParam int mes,
                                                @RequestParam String criterio,
                                                @RequestParam(defaultValue = "10") int limite) {
        List<RankingOrientadorResponse> filas = reporteService.ranking(principal.toAuthenticatedUser(), zonaId, anio, mes, criterio, limite);
        List<String> encabezados = List.of("Orientador/a", "Zona", "Estado", "Cantidad");
        List<List<String>> filasTexto = new ArrayList<>();
        for (RankingOrientadorResponse r : filas) {
            filasTexto.add(List.of(r.nombreCompleto(), r.zonaNombre(), r.estadoCodigo(), String.valueOf(r.cantidad())));
        }
        byte[] archivo = excelExporter.exportar("Ranking (%s) - %d/%d".formatted(criterio.toUpperCase(), mes, anio), encabezados, filasTexto);
        return archivoExcel(archivo, "ranking-%s-%d-%02d.xlsx".formatted(criterio.toLowerCase(), anio, mes));
    }

    private ResponseEntity<byte[]> archivoExcel(byte[] contenido, String nombreArchivo) {
        ContentDisposition disposition = ContentDisposition.attachment().filename(nombreArchivo).build();
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(contenido);
    }
}
