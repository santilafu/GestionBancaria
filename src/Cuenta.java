
import java.io.Serializable;
import java.util.ArrayList;

public class Cuenta implements Serializable {
    private static final long serialVersionUID = 1L;

    private String numeroCuenta;
    private String nombreCliente;
    private ArrayList<Movimiento> movimientos;

    public Cuenta(String numeroCuenta, String nombreCliente) {
        this.numeroCuenta = numeroCuenta;
        this.nombreCliente = nombreCliente;
        this.movimientos = new ArrayList<>();
    }

    // Getters y Setters
    public String getNumeroCuenta() { return numeroCuenta; }
    public String getNombreCliente() { return nombreCliente; }
    public ArrayList<Movimiento> getMovimientos() { return movimientos; }

    public void setMovimientos(ArrayList<Movimiento> movimientos) {
        this.movimientos = movimientos;
    }

    public void agregarMovimiento(Movimiento m) {
        this.movimientos.add(m);
    }

    public double getSaldo() {
        double saldo = 0;
        for (Movimiento m : movimientos) {
            // Aquí deberías implementar lógica de I (suma) y R (resta)
            // Para el ejemplo, sumamos todo o simplificamos
            // Asumimos que en 'Retirada' el importe ya viene en negativo o lo controlamos
            // (Simplificación para el ejercicio)
        }
        return 0.0; // Implementar según lógica deseada
    }
}