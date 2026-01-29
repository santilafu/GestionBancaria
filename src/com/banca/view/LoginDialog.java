package com.banca.view;

import com.banca.model.Cuenta;
import com.banca.persistence.GestorDatos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginDialog extends JDialog {

    private JTextField txtNumCuenta;
    private GestorDatos gestorDatos;
    private java.util.List<Cuenta> listaCuentas;
    private boolean loginExitoso = false;

    public LoginDialog(Frame parent, GestorDatos gestor, java.util.List<Cuenta> cuentas) {
        super(parent, "Acceso Banca Segura", true); // true = modal
        this.gestorDatos = gestor;
        this.listaCuentas = cuentas;
        initComponents();
        setLocationRelativeTo(null); // Centrar en pantalla
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);

        // --- Panel Superior (Título) ---
        JLabel lblTitulo = new JLabel("Identificación de Cliente", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        contentPane.add(lblTitulo, BorderLayout.NORTH);

        // --- Panel Central (Formulario) ---
        JPanel panelForm = new JPanel(new GridLayout(2, 1, 5, 5));
        panelForm.add(new JLabel("Introduce tu Número de Cuenta:"));
        txtNumCuenta = new JTextField(20);
        txtNumCuenta.setFont(new Font("Monospaced", Font.PLAIN, 14));
        panelForm.add(txtNumCuenta);
        contentPane.add(panelForm, BorderLayout.CENTER);

        // --- Panel Inferior (Botones) ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton btnLogin = new JButton("Entrar");
        JButton btnRegistro = new JButton("Soy nuevo / Crear Cuenta");

        // Acción de Login
        btnLogin.addActionListener(e -> intentarLogin());
        // Acción en el campo de texto al pulsar Enter
        txtNumCuenta.addActionListener(e -> intentarLogin());

        // Acción de Registro
        btnRegistro.addActionListener(e -> abrirDialogoRegistro());

        panelBotones.add(btnLogin);
        panelBotones.add(btnRegistro);
        contentPane.add(panelBotones, BorderLayout.SOUTH);

        pack(); // Ajustar tamaño al contenido
        setResizable(false);
    }

    private void intentarLogin() {
        String numInput = txtNumCuenta.getText().trim();
        if (numInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, introduce un número de cuenta.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (Cuenta c : listaCuentas) {
            // Comparamos con el dato descifrado que tenemos en memoria
            if (c.getNumeroCuentaDescifrado().equals(numInput)) {
                abrirDashboard(c);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Cuenta no encontrada. Verifica el número o regístrate.", "Error de Acceso", JOptionPane.ERROR_MESSAGE);
    }

    private void abrirDialogoRegistro() {
        // Usamos diálogos nativos de Swing para pedir datos rápidamente
        String numInput = JOptionPane.showInputDialog(this, "Introduce el NUEVO Número de Cuenta:");
        if (numInput == null || numInput.trim().isEmpty()) return;

        // Verificar duplicados
        for(Cuenta c : listaCuentas) {
            if(c.getNumeroCuentaDescifrado().equals(numInput)) {
                JOptionPane.showMessageDialog(this, "Este número de cuenta ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String nomInput = JOptionPane.showInputDialog(this, "Introduce tu Nombre Completo:");
        if (nomInput == null || nomInput.trim().isEmpty()) return;

        try {
            // --- CIFRADO AES AL REGISTRAR ---
            byte[] numCiph = gestorDatos.getServicioSeguridad().cifrar(numInput, gestorDatos.getClaveAES());
            byte[] nomCiph = gestorDatos.getServicioSeguridad().cifrar(nomInput, gestorDatos.getClaveAES());

            Cuenta nuevaCuenta = new Cuenta(numCiph, nomCiph, numInput, nomInput);
            listaCuentas.add(nuevaCuenta);
            // Guardamos inmediatamente
            gestorDatos.guardarCuentas(listaCuentas);

            JOptionPane.showMessageDialog(this, "¡Cuenta creada con éxito! Accediendo...", "Bienvenido", JOptionPane.INFORMATION_MESSAGE);
            abrirDashboard(nuevaCuenta);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al crear cuenta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirDashboard(Cuenta cuentaActiva) {
        this.loginExitoso = true;
        this.dispose(); // Cerramos ventana de login
        // Abrimos la ventana principal
        new DashboardFrame(cuentaActiva, gestorDatos, listaCuentas).setVisible(true);
    }
}