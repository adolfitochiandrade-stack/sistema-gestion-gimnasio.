package Modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class FiltroFecha {

    private static final DateTimeFormatter FORMATO_VISUAL = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LocalDate desde;
    private LocalDate hasta;

    public FiltroFecha() {
    }

    public FiltroFecha(LocalDate desde, LocalDate hasta) {
        this.desde = desde;
        this.hasta = hasta;
    }

    public static FiltroFecha todo() {
        return new FiltroFecha(null, null);
    }

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }

    // Sin fechas seleccionadas = sin filtro = se exporta todo
    public boolean esTodo() {
        return desde == null && hasta == null;
    }

    // Texto amigable para mostrar debajo del cuadrito del módulo
    public String textoEstado() {
        if (esTodo()) {
            return "Mostrando: todos los registros";
        }
        String desdeTexto = desde != null ? desde.format(FORMATO_VISUAL) : "el inicio";
        String hastaTexto = hasta != null ? hasta.format(FORMATO_VISUAL) : "hoy";
        return "Del " + desdeTexto + " al " + hastaTexto;
    }
}