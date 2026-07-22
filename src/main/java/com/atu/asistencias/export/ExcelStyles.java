package com.atu.asistencias.export;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Fabrica de estilos para la planilla exportada. Los estilos de estado se cachean
 * por color (una entrada por codigo del catalogo) para no agotar el limite de
 * ~64k estilos por libro que impone el formato xlsx.
 */
class ExcelStyles {

    private final XSSFWorkbook workbook;
    private final Map<String, XSSFCellStyle> estadoStyleCache = new HashMap<>();
    private final XSSFFont fontBold;
    private final XSSFFont fontRegular;

    ExcelStyles(XSSFWorkbook workbook) {
        this.workbook = workbook;
        this.fontBold = workbook.createFont();
        this.fontBold.setBold(true);
        this.fontRegular = workbook.createFont();
    }

    XSSFCellStyle titulo() {
        XSSFCellStyle style = base();
        style.setFillForegroundColor(toXssfColor("#BDD7EE"));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    XSSFCellStyle etiqueta() {
        XSSFCellStyle style = base();
        style.setFont(fontBold);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    XSSFCellStyle valor() {
        XSSFCellStyle style = base();
        style.setFont(fontRegular);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    XSSFCellStyle bandaSemana() {
        XSSFCellStyle style = base();
        style.setFillForegroundColor(toXssfColor("#D9E1F2"));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(fontBold);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    XSSFCellStyle diaNumero() {
        XSSFCellStyle style = base();
        style.setFont(fontBold);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    XSSFCellStyle encabezadoColumna() {
        XSSFCellStyle style = base();
        style.setFillForegroundColor(toXssfColor("#EDEDED"));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(fontBold);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    XSSFCellStyle nombreOrientador() {
        XSSFCellStyle style = base();
        style.setFont(fontRegular);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    XSSFCellStyle numeroFila() {
        XSSFCellStyle style = base();
        style.setFont(fontRegular);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    XSSFCellStyle totalEncabezado() {
        XSSFCellStyle style = base();
        style.setFillForegroundColor(toXssfColor("#EDEDED"));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(fontBold);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    XSSFCellStyle totalValor() {
        XSSFCellStyle style = base();
        style.setFont(fontRegular);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    XSSFCellStyle leyendaTitulo() {
        XSSFCellStyle style = base();
        style.setFillForegroundColor(toXssfColor("#FFFF00"));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(fontBold);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    XSSFCellStyle celdaVacia() {
        return base();
    }

    XSSFCellStyle celdaEstado(String colorHex) {
        return estadoStyleCache.computeIfAbsent(colorHex, hex -> {
            XSSFCellStyle style = base();
            style.setFillForegroundColor(toXssfColor(hex));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFont(fontBold);
            style.setAlignment(HorizontalAlignment.CENTER);
            return style;
        });
    }

    private XSSFCellStyle base() {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private XSSFColor toXssfColor(String hex) {
        Color c = Color.decode(hex);
        return new XSSFColor(new byte[]{(byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue()}, null);
    }
}
