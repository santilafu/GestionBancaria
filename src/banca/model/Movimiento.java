package banca.model;

import java.io.Serializable;
import java.util.Date;

public class Movimiento implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date fecha;
    private String concepto;
    private double importe;
    private char tipo; // 'I' = Ingreso, 'R' = Retirada

    public Movimiento(String concepto, double importe, char tipo) {
        this.fecha = new Date(); // Fecha actual
        this.concepto = concepto;
        this.importe = importe;
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return String.format("[%s] %c - %s: %.2f €", fecha, tipo, concepto, importe);
    }
}