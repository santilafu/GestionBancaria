package com.banca.view;

import com.banca.model.Cuenta;
import com.banca.model.Movimiento;
import com.banca.persistence.GestorDatos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

public class DashboardFrame extends JFrame {

    private Cuenta cuentaActiva;
    private GestorDatos gestorDatos;
    private java.util.List<Cuenta> listaGlobalCuentas;

    private JLabel lblSaldoValor;
    private JTable tablaMovimientos;
    private DefaultTableModel modeloTabla;

    public DashboardFrame(Cuenta cuenta, GestorDatos gestor, java.util.List<Cuenta> listaGlobal) {
        this.cuentaActiva = cuenta;
        this.gestorDatos = gestor;
        this.listaGlobalCuentas = listaGlobal;

        setTitle("Banca Segura - " + cuentaActiva.getNombreClienteDescifrado());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 600); // Un poco más grande
        setLocationRelativeTo(null);

        initComponents();
        actualizarDatosUI();

        // Listener para la X de la ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                guardarYSalir();
            }
        });
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);

        // --- NORTE: Panel Header Estilizado ---
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBackground(new Color(245, 245, 250)); // Gris muy claro
        panelNorte.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(""), new EmptyBorder(15, 15, 15, 15)));

        JLabel lblBienvenida = new JLabel("Hola, " + cuentaActiva.getNombreClienteDescifrado());
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel lblCuentaInfo = new JLabel("IBAN: " + cuentaActiva.getNumeroCuentaDescifrado());
        lblCuentaInfo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        lblCuentaInfo.setForeground(Color.GRAY);

        JPanel panelInfoUser = new JPanel(new GridLayout(2, 1));
        panelInfoUser.setOpaque(false);
        panelInfoUser.add(lblBienvenida);
        panelInfoUser.add(lblCuentaInfo);

        // Saldo
        JPanel panelSaldo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSaldo.setOpaque(false);
        JLabel lblSaldoTitulo = new JLabel("Saldo Disponible: ");
        lblSaldoTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        lblSaldoValor = new JLabel("...");
        lblSaldoValor.setFont(new Font("Segoe UI", Font.BOLD, 26));

        panelSaldo.add(lblSaldoTitulo);
        panelSaldo.add(lblSaldoValor);

        panelNorte.add(panelInfoUser, BorderLayout.WEST);
        panelNorte.add(panelSaldo, BorderLayout.EAST);
        contentPane.add(panelNorte, BorderLayout.NORTH);

        // --- CENTRO: Tabla Mejorada ---
        String[] columnas = {"Fecha", "Tipo", "Concepto", "Importe"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaMovimientos = new JTable(modeloTabla);
        tablaMovimientos.setRowHeight(30);
        tablaMovimientos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaMovimientos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // APLICAMOS EL COLOREADOR A TODAS LAS COLUMNAS
        ColoreadorMovimientos coloreador = new ColoreadorMovimientos();
        for (int i = 0; i < tablaMovimientos.getColumnCount(); i++) {
            tablaMovimientos.getColumnModel().getColumn(i).setCellRenderer(coloreador);
        }

        JScrollPane scrollPane = new JScrollPane(tablaMovimientos);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // --- SUR: Botonera ---
        JPanel panelSur = new JPanel(new GridLayout(1, 4, 15, 0)); // Grid para botones iguales

        JButton btnIngresar = crearBoton("📥 Ingresar", new Color(220, 255, 220));
        JButton btnRetirar = crearBoton("📤 Retirar", new Color(255, 220, 220));
        JButton btnTransferir = crearBoton("💸 Transferir", new Color(220, 240, 255)); // NUEVO
        JButton btnLogout = crearBoton("🔒 Cerrar Sesión", new Color(240, 240, 240)); // NUEVO

        btnIngresar.addActionListener(e -> realizarOperacion('I'));
        btnRetirar.addActionListener(e -> realizarOperacion('R'));
        btnTransferir.addActionListener(e -> realizarTransferencia());
        btnLogout.addActionListener(e -> cerrarSesion());

        panelSur.add(btnIngresar);
        panelSur.add(btnRetirar);
        panelSur.add(btnTransferir);
        panelSur.add(btnLogout);
        contentPane.add(panelSur, BorderLayout.SOUTH);
    }

    private JButton crearBoton(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- LÓGICA ---

    private void actualizarDatosUI() {
        double saldo = 0;
        for (Movimiento m : cuentaActiva.getMovimientos()) {
            saldo += m.getImporteReal();
        }
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        lblSaldoValor.setText(nf.format(saldo));
        lblSaldoValor.setForeground(saldo >= 0 ? new Color(0, 100, 0) : Color.RED);

        modeloTabla.setRowCount(0);
        // Mostrar movimientos en orden inverso (más recientes arriba)
        java.util.List<Movimiento> movs = cuentaActiva.getMovimientos();
        for (int i = movs.size() - 1; i >= 0; i--) {
            modeloTabla.addRow(movs.get(i).toVectorFila());
        }
    }

    private void realizarOperacion(char tipo) {
        // (Igual que en tu código anterior, sin cambios)
        String tipoStr = (tipo == 'I') ? "Ingreso" : "Retirada";
        JPanel panelInput = new JPanel(new GridLayout(2, 2, 5, 10));
        JTextField txtConcepto = new JTextField();
        JTextField txtImporte = new JTextField();
        panelInput.add(new JLabel("Concepto:")); panelInput.add(txtConcepto);
        panelInput.add(new JLabel("Importe (€):")); panelInput.add(txtImporte);

        int result = JOptionPane.showConfirmDialog(this, panelInput, "Realizar " + tipoStr, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String concepto = txtConcepto.getText();
                double importe = Double.parseDouble(txtImporte.getText().replace(",", "."));
                if (tipo == 'I') cuentaActiva.ingresar(concepto, importe);
                else cuentaActiva.retirar(concepto, importe);
                actualizarDatosUI();
                gestorDatos.guardarCuentas(listaGlobalCuentas); // Auto-guardado
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- NUEVA LÓGICA: TRANSFERENCIAS ---
    private void realizarTransferencia() {
        JPanel panelTrans = new JPanel(new GridLayout(3, 2, 5, 10));
        JTextField txtDestino = new JTextField();
        JTextField txtImporte = new JTextField();
        JTextField txtConcepto = new JTextField("Transferencia");

        panelTrans.add(new JLabel("Cuenta Destino:")); panelTrans.add(txtDestino);
        panelTrans.add(new JLabel("Importe (€):")); panelTrans.add(txtImporte);
        panelTrans.add(new JLabel("Concepto:")); panelTrans.add(txtConcepto);

        int res = JOptionPane.showConfirmDialog(this, panelTrans, "Realizar Transferencia", JOptionPane.OK_CANCEL_OPTION);

        if (res == JOptionPane.OK_OPTION) {
            try {
                String cuentaDestinoStr = txtDestino.getText().trim();
                double importe = Double.parseDouble(txtImporte.getText().replace(",", "."));
                String concepto = txtConcepto.getText();

                if(cuentaDestinoStr.equals(cuentaActiva.getNumeroCuentaDescifrado())) {
                    throw new Exception("No puedes transferirte dinero a ti mismo.");
                }

                // 1. Buscar Cuenta Destino
                Cuenta cuentaDestino = null;
                for(Cuenta c : listaGlobalCuentas) {
                    if(c.getNumeroCuentaDescifrado().equals(cuentaDestinoStr)) {
                        cuentaDestino = c;
                        break;
                    }
                }

                if(cuentaDestino == null) {
                    throw new Exception("La cuenta de destino no existe.");
                }

                // 2. Ejecutar Operación Atómica (Retirar de aquí, Ingresar allí)
                // Primero intentamos retirar (si falla por saldo, salta excepción y no se ingresa)
                cuentaActiva.retirar("Transf. a " + cuentaDestino.getNombreClienteDescifrado() + ": " + concepto, importe);
                cuentaDestino.ingresar("Transf. de " + cuentaActiva.getNombreClienteDescifrado() + ": " + concepto, importe);

                // 3. Guardar y Actualizar
                gestorDatos.guardarCuentas(listaGlobalCuentas);
                actualizarDatosUI();
                JOptionPane.showMessageDialog(this, "Transferencia realizada con éxito.");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Fallo Transferencia", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cerrarSesion() {
        // Guardamos antes de salir
        try {
            gestorDatos.guardarCuentas(listaGlobalCuentas);
        } catch (Exception e) { e.printStackTrace(); }

        this.dispose(); // Cierra esta ventana
        // Abre de nuevo el login
        new LoginDialog(null, gestorDatos, listaGlobalCuentas).setVisible(true);
    }

    private void guardarYSalir() {
        try {
            gestorDatos.guardarCuentas(listaGlobalCuentas);
            System.exit(0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage());
        }
    }
}