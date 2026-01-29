package com.banca.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class ColoreadorMovimientos extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Obtenemos el valor de la columna "Tipo" (Índice 1)
        String tipo = (String) table.getModel().getValueAt(row, 1);

        // Si la fila está seleccionada, usamos colores de sistema para selección
        if (isSelected) {
            c.setForeground(table.getSelectionForeground());
            c.setBackground(table.getSelectionBackground());
        } else {
            c.setBackground(Color.WHITE); // Fondo blanco por defecto

            // LÓGICA DE COLORES
            if ("Ingreso".equals(tipo)) {
                c.setForeground(new Color(0, 128, 0)); // Verde Oscuro
            } else {
                c.setForeground(Color.RED); // Rojo para retiradas
            }
        }

        // Negrita para el importe (Columna 3)
        if (column == 3) {
            c.setFont(c.getFont().deriveFont(Font.BOLD));
            setHorizontalAlignment(JLabel.RIGHT); // Alinear dinero a la derecha
        } else {
            setHorizontalAlignment(JLabel.LEFT);
        }

        return c;
    }
}