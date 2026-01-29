
import java.util.Scanner;

public class Principal {

    public static void main(String[] args) {
        GestorSeguridad seguridad = new GestorSeguridad();

        // SOLO PRIMERA VEZ: Descomentar para generar las claves DSA
        seguridad.generarEntornoFirmaDSA();

        System.out.println("🔒 Verificando Identidad con DSA...");
        if (!seguridad.verificarIdentidadDSA()) {
            System.out.println("⛔ ACCESO DENEGADO: Firma digital inválida.");
            return;
        }
        System.out.println("✅ Identidad verificada.");

        // Carga o crea cuenta
        Cuenta cuenta = seguridad.cargarCuenta();
        Scanner sc = new Scanner(System.in);

        if (cuenta == null) {
            System.out.println("No existen datos. Creando cuenta nueva...");
            System.out.print("Introduce Número de Cuenta: ");
            String num = sc.nextLine();
            System.out.print("Introduce Nombre Cliente: ");
            String nom = sc.nextLine();
            cuenta = new Cuenta(num, nom);
        } else {
            System.out.println("👋 Bienvenido de nuevo, " + cuenta.getNombreCliente());
        }

        boolean salir = false;
        while (!salir) {
            System.out.println("\n--- GESTIÓN BANCARIA ---");
            System.out.println("1. Ingresar Dinero");
            System.out.println("2. Retirar Dinero");
            System.out.println("3. Ver Movimientos");
            System.out.println("4. Guardar y Salir");
            System.out.print("Opcion: ");
            String op = sc.nextLine();

            switch (op) {
                case "1":
                    System.out.print("Concepto: "); String conI = sc.nextLine();
                    System.out.print("Cantidad: "); double cantI = Double.parseDouble(sc.nextLine());
                    cuenta.agregarMovimiento(new Movimiento(conI, cantI, 'I'));
                    break;
                case "2":
                    System.out.print("Concepto: "); String conR = sc.nextLine();
                    System.out.print("Cantidad: "); double cantR = Double.parseDouble(sc.nextLine());
                    cuenta.agregarMovimiento(new Movimiento(conR, cantR, 'R'));
                    break;
                case "3":
                    System.out.println("--- MOVIMIENTOS ---");
                    System.out.println("Cliente (Descifrado): " + cuenta.getNombreCliente());
                    System.out.println("Cuenta (Descifrado): " + cuenta.getNumeroCuenta());
                    for (Movimiento m : cuenta.getMovimientos()) {
                        System.out.println(m); // Se imprime sin descifrar porque no estaba cifrado
                    }
                    break;
                case "4":
                    seguridad.guardarCuenta(cuenta);
                    salir = true;
                    break;
            }
        }
        sc.close();
    }
}