package com.mycompany.estacionamiento;

import javax.swing.*;
import java.awt.*;

public class DialogUtils {

    public static void showCustomDialog(Component parent, JPanel content, String title, ImageIcon icon) {
        Window parentWindow = null;
        if (parent != null) {
            parentWindow = SwingUtilities.getWindowAncestor(parent);
        }

        JDialog dialog;
        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, title, true);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, title, true);
        } else {
            dialog = new JDialog((Frame) null, title, true);
        }

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 245, 255));

        if (icon != null) {
            JLabel lblIcon = new JLabel(icon);
            lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
            lblIcon.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            mainPanel.add(lblIcon, BorderLayout.NORTH);
        }

        mainPanel.add(content, BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCerrar.setBackground(new Color(255, 0, 0));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(8, 30, 8, 30));
        btnCerrar.addActionListener(e -> dialog.dispose());

        JPanel panelBtn = new JPanel();
        panelBtn.setBackground(new Color(240, 245, 255));
        panelBtn.add(btnCerrar);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(panelBtn, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    public static void showSuccessDialog(Component parent, String mensaje) {
        ImageIcon checkIcon = loadIcon("/imagenes/check.png");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 245, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        if (checkIcon != null) {
            JLabel lblIcon = new JLabel(checkIcon);
            lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(lblIcon);
        }

        JLabel lblMsg = new JLabel("<html><div style='text-align:center;'>" + mensaje.replace("\\n", "<br>") + "</div></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMsg.setForeground(new Color(39, 174, 96));
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblMsg);

        showCustomDialog(parent, panel, "Éxito", null);
    }

    public static void showErrorDialog(Component parent, String mensaje) {
        ImageIcon errorIcon = loadIcon("/imagenes/error.png");
        showIconMessageDialog(parent, mensaje, "Error", errorIcon, new Color(231, 76, 60));
    }

    public static void showInfoDialog(Component parent, Object message) {
        ImageIcon infoIcon = loadIcon("/imagenes/info.png");
        JOptionPane.showMessageDialog(parent, message, "Información", JOptionPane.INFORMATION_MESSAGE, infoIcon);
    }

    public static void showIconMessageDialog(Component parent, String mensaje, String titulo, ImageIcon icon, Color colorTexto) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 245, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        if (icon != null) {
            JLabel lblIcon = new JLabel(icon);
            lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(lblIcon);
        }

        JLabel lblMsg = new JLabel("<html><div style='text-align:center;'>" + mensaje.replace("\\n", "<br>") + "</div></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMsg.setForeground(colorTexto);
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblMsg);

        showCustomDialog(parent, panel, titulo, null);
    }

    // MODIFICADO: Eliminado el icono de pregunta (question.png)
    public static int showConfirmDialog(Component parent, String mensaje, String titulo) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 245, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel lblMsg = new JLabel("<html><div style='text-align:center;'>" + mensaje.replace("\\n", "<br>") + "</div></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblMsg.setForeground(new Color(25, 118, 210));
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblMsg);

        Object[] options = {"Sí", "No"};
        int result = JOptionPane.showOptionDialog(
            parent,
            panel,
            titulo,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, // <-- Sin icono personalizado
            options,
            options[1]
        );

        return result;
    }

    public static void showTicketDialog(Component parent, String comprobante) {
        JTextArea area = new JTextArea(comprobante);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        area.setEditable(false);
        area.setBackground(new Color(240, 245, 255));

        ImageIcon iconoVehiculo = loadIcon("/imagenes/vehiculo.png");

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 245, 255));
        panel.add(new JScrollPane(area), BorderLayout.CENTER);

        showCustomDialog(parent, panel, "Ticket de Estacionamiento", iconoVehiculo);
    }

    // NUEVO: Diálogo de ticket mejorado con botón de imprimir
    public static void showTicketDialogMejorado(Component parent, Ticket ticket) {
        Window parentWindow = null;
        if (parent != null) {
            parentWindow = SwingUtilities.getWindowAncestor(parent);
        }

        JDialog dialog;
        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, "Comprobante de Estacionamiento", true);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, "Comprobante de Estacionamiento", true);
        } else {
            dialog = new JDialog((Frame) null, "Comprobante de Estacionamiento", true);
        }

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(parent);

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
        header.setPreferredSize(new Dimension(500, 80));
        header.setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Comprobante de Estacionamiento", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.CENTER);

        mainPanel.add(header, BorderLayout.NORTH);

        // Contenido del ticket
        JTextArea area = new JTextArea(ticket.generarComprobante());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        mainPanel.add(scroll, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.setOpaque(false);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrar.setBackground(new Color(231, 76, 60));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dialog.dispose());

        JButton btnImprimir = new JButton("Imprimir Ticket");
        btnImprimir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnImprimir.setBackground(new Color(52, 152, 219));
        btnImprimir.setForeground(Color.WHITE);
        btnImprimir.setFocusPainted(false);
        btnImprimir.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnImprimir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnImprimir.addActionListener(e -> {
            PDFGenerator.generarYAbrirTicketPDF(ticket);
        });

        panelBotones.add(btnImprimir);
        panelBotones.add(btnCerrar);

        mainPanel.add(panelBotones, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    public static void showInfoPanel(Component parent, JPanel panel, String titulo) {
        Window parentWindow = null;
        if (parent != null) {
            parentWindow = SwingUtilities.getWindowAncestor(parent);
        }

        JDialog dialog;
        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, titulo, true);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, titulo, true);
        } else {
            dialog = new JDialog((Frame) null, titulo, true);
        }

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.setUndecorated(true);

        // Panel principal decorado
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        // Título
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerPanel.add(lblTitulo, BorderLayout.CENTER);

        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(12));

        // Panel de información (el que recibes como parámetro)
        panel.setOpaque(false);
        mainPanel.add(panel);

        mainPanel.add(Box.createVerticalStrut(20));

        // Botón cerrar centrado
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCerrar.setBackground(new Color(255, 0, 0));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(8, 28, 8, 28));
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.setBorder(BorderFactory.createLineBorder(new Color(237, 27, 27), 1, true));
        btnCerrar.setPreferredSize(new Dimension(100, 40));
        btnCerrar.addActionListener(e -> {
            dialog.setVisible(false);
            dialog.dispose();
        });

        btnPanel.add(btnCerrar);
        mainPanel.add(btnPanel);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    public static int showModernConfirmDialog(Component parent, String mensaje, String titulo) {
        Window parentWindow = null;
        if (parent != null) {
            parentWindow = SwingUtilities.getWindowAncestor(parent);
        }

        JDialog dialog;
        if (parentWindow instanceof Frame) {
            dialog = new JDialog((Frame) parentWindow, titulo, true);
        } else if (parentWindow instanceof Dialog) {
            dialog = new JDialog((Dialog) parentWindow, titulo, true);
        } else {
            dialog = new JDialog((Frame) null, titulo, true);
        }

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.setUndecorated(true); //Esto borra las barras de arriba se ve muchisimo mejor xd

        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        // Título
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(lblTitulo, BorderLayout.CENTER);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0)); // top, left, bottom, right
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerPanel.add(lblTitulo, BorderLayout.WEST);

        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(12));

        // Mensaje
        JLabel lblMsg = new JLabel("<html><div style='width:100%; text-align:center;'>" + mensaje.replace("\\n", "<br>") + "</div></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMsg.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblMsg);

        mainPanel.add(Box.createVerticalStrut(20));

        // Botones centrados
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnCancelar.setBackground(new Color(231, 76, 60));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(BorderFactory.createEmptyBorder(8, 28, 8, 28));
        btnCancelar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancelar.setBorder(BorderFactory.createLineBorder(new Color(231, 76, 60), 1, true));
        btnCancelar.setPreferredSize(new Dimension(80, 40)); // <-- tamaño

        JButton btnConfirmar = new JButton("Confirmar");
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnConfirmar.setBackground(new Color(39, 174, 96));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setBorder(BorderFactory.createEmptyBorder(8, 28, 8, 28));
        btnConfirmar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfirmar.setPreferredSize(new Dimension(80, 40)); // <-- tamaño
        btnConfirmar.setBorder(BorderFactory.createLineBorder(new Color(39, 174, 96), 1, true));

        final int[] result = {JOptionPane.NO_OPTION};

        btnCancelar.addActionListener(e -> {
            result[0] = JOptionPane.NO_OPTION;
            dialog.setVisible(false);
            dialog.dispose();
        });

        btnConfirmar.addActionListener(e -> {
            result[0] = JOptionPane.YES_OPTION;
            dialog.setVisible(false);
            dialog.dispose();
        });

        btnPanel.add(btnConfirmar);
        btnPanel.add(btnCancelar);

        mainPanel.add(btnPanel);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    private static ImageIcon loadIcon(String path) {
        try {
            java.net.URL url = DialogUtils.class.getResource(path);
            if (url != null) {
                return new ImageIcon(url);
            }
        } catch (Exception e) {
            // Ignorar, devolverá null
        }
        return null;
    }
}