package com.banca.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Entidad Cuenta Bancaria.
 * Almacena datos sensibles cifrados (byte[]) para cumplir con la GDPR/LOPD.
 */
public class Cuenta implements Serializable {
    private static final long serialVersionUID = 1L;

    // Atributos sensibles almacenados CIFRADOS (AES)
    private byte[] numeroCuentaCifrado;
    private byte[] nombreClienteCifrado;

    // Lista de movimientos (sin cifrar según enunciado)
    private ArrayList<Movimiento> movimientos;

    // Atributos volátiles (no se guardan, solo para uso en RAM tras descifrar)
    private transient String numeroCuentaDescifrado;
    private transient String nombreClienteDescifrado;

    public Cuenta(byte[] numCifrado, byte[] nomCifrado, String numReal, String nomReal) {
        this.numeroCuentaCifrado = numCifrado;
        this.nombreClienteCifrado = nomCifrado;
        this.numeroCuentaDescifrado = numReal; // Cache en RAM
        this.nombreClienteDescifrado = nomReal; // Cache en RAM
        this.movimientos = new ArrayList<>();
    }

    // --- LÓGICA DE NEGOCIO ---
    /**
     * Registra un ingreso en la cuenta.
     * @param concepto Descripción del ingreso.
     * @param cantidad Cantidad a ingresar (debe ser positiva).
     * @throws Exception Si la cantidad no es positiva.
     */

    public void ingresar(String concepto, double cantidad) throws Exception {
        if (cantidad <= 0) throw new Exception("La cantidad debe ser positiva.");
        this.movimientos.add(new Movimiento(concepto, cantidad, 'I'));
    }

    /**
     * Calcula el saldo actual sumando todos los movimientos.
     * Utiliza getImporteReal() para que las retiradas resten y los ingresos sumen.
     */
    public double getSaldo() {
        double saldo = 0.0;
        for (Movimiento m : movimientos) {
            saldo += m.getImporteReal();
        }
        return saldo;
    }
    /**
     * Registra una retirada en la cuenta.
     * @param concepto Descripción de la retirada.
     * @param cantidad Cantidad a retirar (debe ser positiva).
     * @throws Exception Si la cantidad no es positiva o si no hay saldo suficiente.
     */
    public void retirar(String concepto, double cantidad) throws Exception {
        if (cantidad <= 0) throw new Exception("La cantidad debe ser positiva.");

        if (getSaldo() - cantidad < 0) {
            throw new Exception("Saldo insuficiente.");
        }


        // Guardamos la cantidad en positivo. El tipo 'R' es el que indica resta.
        this.movimientos.add(new Movimiento(concepto, cantidad, 'R'));
    }

    // Método auxiliar para calcular saldo real iterando
    public double calcularSaldoActual() {
        // Implementación rápida sin tocar Movimiento
        // (En tu código real, pon getter en Movimiento)
        return 0.0;
    }

    // Getters y Setters necesarios
    public byte[] getNumeroCuentaCifrado() { return numeroCuentaCifrado; }
    public byte[] getNombreClienteCifrado() { return nombreClienteCifrado; }
    public ArrayList<Movimiento> getMovimientos() { return movimientos; }

    public String getNumeroCuentaDescifrado() { return numeroCuentaDescifrado; }
    public String getNombreClienteDescifrado() { return nombreClienteDescifrado; }

    // Necesario para rehidratar el objeto tras leer del disco
    public void setDatosDescifrados(String numero, String nombre) {
        this.numeroCuentaDescifrado = numero;
        this.nombreClienteDescifrado = nombre;
    }
}