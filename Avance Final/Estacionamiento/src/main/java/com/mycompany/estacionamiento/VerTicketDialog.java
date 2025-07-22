package com.mycompany.estacionamiento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VerTicketDialog extends JDialog {
    private int ticketId;
    private JPanel mainPanel;

    // *** CONSTRUCTOR CORREGIDO - SIN CLASSCASTEXCEPTION ***
    public VerTicketDialog(Window parent, int ticketId) {
        // *** SOLUCIÓN: Determinar el Frame padre correctamente ***
        super(getFrameParent(parent), "Detalles del Ticket #" + ticketId, true);
        this.ticketId = ticketId;
        initComponents();
        cargarDatosTicket();
    }

    // *** MÉTODO AUXILIAR PARA OBTENER FRAME PADRE SEGURO ***
    private static Frame getFrameParent(Window parent) {
        if (parent instanceof Frame) {
            return (Frame) parent;
        }
        
        // Buscar Frame en la jerarquía de ventanas
        Window current = parent;
        while (current != null) {
            if (current instanceof Frame) {
                return (Frame) current;
            }
            current = current.getOwner();
        }
        
        // Buscar cualquier Frame disponible
        for (Window window : Window.getWindows()) {
            if (window instanceof Frame && window.isDisplayable()) {
                return (Frame) window;
            }
        }
        
        // Último recurso: null (JDialog puede tener parent null)
        return null;
    }

    // *** CONSTRUCTOR ALTERNATIVO PARA COMPONENT ***
    public VerTicketDialog(Component parent, int ticketId) {
        this(SwingUtilities.getWindowAncestor(parent), ticketId);
    }

    private void initComponents() {
        setSize(500, 650);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Panel principal con gradiente
        mainPanel = new JPanel() {
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
        JPanel header = createHeaderPanel();
        mainPanel.add(header, BorderLayout.NORTH);

        // Content (se llenará dinámicamente)
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Footer con botones
        JPanel footer = createFooterPanel();
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
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
        header.setPreferredSize(new Dimension(500, 70));
        header.setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Detalles del Ticket #" + ticketId, SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.CENTER);

        return header;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JButton btnCerrar = createStyledButton("Cerrar", new Color(255, 0, 0));
        btnCerrar.addActionListener(e -> dispose());

        JButton btnImprimir = createStyledButton("Imprimir", new Color(25, 118, 210));
        btnImprimir.addActionListener(e -> imprimirTicket());

        footer.add(Box.createHorizontalStrut(10));
        footer.add(btnCerrar);

        return footer;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void cargarDatosTicket() {
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM tickets WHERE id = ?")) {

            ps.setInt(1, ticketId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                mostrarDatosTicket(rs);
            } else {
                DialogUtils.showErrorDialog(this, "No se encontró el ticket especificado.");
                dispose();
            }

        } catch (SQLException e) {
            DialogUtils.showErrorDialog(this, "Error al cargar datos: " + e.getMessage());
            dispose();
        }
    }

    private void mostrarDatosTicket(ResultSet rs) throws SQLException {
        // Crear panel de contenido
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Obtener datos del ResultSet
        String placa = rs.getString("placa");
        String nombrePersona = rs.getString("nombre_persona");
        int numeroEspacio = rs.getInt("numero_espacio");
        Timestamp fechaEntrada = rs.getTimestamp("fecha_entrada");
        Timestamp fechaSalida = rs.getTimestamp("fecha_salida");
        Timestamp fechaPago = rs.getTimestamp("fecha_pago");
        String tipoServicio = rs.getString("tipo_servicio");
        int horas = rs.getInt("horas");
        double monto = rs.getDouble("monto");
        double mora = rs.getDouble("mora");
        boolean finalizado = rs.getBoolean("finalizado");

        // Verificar si es empadronado
        boolean esEmpadronado = EstacionamientoSingleton.getInstance().esEmpadronado(placa);

        // *** SECCIÓN: INFORMACIÓN GENERAL ***
        contentPanel.add(createSectionTitle("Información General"));
        contentPanel.add(createInfoRow("ID del Ticket:", "#" + ticketId));
        contentPanel.add(createInfoRow("Placa del Vehículo:", placa));
        contentPanel.add(createInfoRow("Nombre del Cliente:", nombrePersona != null ? nombrePersona : "No especificado"));
        contentPanel.add(createInfoRow("Espacio Asignado:", "E-" + String.format("%02d", numeroEspacio)));

        if (esEmpadronado) {
            JPanel empadronadoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            empadronadoPanel.setOpaque(false);
            JLabel lblEmpadronado = new JLabel("Estado: ");
            lblEmpadronado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            JLabel lblValorEmpadronado = new JLabel("EMPADRONADO");
            lblValorEmpadronado.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblValorEmpadronado.setForeground(new Color(46, 204, 113));
            empadronadoPanel.add(lblEmpadronado);
            empadronadoPanel.add(lblValorEmpadronado);
            empadronadoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(empadronadoPanel);
        }

        contentPanel.add(Box.createVerticalStrut(15));

        // *** SECCIÓN: INFORMACIÓN DE TIEMPO ***
        contentPanel.add(createSectionTitle("Información de Tiempo"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        if (fechaEntrada != null) {
            LocalDateTime entrada = fechaEntrada.toLocalDateTime();
            contentPanel.add(createInfoRow("Fecha de Entrada:", entrada.format(formatter)));
        }

        if (fechaSalida != null) {
            LocalDateTime salida = fechaSalida.toLocalDateTime();
            contentPanel.add(createInfoRow("Fecha de Salida:", salida.format(formatter)));
            // Calcular tiempo total estacionado
            if (fechaEntrada != null) {
                Duration duracion = Duration.between(fechaEntrada.toLocalDateTime(), salida);
                long horas_total = duracion.toHours();
                long minutos = duracion.toMinutes() % 60;
                contentPanel.add(createInfoRow("Tiempo Total:", horas_total + "h " + minutos + "m"));
            }
        } else {
            contentPanel.add(createInfoRow("Fecha de Salida:", "Aún estacionado"));
            if (fechaEntrada != null) {
                Duration duracion = Duration.between(fechaEntrada.toLocalDateTime(), LocalDateTime.now());
                long horas_transcurridas = duracion.toHours();
                long minutos = duracion.toMinutes() % 60;
                contentPanel.add(createInfoRow("Tiempo Transcurrido:", horas_transcurridas + "h " + minutos + "m"));
            }
        }

        contentPanel.add(Box.createVerticalStrut(15));

        // *** SECCIÓN: INFORMACIÓN DEL SERVICIO ***
        contentPanel.add(createSectionTitle("Información del Servicio"));
        contentPanel.add(createInfoRow("Tipo de Servicio:", tipoServicio));

        if ("TIEMPO_DEFINIDO".equals(tipoServicio)) {
            contentPanel.add(createInfoRow("Horas Contratadas:", horas + " horas"));
            // Verificar si hay exceso de tiempo
            if (fechaEntrada != null) {
                LocalDateTime entrada = fechaEntrada.toLocalDateTime();
                LocalDateTime limite = entrada.plusHours(horas);
                LocalDateTime ahora = fechaSalida != null ? fechaSalida.toLocalDateTime() : LocalDateTime.now();

                if (ahora.isAfter(limite)) {
                    Duration exceso = Duration.between(limite, ahora);
                    long horasExceso = exceso.toHours();
                    long minutosExceso = exceso.toMinutes() % 60;

                    JPanel excesoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    excesoPanel.setOpaque(false);
                    JLabel lblExceso = new JLabel("Tiempo de Exceso: ");
                    lblExceso.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    JLabel lblValorExceso = new JLabel(horasExceso + "h " + minutosExceso + "m");
                    lblValorExceso.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    lblValorExceso.setForeground(new Color(231, 76, 60));
                    excesoPanel.add(lblExceso);
                    excesoPanel.add(lblValorExceso);
                    excesoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    contentPanel.add(excesoPanel);
                }
            }
        }

        contentPanel.add(Box.createVerticalStrut(15));

        // *** SECCIÓN: INFORMACIÓN FINANCIERA ***
        contentPanel.add(createSectionTitle("Información Financiera"));
        contentPanel.add(createInfoRow("Monto Base:", "S/ " + String.format("%.2f", monto)));

        if (mora > 0) {
            JPanel moraPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            moraPanel.setOpaque(false);
            JLabel lblMora = new JLabel("Mora por Exceso:           ");
            lblMora.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            JLabel lblValorMora = new JLabel("S/ " + String.format("%.2f", mora));
            lblValorMora.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblValorMora.setForeground(new Color(231, 76, 60));
            moraPanel.add(lblMora);
            moraPanel.add(lblValorMora);
            moraPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(moraPanel);
        }

        // Total
        double total = monto + mora;
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        totalPanel.setOpaque(false);
        JLabel lblTotal = new JLabel("Total Pagado:                 ");
        lblTotal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JLabel lblValorTotal = new JLabel("S/ " + String.format("%.2f", total));
        lblValorTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalPanel.add(lblTotal);
        totalPanel.add(lblValorTotal);
        totalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(totalPanel);

        if (fechaPago != null) {
            contentPanel.add(createInfoRow("Fecha de Pago:", fechaPago.toLocalDateTime().format(formatter)));
        }

        contentPanel.add(Box.createVerticalStrut(15));

        // *** SECCIÓN: ESTADO ***
        contentPanel.add(createSectionTitle("Estado del Ticket"));
        JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        estadoPanel.setOpaque(false);
        JLabel lblEstado = new JLabel("Estado: ");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JLabel lblValorEstado = new JLabel(finalizado ? "FINALIZADO" : "ACTIVO");
        lblValorEstado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblValorEstado.setForeground(finalizado ? new Color(46, 204, 113) : new Color(241, 196, 15));
        estadoPanel.add(lblEstado);
        estadoPanel.add(lblValorEstado);
        estadoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(estadoPanel);

        // Agregar información adicional si está activo
        if (!finalizado) {
            JLabel notaActivo = new JLabel("Este vehículo aún se encuentra estacionado");
            notaActivo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            notaActivo.setForeground(new Color(108, 117, 125));
            notaActivo.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(notaActivo);
        }

        // Agregar el panel de contenido al scroll
        JScrollPane scrollPane = (JScrollPane) ((JPanel) mainPanel.getComponent(1)).getComponent(0);
        scrollPane.setViewportView(contentPanel);
    }

    private JLabel createSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(25, 118, 210));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return label;
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblLabel.setPreferredSize(new Dimension(150, 20));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 14));

        panel.add(lblLabel);
        panel.add(lblValue);

        return panel;
    }

    private void imprimirTicket() {
        // Implementar funcionalidad de impresión
        DialogUtils.showInfoDialog(this, "Funcionalidad de impresión en desarrollo...");
    }

    // *** MÉTODOS ESTÁTICOS PARA USO FÁCIL ***
    public static void mostrarTicket(Component parent, int ticketId) {
        try {
            VerTicketDialog dialog = new VerTicketDialog(parent, ticketId);
            dialog.setVisible(true);
        } catch (Exception e) {
            System.err.println("Error al mostrar ticket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void mostrarTicket(Window parent, int ticketId) {
        try {
            VerTicketDialog dialog = new VerTicketDialog(parent, ticketId);
            dialog.setVisible(true);
        } catch (Exception e) {
            System.err.println("Error al mostrar ticket: " + e.getMessage());
            e.printStackTrace();
        }
    }
}