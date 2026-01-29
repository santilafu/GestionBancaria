package com.banca.view;

import com.banca.model.Cuenta;
import com.banca.persistence.GestorDatos;

import javax.swing.*;
import java.util.List;

public class BankingAppLauncher {

    public static void main(String[] args) {
        // Usamos el hilo de eventos de Swing para que sea thread-safe
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Configurar Look and Feel nativo del sistema operativo (se ve más moderno)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // 2. Inicializar gestores
                GestorDatos gestor = new GestorDatos();

                // 3. VERIFICACIÓN FIRMA DIGITAL (LOGIN SEGURO)
                // Si falla, el propio gestor lanza excepción y lo capturamos abajo.
                gestor.getServicioSeguridad().verificarFirmaLogin();
                // Si pasa, mostramos mensaje de éxito temporal
                JOptionPane.showMessageDialog(null, "✅ Firma digital verificada correctamente.", "Seguridad", JOptionPane.INFORMATION_MESSAGE);

                // 4. Cargar datos
                List<Cuenta> cuentas = gestor.cargarCuentas();

                // 5. Lanzar la ventana de Login
                LoginDialog loginView = new LoginDialog(null, gestor, cuentas);
                loginView.setVisible(true);

                // El flujo continúa cuando el diálogo se cierra.
                // Si el login fue exitoso, el propio diálogo abre el Dashboard.

            } catch (SecurityException se) {
                // Error específico de firma digital
                JOptionPane.showMessageDialog(null, "⛔ ACCESO DENEGADO\n" + se.getMessage(), "Error de Seguridad Crítico", JOptionPane.ERROR_MESSAGE);
                System.exit(403);
            } catch (Exception e) {
                // Otros errores generales
                JOptionPane.showMessageDialog(null, "❌ Error fatal en la aplicación:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}