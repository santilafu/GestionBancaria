package com.banca.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Representa una transacción bancaria.
 * Implementa Serializable para poder guardarse en fichero.
 */
public class Movimiento implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date fecha;
    private String concepto;
    private double importe;
    private char tipo; // 'I' = Ingreso, 'R' = Retirada

    public Movimiento(String concepto, double importe, char tipo) {
        this.fecha = new Date();
        this.concepto = concepto;
        this.importe = importe;
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String tipoStr = (tipo == 'I') ? "INGRESO " : "RETIRADA";
        return String.format("| %-16s | %-10s | %-20s | %10.2f € |",
                sdf.format(fecha), tipoStr, concepto, importe);
    }
    /** Devuelve el importe con signo negativo si es retirada. Útil para calcular saldo. */
    public double getImporteReal() {
        return (this.tipo == 'R' ? -this.importe : this.importe);
    }

    /** Devuelve los datos como un array para rellenar una fila de JTable fácilmente. */
    public Object[] toVectorFila() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String tipoStr = (tipo == 'I') ? "Ingreso" : "Retirada";
        // Formateamos el importe para que se vea bonito
        String importeStr = String.format("%,.2f €", getImporteReal());
        return new Object[]{sdf.format(fecha), tipoStr, concepto, importeStr};
    }
}