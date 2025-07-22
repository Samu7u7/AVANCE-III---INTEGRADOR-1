package com.mycompany.estacionamiento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class EmpadronadoDialog extends JDialog {
    private JTextField txtPlaca;
    private JTextField txtNombre;
    private boolean confirmado = false;
    private Empadronado empadronado;
    private boolean esEdicion;

    public EmpadronadoDialog(Dialog parent, Empadronado emp) {
        super(parent, emp == null ? "Agregar Empadronado" : "Editar Empadronado", true);
        this.empadronado = emp;
        this.esEdicion = emp != null;
        initComponents();
        if (esEdicion) {
            cargarDatos();
        }
    }

    private void initComponents() {
        setSize(450, 350);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Panel principal con gradiente
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(240, 245, 255),
                        getWidth(), getHeight(), new Color(220, 230, 250));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 118, 210),
                        getWidth(), getHeight(), new Color(33, 150, 243));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(450, 60));
        header.setLayout(new BorderLayout());

        JLabel titulo = new JLabel(esEdicion ? "Editar Empadronado" : "Agregar Empadronado",
                SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.CENTER);
        mainPanel.add(header, BorderLayout.NORTH);

        // Formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Placa
        JLabel lblPlaca = new JLabel("Placa:");
        lblPlaca.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(lblPlaca, gbc);

        txtPlaca = new JTextField();
        txtPlaca.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPlaca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(25, 118, 210), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(txtPlaca, gbc);

        // Nombre
        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(lblNombre, gbc);

        txtNombre = new JTextField();
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(25, 118, 210), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        formPanel.add(txtNombre, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.setOpaque(false);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JButton btnGuardar = new JButton(esEdicion ? "Actualizar" : "Guardar");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setBackground(new Color(46, 204, 113));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnGuardar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardar());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelar.setBackground(new Color(231, 76, 60));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        mainPanel.add(panelBotones, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void cargarDatos() {
        if (empadronado != null) {
            txtPlaca.setText(empadronado.getPlaca());
            txtNombre.setText(empadronado.getNombre());
        }
    }

    private void guardar() {
        String placa = txtPlaca.getText().trim().toUpperCase();
        String nombre = txtNombre.getText().trim();

        if (placa.isEmpty() || nombre.isEmpty()) {
            DialogUtils.showErrorDialog(this, "Placa y nombre son obligatorios.");
            return;
        }

        try (Connection conn = ConexionBD.getConexion()) {
            if (esEdicion) {
                // Actualizar
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE empadronados SET placa = ?, nombre = ? WHERE placa = ?")) {
                    ps.setString(1, placa);
                    ps.setString(2, nombre);
                    ps.setString(3, empadronado.getPlaca());
                    ps.executeUpdate();
                }
            } else {
                // Insertar
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO empadronados (placa, nombre) VALUES (?, ?)")) {
                    ps.setString(1, placa);
                    ps.setString(2, nombre);
                    ps.executeUpdate();
                }
            }

            confirmado = true;
            DialogUtils.showSuccessDialog(this,
                    esEdicion ? "Empadronado actualizado exitosamente." : "Empadronado agregado exitosamente.");
            dispose();

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                DialogUtils.showErrorDialog(this, "Ya existe un empadronado con esa placa.");
            } else {
                DialogUtils.showErrorDialog(this, "Error al guardar: " + e.getMessage());
            }
        }
    }

    public boolean isConfirmado() {
        return confirmado;
    }
}