package com.atu.asistencias.reporte;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Exportador generico de tablas de reporte (titulo + encabezados + filas de
 * texto ya formateado). A diferencia de la planilla de asistencias, estos
 * reportes son tabulares y no necesitan bandas de semana ni colores por
 * estado, asi que no reutiliza ExcelStyles.
 */
@Component
public class ReporteExcelExporter {

    public byte[] exportar(String titulo, List<String> encabezados, List<List<String>> filas) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet hoja = workbook.createSheet("Reporte");

            XSSFFont fontTitulo = workbook.createFont();
            fontTitulo.setBold(true);
            fontTitulo.setFontHeightInPoints((short) 13);
            XSSFCellStyle estiloTitulo = workbook.createCellStyle();
            estiloTitulo.setFont(fontTitulo);

            XSSFFont fontHeader = workbook.createFont();
            fontHeader.setBold(true);
            XSSFCellStyle estiloHeader = celdaConBorde(workbook);
            estiloHeader.setFont(fontHeader);
            estiloHeader.setFillForegroundColor(new XSSFColor(colorDe("#EDEDED"), null));
            estiloHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloHeader.setAlignment(HorizontalAlignment.CENTER);

            XSSFCellStyle estiloCelda = celdaConBorde(workbook);

            int ultimaColumna = Math.max(0, encabezados.size() - 1);

            XSSFRow filaTitulo = hoja.createRow(0);
            var celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue(titulo);
            celdaTitulo.setCellStyle(estiloTitulo);
            if (ultimaColumna > 0) {
                hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, ultimaColumna));
            }

            XSSFRow filaHeader = hoja.createRow(2);
            for (int i = 0; i < encabezados.size(); i++) {
                var celda = filaHeader.createCell(i);
                celda.setCellValue(encabezados.get(i));
                celda.setCellStyle(estiloHeader);
            }

            int numeroFila = 3;
            for (List<String> fila : filas) {
                XSSFRow row = hoja.createRow(numeroFila++);
                for (int i = 0; i < fila.size(); i++) {
                    var celda = row.createCell(i);
                    celda.setCellValue(fila.get(i));
                    celda.setCellStyle(estiloCelda);
                }
            }

            for (int i = 0; i < encabezados.size(); i++) {
                hoja.setColumnWidth(i, (i == 0 ? 28 : 16) * 256);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel del reporte", e);
        }
    }

    private XSSFCellStyle celdaConBorde(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private byte[] colorDe(String hex) {
        Color c = Color.decode(hex);
        return new byte[]{(byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue()};
    }
}
