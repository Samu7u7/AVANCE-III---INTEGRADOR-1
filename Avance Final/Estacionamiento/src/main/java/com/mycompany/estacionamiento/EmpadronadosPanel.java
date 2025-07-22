package com.mycompany.estacionamiento;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class EmpadronadosPanel extends JPanel {
    private DefaultTableModel modelo;
    private JTable tabla;
    private JLabel lblTotal;

    public EmpadronadosPanel() {
        initComponents();
        cargarDatos();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header con gradiente
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(229, 216, 8),
                        getWidth(), getHeight(), new Color(213, 202, 37));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        header.setPreferredSize(new Dimension(0, 50));
        header.setLayout(new BorderLayout());
        header.setOpaque(false);

        JLabel titulo = new JLabel("Vehiculos Empadronados", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.CENTER);

        lblTotal = new JLabel("Total: 0", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotal.setForeground(Color.WHITE);
        lblTotal.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
        header.add(lblTotal, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"Placa", "Nombre"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tabla.setRowHeight(22);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabla.getTableHeader().setBackground(new Color(229, 216, 8));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionBackground(new Color(229, 216, 8, 100));
        tabla.setGridColor(new Color(200, 200, 200));

        // Ajustar ancho de columnas
        tabla.getColumnModel().getColumn(0).setPreferredWidth(80); // Placa
        tabla.getColumnModel().getColumn(1).setPreferredWidth(120); // Nombre

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(229, 216, 8), 2));
        scroll.setPreferredSize(new Dimension(300, 200));

        add(scroll, BorderLayout.CENTER);

        // Footer con botón de refrescar
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefrescar.setBackground(new Color(234, 207, 16));
        btnRefrescar.setForeground(Color.WHITE);
        btnRefrescar.setFocusPainted(false);
        btnRefrescar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnRefrescar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefrescar.setToolTipText("Refrescar lista");
        btnRefrescar.addActionListener(e -> cargarDatos());

        footer.add(btnRefrescar);
        add(footer, BorderLayout.SOUTH);
    }

    public void cargarDatos() {
        modelo.setRowCount(0);
        int total = 0;

        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT placa, nombre FROM empadronados ORDER BY nombre")) {

            while (rs.next()) {
                Object[] fila = {
                    rs.getString("placa"),
                    rs.getString("nombre")
                };
                modelo.addRow(fila);
                total++;
            }

            // Mostrar total con indicador de límite
            String textoTotal = "Total: " + total + "/5";
            if (total >= 5) {
            }
            lblTotal.setText(textoTotal);

        } catch (SQLException e) {
            System.out.println("[ERROR] Error al cargar empadronados: " + e.getMessage());
            lblTotal.setText("Total: Error");
        }
    }

    // *** MÉTODO PARA VALIDAR LÍMITE ***
    public static boolean validarLimiteEmpadronados(Component parent) {
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM empadronados")) {
            
            if (rs.next()) {
                int total = rs.getInt("total");
                if (total >= 5) {
                    DialogUtils.showErrorDialog(parent,
                        "Solo se permiten máximo 5 empadronados (E-06 a E-10).");
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            DialogUtils.showErrorDialog(parent, "Error al verificar límite: " + e.getMessage());
            return false;
        }
    }

    // *** MÉTODO PARA CONTAR EMPADRONADOS ***
    public static int contarEmpadronados() {
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM empadronados")) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("[ERROR] Error al contar empadronados: " + e.getMessage());
        }
        return 0;
    }
}