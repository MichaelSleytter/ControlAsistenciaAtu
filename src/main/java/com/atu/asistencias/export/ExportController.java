package com.atu.asistencias.export;

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

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ExcelExportService excelExportService;

    @GetMapping("/asistencias.xlsx")
    public ResponseEntity<byte[]> exportarAsistenciasExcel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Long zonaId,
            @RequestParam int anio,
            @RequestParam int mes) {
        ExcelExportService.ExcelFile archivo = excelExportService.exportarPlanillaMensual(
                principal.toAuthenticatedUser(), zonaId, anio, mes);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(archivo.nombreArchivo())
                .build();

        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(archivo.contenido());
    }
}
