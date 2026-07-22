package com.atu.asistencias.asistencia;

/**
 * Acumula conteos de estados de asistencia para un grupo (zona, supervisor,
 * orientador...). La logica de que codigo cuenta como que columna es la misma
 * que usa la grilla y la exportacion a Excel.
 */
public class ContadorEstados {

    private long asistio;
    private long tardanzas;
    private long faltas;
    private long faltasJustificadas;
    private long descansosMedicos;
    private long total;

    public void contar(String codigo) {
        total++;
        switch (codigo) {
            case "A" -> asistio++;
            case "T" -> tardanzas++;
            case "F" -> faltas++;
            case "FJ" -> faltasJustificadas++;
            case "DM" -> descansosMedicos++;
            default -> { }
        }
    }

    public long getAsistio() {
        return asistio;
    }

    public long getTardanzas() {
        return tardanzas;
    }

    public long getFaltas() {
        return faltas;
    }

    public long getFaltasJustificadas() {
        return faltasJustificadas;
    }

    public long getDescansosMedicos() {
        return descansosMedicos;
    }

    public long getTotal() {
        return total;
    }

    public double getPorcentajeAsistencia() {
        return total == 0 ? 0.0 : Math.round((asistio * 10000.0) / total) / 100.0;
    }
}
