package com.mycompany.estacionamiento;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminPanel extends JDialog {
    private JTabbedPane tabbedPane;
    private DefaultTableModel modeloVehiculos;
    private DefaultTableModel modeloEmpadronados;
    private DefaultTableModel modeloHistorial;
    private JTable tablaVehiculos;
    private JTable tablaEmpadronados;
    private JTable tablaHistorial;
    
    // *** NUEVOS COMPONENTES PARA ESTADÍSTICAS EN TIEMPO REAL ***
    private JLabel lblIngresosDia;
    private JLabel lblTicketsHoy;
    private JLabel lblTiempoPromedio;
    private JLabel lblTotalEmpadronados;
    private Timer timerEstadisticas;

    public AdminPanel(Window parent) {
        super((Frame) parent, "Panel de Administración", true);
        setUndecorated(true); 
        initComponents();
        cargarDatos();
        iniciarActualizacionAutomatica(); // *** NUEVO: Iniciar actualización automática ***
    }

    // *** NUEVO MÉTODO: Inicializar actualización automática ***
    private void iniciarActualizacionAutomatica() {
        // Timer que actualiza estadísticas cada 5 segundos
        timerEstadisticas = new Timer(5000, e -> actualizarEstadisticasEnTiempoReal());
        timerEstadisticas.start();
        
        // Actualizar inmediatamente
        actualizarEstadisticasEnTiempoReal();
    }

    // *** NUEVO MÉTODO: Actualizar estadísticas en tiempo real ***
    private void actualizarEstadisticasEnTiempoReal() {
        SwingUtilities.invokeLater(() -> {
            if (lblIngresosDia != null) {
                lblIngresosDia.setText("S/ " + String.format("%.2f", calcularIngresosDia()));
            }
            if (lblTicketsHoy != null) {
                lblTicketsHoy.setText(String.valueOf(contarTicketsHoy()));
            }
            if (lblTiempoPromedio != null) {
                lblTiempoPromedio.setText(calcularTiempoPromedio() + " horas");
            }
            if (lblTotalEmpadronados != null) {
                lblTotalEmpadronados.setText(String.valueOf(contarEmpadronados()));
            }
        });
    }

    @Override
    public void dispose() {
        // *** IMPORTANTE: Detener el timer al cerrar ***
        if (timerEstadisticas != null) {
            timerEstadisticas.stop();
        }
        super.dispose();
    }

    private void initComponents() {
        setSize(1200, 850); // *** AUMENTADO EL TAMAÑO PARA QUE ENTREN TODAS LAS TARIFAS ***
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
        JPanel header = createHeaderPanel();
        mainPanel.add(header, BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(240, 245, 255));

        // Tab 1: Gestión de Vehículos
        JPanel panelVehiculos = createVehiculosPanel();
        tabbedPane.addTab("Vehiculos Activos", panelVehiculos);

        // Tab 2: Gestión de Empadronados
        JPanel panelEmpadronados = createEmpadronadosPanel();
        tabbedPane.addTab("Empadronados", panelEmpadronados);

        // Tab 3: Historial de Tickets
        JPanel panelHistorial = createHistorialPanel();
        tabbedPane.addTab("Historial", panelHistorial);
        
        // Tab 4: Tarifas
        JPanel panelTarifas = createTarifasPanel();
        tabbedPane.addTab("Tarifas", panelTarifas);

        // Tab 5: Estadísticas
        JPanel panelEstadisticas = createEstadisticasPanel();
        tabbedPane.addTab("Estadisticas", panelEstadisticas);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Footer
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
        header.setPreferredSize(new Dimension(1000, 80));
        header.setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Panel de Administracion", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.CENTER);

        return header;
    }

    private JPanel createVehiculosPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel titulo = new JLabel("Gestión de Vehículos Estacionados");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(new Color(25, 118, 210));
        panel.add(titulo, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"Espacio", "Placa", "Nombre", "Entrada", "Tipo", "Horas"};
        modeloVehiculos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaVehiculos = new JTable(modeloVehiculos);
        tablaVehiculos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaVehiculos.setRowHeight(25);
        tablaVehiculos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaVehiculos.getTableHeader().setBackground(new Color(25, 118, 210));
        tablaVehiculos.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollVehiculos = new JScrollPane(tablaVehiculos);
        scrollVehiculos.setBorder(BorderFactory.createTitledBorder("Vehículos Activos"));
        panel.add(scrollVehiculos, BorderLayout.CENTER);

        // *** BOTONES ACTUALIZADOS CON EDITAR ***
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.setOpaque(false);

        JButton btnLiberar = createStyledButton("Liberar Espacio", new Color(231, 76, 60));
        btnLiberar.addActionListener(e -> liberarEspacioSeleccionado());

        // *** NUEVO BOTÓN: Editar Vehículo ***
        JButton btnEditar = createStyledButton("Editar Vehículo", new Color(241, 196, 15));
        btnEditar.addActionListener(e -> editarVehiculoSeleccionado());

        JButton btnInfo = createStyledButton("Ver Información", new Color(52, 152, 219));
        btnInfo.addActionListener(e -> verInformacionVehiculo());

        JButton btnRefrescar = createStyledButton("Refrescar", new Color(46, 204, 113));
        btnRefrescar.addActionListener(e -> cargarVehiculosActivos());

        panelBotones.add(btnLiberar);
        panelBotones.add(btnEditar); // *** NUEVO BOTÓN AGREGADO ***
        panelBotones.add(btnInfo);
        panelBotones.add(btnRefrescar);

        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    // *** NUEVO MÉTODO: Editar vehículo seleccionado ***
private void editarVehiculoSeleccionado() {
    int fila = tablaVehiculos.getSelectedRow();
    if (fila == -1) {
        DialogUtils.showErrorDialog(this, "Seleccione un vehículo para editar.");
        return;
    }

    String espacio = (String) modeloVehiculos.getValueAt(fila, 0);
    String placa = (String) modeloVehiculos.getValueAt(fila, 1);
    String nombre = (String) modeloVehiculos.getValueAt(fila, 2);
    String tipo = (String) modeloVehiculos.getValueAt(fila, 4);
    int horas = (Integer) modeloVehiculos.getValueAt(fila, 5);

    // Crear diálogo de edición
    EditarVehiculoDialog dialog = new EditarVehiculoDialog(this, placa, nombre, tipo, horas);
    dialog.setVisible(true);

    if (dialog.isConfirmado()) {
        // *** ACTUALIZAR CON NUEVA PLACA ***
        actualizarVehiculoEnBD(
            dialog.getPlacaOriginal(),  // Placa original para buscar
            dialog.getNuevaPlaca(),     // Nueva placa
            dialog.getNuevoNombre(), 
            dialog.getNuevoTipo(), 
            dialog.getNuevasHoras()
        );
        cargarVehiculosActivos();
        DialogUtils.showSuccessDialog(this, "Vehículo actualizado exitosamente.");
    }
}


    // *** NUEVO MÉTODO: Actualizar vehículo en base de datos ***
private void actualizarVehiculoEnBD(String placaOriginal, String nuevaPlaca, String nuevoNombre, String nuevoTipo, int nuevasHoras) {
    try (Connection conn = ConexionBD.getConexion()) {
        
        // 1. Actualizar ticket
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE tickets SET placa = ?, nombre_persona = ?, tipo_servicio = ?, horas = ? WHERE placa = ? AND finalizado = 0")) {
            ps.setString(1, nuevaPlaca);
            ps.setString(2, nuevoNombre);
            ps.setString(3, nuevoTipo);
            ps.setInt(4, nuevasHoras);
            ps.setString(5, placaOriginal);
            ps.executeUpdate();
        }
        
        // 2. Actualizar espacio (si cambió la placa)
        if (!placaOriginal.equals(nuevaPlaca)) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE espacios SET placa = ? WHERE placa = ?")) {
                ps.setString(1, nuevaPlaca);
                ps.setString(2, placaOriginal);
                ps.executeUpdate();
            }
        }
        
        // 3. Actualizar en memoria
        Estacionamiento estacionamiento = EstacionamientoSingleton.getInstance();
        for (Espacio espacio : estacionamiento.getEspacios()) {
            if (espacio.isOcupado() && placaOriginal.equals(espacio.getPlaca())) {
                // Usar reflexión para actualizar la placa en memoria
                try {
                    java.lang.reflect.Field placaField = Espacio.class.getDeclaredField("placa");
                    placaField.setAccessible(true);
                    placaField.set(espacio, nuevaPlaca);
                } catch (Exception e) {
                    System.err.println("Error al actualizar placa en memoria: " + e.getMessage());
                }
                break;
            }
        }
        
        System.out.println("[LOG] Vehículo actualizado: " + placaOriginal + " -> " + nuevaPlaca);
        
    } catch (SQLException e) {
        System.err.println("Error al actualizar vehículo: " + e.getMessage());
        DialogUtils.showErrorDialog(this, "Error al actualizar: " + e.getMessage());
    }
}

    private JPanel createEmpadronadosPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel titulo = new JLabel("Gestión de Vehículos Empadronados");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(new Color(25, 118, 210));
        panel.add(titulo, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"ID", "Placa", "Nombre"};
        modeloEmpadronados = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaEmpadronados = new JTable(modeloEmpadronados);
        tablaEmpadronados.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaEmpadronados.setRowHeight(25);
        tablaEmpadronados.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaEmpadronados.getTableHeader().setBackground(new Color(25, 118, 210));
        tablaEmpadronados.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollEmpadronados = new JScrollPane(tablaEmpadronados);
        scrollEmpadronados.setBorder(BorderFactory.createTitledBorder("Lista de Empadronados"));
        panel.add(scrollEmpadronados, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.setOpaque(false);

        JButton btnAgregar = createStyledButton("Agregar", new Color(46, 204, 113));
        btnAgregar.addActionListener(e -> agregarEmpadronado());

        JButton btnEditar = createStyledButton("Editar", new Color(241, 196, 15));
        btnEditar.addActionListener(e -> editarEmpadronado());

        JButton btnEliminar = createStyledButton("Eliminar", new Color(231, 76, 60));
        btnEliminar.addActionListener(e -> eliminarEmpadronado());

        JButton btnRefrescar = createStyledButton("Refrescar", new Color(52, 152, 219));
        btnRefrescar.addActionListener(e -> cargarEmpadronados());

        panelBotones.add(btnAgregar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnRefrescar);

        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistorialPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel titulo = new JLabel("Historial de Tickets");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(new Color(25, 118, 210));
        panel.add(titulo, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"ID", "Placa", "Nombre", "Espacio", "Entrada", "Salida", "Tipo", "Monto", "Mora" , "Estado"};
        modeloHistorial = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaHistorial = new JTable(modeloHistorial);
        tablaHistorial.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tablaHistorial.setRowHeight(25);
        tablaHistorial.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tablaHistorial.getTableHeader().setBackground(new Color(25, 118, 210));
        tablaHistorial.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollHistorial = new JScrollPane(tablaHistorial);
        scrollHistorial.setBorder(BorderFactory.createTitledBorder("Historial Completo"));
        panel.add(scrollHistorial, BorderLayout.CENTER);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.setOpaque(false);

        JButton btnVerTicket = createStyledButton("Ver Ticket", new Color(52, 152, 219));
        btnVerTicket.addActionListener(e -> verTicketSeleccionado());

        JButton btnRefrescar = createStyledButton("Refrescar", new Color(46, 204, 113));
        btnRefrescar.addActionListener(e -> cargarHistorial());

        JButton btnExportar = createStyledButton("Exportar", new Color(155, 89, 182));
        btnExportar.addActionListener(e -> ExportadorHistorial.exportarHistorialCSV(this));

        panelBotones.add(btnVerTicket);
        panelBotones.add(btnRefrescar);
        panelBotones.add(btnExportar);

        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }
private JPanel createTarifasPanel() {
    JPanel panel = new JPanel(new BorderLayout(10, 10));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // *** REDUCIDO PADDING ***
    
    // Título con icono
    JPanel tituloPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    tituloPanel.setOpaque(false);
    
    JLabel titulo = new JLabel("Gestion de Tarifas");
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 20)); // *** REDUCIDO TAMAÑO DE FUENTE ***
    titulo.setForeground(new Color(25, 118, 210));
    tituloPanel.add(titulo);
    
    panel.add(tituloPanel, BorderLayout.NORTH);
    
    // *** USAR SCROLLPANE PARA ASEGURAR QUE TODO ENTRE ***
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setBorder(null);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    
    // Panel central con diseño moderno
    JPanel panelCentral = new JPanel(new GridBagLayout());
    panelCentral.setOpaque(false);
    panelCentral.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(230, 230, 230), 2, true),
        BorderFactory.createEmptyBorder(15, 15, 15, 15) // *** REDUCIDO PADDING ***
    ));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 8, 8, 8); // *** REDUCIDO ESPACIADO ***
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    // Crear campos de texto con diseño moderno
    JTextField txtTarifaDefinido = createModernTextField(String.valueOf(Ticket.TARIFA_DEFINIDO));
    JTextField txtTarifaIndefinido = createModernTextField(String.valueOf(Ticket.TARIFA_INDEFINIDO));
    JTextField txtMoraPorHora = createModernTextField(String.valueOf(Ticket.MORA_POR_HORA));
    
    // *** NUEVOS CAMPOS PARA TARIFAS NOCTURNAS ***
    JTextField txtTarifaDefinidoNocturna = createModernTextField(String.valueOf(Ticket.TARIFA_DEFINIDO_NOCTURNA));
    JTextField txtTarifaIndefinidoNocturna = createModernTextField(String.valueOf(Ticket.TARIFA_INDEFINIDO_NOCTURNA));
    JTextField txtMoraPorHoraNocturna = createModernTextField(String.valueOf(Ticket.MORA_POR_HORA_NOCTURNA));
    
    // Tarifa Tiempo Definido
    gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4;
    JLabel lblTarifaDefinido = createModernLabel("Tarifa Tiempo Definido (S/):");
    panelCentral.add(lblTarifaDefinido, gbc);
    
    gbc.gridx = 1; gbc.weightx = 0.6;
    panelCentral.add(txtTarifaDefinido, gbc);
    
    // Tarifa Tiempo Indefinido
    gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.4;
    JLabel lblTarifaIndefinido = createModernLabel("Tarifa Tiempo Indefinido (S/):");
    panelCentral.add(lblTarifaIndefinido, gbc);
    
    gbc.gridx = 1; gbc.weightx = 0.6;
    panelCentral.add(txtTarifaIndefinido, gbc);
    
    // Mora por Hora
    gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.4;
    JLabel lblMoraPorHora = createModernLabel("Mora por Hora (S/):");
    panelCentral.add(lblMoraPorHora, gbc);
    
    gbc.gridx = 1; gbc.weightx = 0.6;
    panelCentral.add(txtMoraPorHora, gbc);
    
    // *** SEPARADOR VISUAL ***
    gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0;
    JLabel separadorLabel = new JLabel("TARIFAS NOCTURNAS (12:00 AM - 6:00 AM)");
    separadorLabel.setFont(new Font("Segoe UI", Font.BOLD, 14)); // *** REDUCIDO TAMAÑO ***
    separadorLabel.setForeground(new Color(52, 73, 94));
    separadorLabel.setHorizontalAlignment(SwingConstants.CENTER);
    panelCentral.add(separadorLabel, gbc);
    
    // Tarifa Tiempo Definido Nocturna
    gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 0.4;
    JLabel lblTarifaDefinidoNocturna = createModernLabel("Tarifa Definido Nocturna (S/):");
    panelCentral.add(lblTarifaDefinidoNocturna, gbc);
    
    gbc.gridx = 1; gbc.weightx = 0.6;
    panelCentral.add(txtTarifaDefinidoNocturna, gbc);
    
    // Tarifa Tiempo Indefinido Nocturna
    gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.4;
    JLabel lblTarifaIndefinidoNocturna = createModernLabel("Tarifa Indefinido Nocturna (S/):");
    panelCentral.add(lblTarifaIndefinidoNocturna, gbc);
    
    gbc.gridx = 1; gbc.weightx = 0.6;
    panelCentral.add(txtTarifaIndefinidoNocturna, gbc);
    
    // Mora por Hora Nocturna
    gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.4;
    JLabel lblMoraPorHoraNocturna = createModernLabel("Mora Nocturna por Hora (S/):");
    panelCentral.add(lblMoraPorHoraNocturna, gbc);
    
    gbc.gridx = 1; gbc.weightx = 0.6;
    panelCentral.add(txtMoraPorHoraNocturna, gbc);
    
    // Panel para mostrar valores actuales (tarjeta moderna)
    gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; gbc.weightx = 1.0;
    JPanel panelActuales = createModernInfoCard();
    JLabel lblActuales = new JLabel("<html><div style='text-align: center;'>"
        + "📊 <b>Tarifas Actuales</b><br>"
        + "Definido: <span style='color: #000000;'>S/ " + String.format("%.2f", Ticket.TARIFA_DEFINIDO) + "</span> | "
        + "Indefinido: <span style='color: #000000;'>S/ " + String.format("%.2f", Ticket.TARIFA_INDEFINIDO) + "</span> | "
        + "Mora: <span style='color: #000000;'>S/ " + String.format("%.2f", Ticket.MORA_POR_HORA) + "</span>"
        + "</div></html>");
    lblActuales.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lblActuales.setHorizontalAlignment(SwingConstants.CENTER);
    panelActuales.add(lblActuales);
    
    panelCentral.add(panelActuales, gbc);
    
    panel.add(panelCentral, BorderLayout.CENTER);
    
    // Panel de botones modernos
    JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
    panelBotones.setOpaque(false);
    
    JButton btnGuardar = createModernButton("Guardar Tarifas", new Color(46, 204, 113), Color.WHITE);
    btnGuardar.addActionListener(e -> {
        try {
            double nuevaTarifaDefinido = Double.parseDouble(txtTarifaDefinido.getText());
            double nuevaTarifaIndefinido = Double.parseDouble(txtTarifaIndefinido.getText());
            double nuevaMoraPorHora = Double.parseDouble(txtMoraPorHora.getText());
            
            // *** NUEVAS TARIFAS NOCTURNAS ***
            double nuevaTarifaDefinidoNocturna = Double.parseDouble(txtTarifaDefinidoNocturna.getText());
            double nuevaTarifaIndefinidoNocturna = Double.parseDouble(txtTarifaIndefinidoNocturna.getText());
            double nuevaMoraPorHoraNocturna = Double.parseDouble(txtMoraPorHoraNocturna.getText());
            
            if (nuevaTarifaDefinido <= 0 || nuevaTarifaIndefinido <= 0 || nuevaMoraPorHora <= 0 ||
                nuevaTarifaDefinidoNocturna <= 0 || nuevaTarifaIndefinidoNocturna <= 0 || nuevaMoraPorHoraNocturna <= 0) {
                showModernMessage("Error", 
                    "Todas las tarifas deben ser valores positivos", 
                    new Color(231, 76, 60));
                return;
            }
            
            // Actualizar las tarifas normales
            Ticket.TARIFA_DEFINIDO = nuevaTarifaDefinido;
            Ticket.TARIFA_INDEFINIDO = nuevaTarifaIndefinido;
            Ticket.MORA_POR_HORA = nuevaMoraPorHora;
            
            // *** ACTUALIZAR LAS TARIFAS NOCTURNAS ***
            Ticket.TARIFA_DEFINIDO_NOCTURNA = nuevaTarifaDefinidoNocturna;
            Ticket.TARIFA_INDEFINIDO_NOCTURNA = nuevaTarifaIndefinidoNocturna;
            Ticket.MORA_POR_HORA_NOCTURNA = nuevaMoraPorHoraNocturna;
            
            // Actualizar la etiqueta de valores actuales
            boolean esNocturno = Ticket.esHorarioNocturnoActual();
            String horarioActual = esNocturno ? "🌙 NOCTURNO" : "☀️ DIURNO";
            
            lblActuales.setText("<html><div style='text-align: center;'>"
                + "📊 <b>Tarifas Actuales (" + horarioActual + ")</b><br>"
                + "Definido: <span style='color: #000000;'>S/ " + String.format("%.2f", Ticket.TARIFA_DEFINIDO) + "</span> | "
                + "Indefinido: <span style='color: #000000;'>S/ " + String.format("%.2f", Ticket.TARIFA_INDEFINIDO) + "</span> | "
                + "Mora: <span style='color: #000000;'>S/ " + String.format("%.2f", Ticket.MORA_POR_HORA) + "</span><br>"
                + "<small>Nocturnas - Def: S/ " + String.format("%.2f", Ticket.TARIFA_DEFINIDO_NOCTURNA) + 
                " | Indef: S/ " + String.format("%.2f", Ticket.TARIFA_INDEFINIDO_NOCTURNA) + 
                " | Mora: S/ " + String.format("%.2f", Ticket.MORA_POR_HORA_NOCTURNA) + "</small>"
                + "</div></html>");
            
            showModernMessage("Éxito", 
                "¡Tarifas diurnas y nocturnas actualizadas correctamente!", 
                new Color(46, 204, 113));
                
        } catch (NumberFormatException ex) {
            showModernMessage("Error", 
                "Por favor ingrese valores numéricos válidos en todos los campos", 
                new Color(231, 76, 60));
        }
    });
    
    JButton btnRestaurar = createModernButton("Restaurar", new Color(52, 152, 219), Color.WHITE);
    btnRestaurar.addActionListener(e -> {
        showModernConfirmDialog("Confirmar Restauración", 
            "¿Está seguro de restaurar las tarifas predeterminadas?", 
            () -> {
                txtTarifaDefinido.setText("5.0");
                txtTarifaIndefinido.setText("8.0");
                txtMoraPorHora.setText("3.0");
                
                Ticket.TARIFA_DEFINIDO = 5.0;
                Ticket.TARIFA_INDEFINIDO = 8.0;
                Ticket.MORA_POR_HORA = 3.0;
                
                txtTarifaDefinidoNocturna.setText("8.0");
                txtTarifaIndefinidoNocturna.setText("11.0");
                txtMoraPorHoraNocturna.setText("6.0");
                
                Ticket.TARIFA_DEFINIDO_NOCTURNA = 8.0;
                Ticket.TARIFA_INDEFINIDO_NOCTURNA = 11.0;
                Ticket.MORA_POR_HORA_NOCTURNA = 6.0;
                
                lblActuales.setText("<html><div style='text-align: center;'>"
                    + "📊 <b>Tarifas Actuales</b><br>"
                    + "Definido: <span style='color: #000000;'>S/ " + String.format("%.2f", Ticket.TARIFA_DEFINIDO) + "</span> | "
                    + "Indefinido: <span style='color: #000000;'>S/ " + String.format("%.2f", Ticket.TARIFA_INDEFINIDO) + "</span> | "
                    + "Mora: <span style='color: #000000;'>S/ " + String.format("%.2f", Ticket.MORA_POR_HORA) + "</span>"
                    + "</div></html>");
                
                showModernMessage("Restaurado", 
                    "¡Tarifas restauradas a valores predeterminados!", 
                    new Color(46, 204, 113));
            });
    });
    
    JButton btnRefrescar = createModernButton("Refrescar", new Color(155, 89, 182), Color.WHITE);
    btnRefrescar.addActionListener(e -> {
        txtTarifaDefinido.setText(String.valueOf(Ticket.TARIFA_DEFINIDO));
        txtTarifaIndefinido.setText(String.valueOf(Ticket.TARIFA_INDEFINIDO));
        txtMoraPorHora.setText(String.valueOf(Ticket.MORA_POR_HORA));
        
        lblActuales.setText("<html><div style='text-align: center;'>"
            + "📊 <b>Tarifas Actuales</b><br>"
            + "Definido: <span style='color: #000000;'>S/ " + String.format("%.2f", Ticket.TARIFA_DEFINIDO) + "</span> | "
            + "Indefinido: <span style='color: #000000;'>S/ " + String.format("%.2f", Ticket.TARIFA_INDEFINIDO) + "</span> | "
            + "Mora: <span style='color: #000000;'>S/ " + String.format("%.2f", Ticket.MORA_POR_HORA) + "</span>"
            + "</div></html>");
        
        showModernMessage("Actualizado", 
            "Valores actualizados correctamente", 
            new Color(52, 152, 219));
    });
    
    panelBotones.add(btnGuardar);
    panelBotones.add(btnRestaurar);
    panelBotones.add(btnRefrescar);
    
    panel.add(panelBotones, BorderLayout.SOUTH);
    
    return panel;
}

// Método para crear campos de texto modernos
private JTextField createModernTextField(String text) {
    JTextField field = new JTextField(text);
    field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    field.setPreferredSize(new Dimension(120, 35));
    field.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
        BorderFactory.createEmptyBorder(8, 12, 8, 12)
    ));
    field.setBackground(new Color(248, 249, 250));
    
    // Efecto hover
    field.addFocusListener(new java.awt.event.FocusAdapter() {
        @Override
        public void focusGained(java.awt.event.FocusEvent evt) {
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(25, 118, 210), 2, true),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)
            ));
        }
        
        @Override
        public void focusLost(java.awt.event.FocusEvent evt) {
            field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
        }
    });
    
    return field;
}

// Método para crear etiquetas modernas
private JLabel createModernLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("Segoe UI", Font.BOLD, 14));
    label.setForeground(new Color(52, 73, 94));
    return label;
}

// Método para crear botones modernos
private JButton createModernButton(String text, Color bgColor, Color textColor) {
    JButton button = new JButton(text);
    button.setFont(new Font("Segoe UI", Font.BOLD, 13));
    button.setForeground(textColor);
    button.setBackground(bgColor);
    button.setPreferredSize(new Dimension(110, 30));
    button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    button.setFocusPainted(false);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    
    // Efecto hover
    button.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            button.setBackground(bgColor.darker());
        }
        
        @Override
        public void mouseExited(java.awt.event.MouseEvent evt) {
            button.setBackground(bgColor);
        }
    });
    
    return button;
}

// Método para crear tarjeta de información moderna
private JPanel createModernInfoCard() {
    JPanel card = new JPanel(new FlowLayout(FlowLayout.CENTER));
    card.setBackground(new Color(248, 249, 250));
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
        BorderFactory.createEmptyBorder(15, 20, 15, 20)
    ));
    return card;
}

// Método para mostrar mensajes modernos
private void showModernMessage(String title, String message, Color color) {
    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
    dialog.setUndecorated(true);
    dialog.setSize(400, 180);
    dialog.setLocationRelativeTo(this);
    
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    // Título
    JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
    lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
    lblTitle.setForeground(color);
    panel.add(lblTitle, BorderLayout.NORTH);
    
    // Mensaje
    JLabel lblMessage = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>", SwingConstants.CENTER);
    lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblMessage.setForeground(new Color(52, 73, 94));
    lblMessage.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
    panel.add(lblMessage, BorderLayout.CENTER);
    
    // Botón cerrar
    JButton btnCerrar = createModernButton("Cerrar", color, Color.WHITE);
    btnCerrar.addActionListener(e -> dialog.dispose());
    
    JPanel buttonPanel = new JPanel(new FlowLayout());
    buttonPanel.setBackground(Color.WHITE);
    buttonPanel.add(btnCerrar);
    panel.add(buttonPanel, BorderLayout.SOUTH);
    
    dialog.add(panel);
    dialog.setVisible(true);
}

// Método para mostrar diálogo de confirmación moderno
private void showModernConfirmDialog(String title, String message, Runnable onConfirm) {
    JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
    dialog.setUndecorated(true);
    dialog.setSize(450, 200);
    dialog.setLocationRelativeTo(this);
    
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    // Título
    JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
    lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
    lblTitle.setForeground(new Color(52, 152, 219));
    panel.add(lblTitle, BorderLayout.NORTH);
    
    // Mensaje
    JLabel lblMessage = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>", SwingConstants.CENTER);
    lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblMessage.setForeground(new Color(52, 73, 94));
    lblMessage.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
    panel.add(lblMessage, BorderLayout.CENTER);
    
    // Botones
    JPanel buttonPanel = new JPanel(new FlowLayout());
    buttonPanel.setBackground(Color.WHITE);
    
    JButton btnSi = createModernButton("Sí", new Color(46, 204, 113), Color.WHITE);
    btnSi.addActionListener(e -> {
        dialog.dispose();
        onConfirm.run();
    });
    
    JButton btnNo = createModernButton("No", new Color(231, 76, 60), Color.WHITE);
    btnNo.addActionListener(e -> dialog.dispose());
    
    buttonPanel.add(btnSi);
    buttonPanel.add(btnNo);
    panel.add(buttonPanel, BorderLayout.SOUTH);
    
    dialog.add(panel);
    dialog.setVisible(true);
}
        
    // *** MÉTODO ACTUALIZADO: Estadísticas con referencias a labels ***
    private JPanel createEstadisticasPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // *** ESTADÍSTICAS CON LABELS REFERENCIADOS ***
        JPanel panelIngresos = createEstadisticaCardConLabel("Ingreso Total", new Color(46, 204, 113));
        lblIngresosDia = (JLabel) ((JPanel) panelIngresos).getComponent(2); // Obtener referencia al label

        JPanel panelTickets = createEstadisticaCardConLabel("Tickets Procesados", new Color(52, 152, 219));
        lblTicketsHoy = (JLabel) ((JPanel) panelTickets).getComponent(2);

        JPanel panelTiempo = createEstadisticaCardConLabel("Tiempo Promedio", new Color(155, 89, 182));
        lblTiempoPromedio = (JLabel) ((JPanel) panelTiempo).getComponent(2);

        JPanel panelEmpadronados = createEstadisticaCardConLabel("Total Empadronados", new Color(241, 196, 15));
        lblTotalEmpadronados = (JLabel) ((JPanel) panelEmpadronados).getComponent(2);

        panel.add(panelIngresos);
        panel.add(panelTickets);
        panel.add(panelTiempo);
        panel.add(panelEmpadronados);

        return panel;
    }

    // *** NUEVO MÉTODO: Crear card de estadística con label referenciado ***
    private JPanel createEstadisticaCardConLabel(String titulo, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, color, getWidth(), getHeight(), color.darker());
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblValor = new JLabel("Cargando...");
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValor.setForeground(Color.WHITE);
        lblValor.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(10));
        card.add(lblValor);

        return card;
    }

    private JPanel createEstadisticaCard(String titulo, String valor, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, color, getWidth(), getHeight(), color.darker());
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblValor.setForeground(Color.WHITE);
        lblValor.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(10));
        card.add(lblValor);

        return card;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton btnCerrar = createStyledButton("Cerrar", new Color(231, 76, 60));
        btnCerrar.addActionListener(e -> dispose());

        footer.add(btnCerrar);

        return footer;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void cargarDatos() {
        cargarVehiculosActivos();
        cargarEmpadronados();
        cargarHistorial();
    }

    private void cargarVehiculosActivos() {
        modeloVehiculos.setRowCount(0);
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT e.numero, t.placa, t.nombre_persona, t.fecha_entrada, t.tipo_servicio, t.horas " +
                     "FROM espacios e " +
                     "JOIN tickets t ON e.placa = t.placa " +
                     "WHERE e.ocupado = 1 AND t.finalizado = 0 " +
                     "ORDER BY e.numero")) {

            while (rs.next()) {
                Object[] fila = {
                    "E-" + String.format("%02d", rs.getInt("numero")),
                    rs.getString("placa"),
                    rs.getString("nombre_persona"),
                    rs.getTimestamp("fecha_entrada").toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    rs.getString("tipo_servicio"),
                    rs.getInt("horas")
                };
                modeloVehiculos.addRow(fila);
            }

        } catch (SQLException e) {
            DialogUtils.showErrorDialog(this, "Error al cargar vehículos: " + e.getMessage());
        }
    }

    private void cargarEmpadronados() {
        modeloEmpadronados.setRowCount(0);
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, placa, nombre FROM empadronados ORDER BY id ASC")) {

            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id"),
                    rs.getString("placa"),
                    rs.getString("nombre")
                };
                modeloEmpadronados.addRow(fila);
            }

        } catch (SQLException e) {
            DialogUtils.showErrorDialog(this, "Error al cargar empadronados: " + e.getMessage());
        }
    }

    private void cargarHistorial() {
        modeloHistorial.setRowCount(0);
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, placa, nombre_persona, numero_espacio, fecha_entrada, fecha_salida, " +
                     "tipo_servicio, monto, mora, finalizado FROM tickets ORDER BY fecha_entrada ASC LIMIT 100")) {

            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id"),
                    rs.getString("placa"),
                    rs.getString("nombre_persona"),
                    "E-" + String.format("%02d", rs.getInt("numero_espacio")),
                    rs.getTimestamp("fecha_entrada").toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    rs.getTimestamp("fecha_salida") != null ?
                            rs.getTimestamp("fecha_salida").toLocalDateTime()
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Activo",
                    rs.getString("tipo_servicio"),
                    "S/ " + String.format("%.2f", rs.getDouble("monto")),
                    rs.getString("mora"),
                    rs.getBoolean("finalizado") ? "Finalizado" : "Activo"
                };
                modeloHistorial.addRow(fila);
            }

        } catch (SQLException e) {
            DialogUtils.showErrorDialog(this, "Error al cargar historial: " + e.getMessage());
        }
    }

    // Métodos de acción
private void liberarEspacioSeleccionado() {
    int fila = tablaVehiculos.getSelectedRow();
    if (fila == -1) {
        DialogUtils.showErrorDialog(this, "Seleccione un vehículo para liberar.");
        return;
    }

    String espacioStr = (String) modeloVehiculos.getValueAt(fila, 0); // E-01, E-02, etc.
    String placa = (String) modeloVehiculos.getValueAt(fila, 1);
    
    // Extraer número del espacio (E-01 -> 1)
    int numeroEspacio = Integer.parseInt(espacioStr.substring(2));

    int confirm = DialogUtils.showModernConfirmDialog(this,
            "¿Está seguro de liberar el vehículo con placa " + placa + "?",
            "Confirmar Liberación");

    if (confirm == JOptionPane.YES_OPTION) {
        try {
            // *** EJECUTAR LIBERACIÓN REAL ***
            
            // 1. Obtener el espacio del singleton
            Estacionamiento estacionamiento = EstacionamientoSingleton.getInstance();
            Espacio espacio = null;
            
            // Buscar el espacio por número
            for (Espacio e : estacionamiento.getEspacios()) {
                if (e.getNumero() == numeroEspacio) {
                    espacio = e;
                    break;
                }
            }
            
            if (espacio != null && espacio.isOcupado()) {
                // 2. Buscar y finalizar el ticket
                Ticket ticket = estacionamiento.buscarTicketPorPlaca(placa);
                if (ticket != null) {
                    ticket.registrarSalida();
                    
                    // Calcular mora si existe
                    double mora = ticket.calcularMora();
                    if (mora > 0) {
                        ticket.registrarMora(mora);
                    }
                }
                
                // 3. Liberar el espacio (actualiza BD y memoria)
                estacionamiento.liberarEspacio(espacio);
                
                // 4. Actualizar la tabla
                cargarVehiculosActivos();
                
                // 5. Mostrar confirmación
                DialogUtils.showSuccessDialog(this, 
                    "Vehículo con placa " + placa + " liberado exitosamente.");
                
                System.out.println("[LOG] Espacio " + numeroEspacio + " liberado desde AdminPanel");
                
            } else {
                DialogUtils.showErrorDialog(this, "Error: El espacio no está ocupado o no existe.");
            }
            
        } catch (Exception e) {
            System.err.println("Error al liberar espacio: " + e.getMessage());
            e.printStackTrace();
            DialogUtils.showErrorDialog(this, "Error al liberar espacio: " + e.getMessage());
        }
    }
}

private void verInformacionVehiculo() {
    int fila = tablaVehiculos.getSelectedRow();
    if (fila == -1) {
        DialogUtils.showErrorDialog(this, "Seleccione un vehículo para ver información.");
        return;
    }

    String espacioStr = (String) modeloVehiculos.getValueAt(fila, 0); // E-01, E-02, etc.
    String placa = (String) modeloVehiculos.getValueAt(fila, 1);
    
    // Extraer número del espacio (E-01 -> 1)
    int numeroEspacio = Integer.parseInt(espacioStr.substring(2));
    
    // Buscar el espacio en el singleton
    Estacionamiento estacionamiento = EstacionamientoSingleton.getInstance();
    Espacio espacio = null;
    
    for (Espacio e : estacionamiento.getEspacios()) {
        if (e.getNumero() == numeroEspacio) {
            espacio = e;
            break;
        }
    }
    
    if (espacio == null) {
        DialogUtils.showErrorDialog(this, "No se encontró el espacio seleccionado.");
        return;
    }
    
    // Usar la misma lógica que mostrarInfoAuto
    LocalDateTime horaEntrada = espacio.getHoraEntrada();
    Ticket ticket = estacionamiento.buscarTicketPorPlaca(placa);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
    panel.add(Box.createVerticalStrut(1));

    // *** VERIFICAR SI ES EMPADRONADO ***
    boolean esEmpadronado = false;
    if (placa != null && !placa.trim().isEmpty()) {
        esEmpadronado = EstacionamientoSingleton.getInstance().esEmpadronado(placa);
    }

    // *** INFORMACIÓN BÁSICA ***
    if (ticket != null) {
        JLabel nombreLabel = new JLabel("Nombre: " + (ticket.getNombrePersona() != null ? ticket.getNombrePersona() : "No especificado"));
        nombreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nombreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(nombreLabel);
    }

    JLabel placaLabel = new JLabel("Placa: " + (placa != null ? placa : "N/A"));
    placaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    placaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(placaLabel);

    // *** MOSTRAR SI ES EMPADRONADO ***
    if (esEmpadronado) {
        JLabel empadronadoLabel = new JLabel("Empadronado: Si");
        empadronadoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        empadronadoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(empadronadoLabel);
    }

    JLabel lblEspacio = new JLabel("Espacio: E-" + String.format("%02d", espacio.getNumero()));
    lblEspacio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblEspacio.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(lblEspacio);

    panel.add(Box.createVerticalStrut(10));

    // *** SEPARADOR VISUAL ***
    JPanel separador = new JPanel();
    separador.setBackground(new Color(200, 200, 200));
    separador.setMaximumSize(new Dimension(250, 1));
    separador.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(separador);

    panel.add(Box.createVerticalStrut(10));

    // *** INFORMACIÓN DE TIEMPO ***
    JLabel horaLabel = new JLabel("Hora de Entrada: " + (horaEntrada != null ?
            horaEntrada.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "N/A"));
    horaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    horaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(horaLabel);

    if (ticket != null) {
        // *** VERIFICAR SI HAY FECHA DE SALIDA (EVITAR NULL POINTER) ***
        if (ticket.getSalida() != null) {
            JLabel salidaLabel = new JLabel("Hora de Salida: " + ticket.getSalida()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            salidaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            salidaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(salidaLabel);
        } else {
            JLabel estadoLabel = new JLabel("Hora de Salida: Aun estacionado");
            estadoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            estadoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(estadoLabel);
        }

        panel.add(Box.createVerticalStrut(10));

        // *** INFORMACIÓN DEL SERVICIO ***
        JLabel tipoLabel = new JLabel("Tipo de servicio: " + ticket.getTipoServicio().getDescripcion());
        tipoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tipoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(tipoLabel);

        // *** CALCULAR MORA ACTUAL ***
        double moraActual = 0.0;
        if (ticket.getTipoServicio() == Ticket.TipoServicio.TIEMPO_DEFINIDO) {
            JLabel horasLabel = new JLabel("Horas contratadas: " + ticket.getHorasContratadas());
            horasLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            horasLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(horasLabel);

            // *** CALCULAR TIEMPO RESTANTE ***
            long minutosTranscurridos = Duration.between(ticket.getEntrada(), LocalDateTime.now()).toMinutes();
            long minutosTotales = ticket.getHorasContratadas() * 60L;
            long minutosRestantes = minutosTotales - minutosTranscurridos;

            if (minutosRestantes > 0) {
                // *** TIEMPO AÚN DISPONIBLE ***
                JLabel tiempoRestanteLabel = new JLabel("Tiempo restante: " + (minutosRestantes / 60) + "h " +
                        (minutosRestantes % 60) + "m");
                tiempoRestanteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                tiempoRestanteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(tiempoRestanteLabel);
            } else {
                // *** TIEMPO VENCIDO - CALCULAR MORA ***
                JLabel tiempoRestanteLabel = new JLabel("Tiempo restante: Vencido");
                tiempoRestanteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                tiempoRestanteLabel.setForeground(new Color(202, 6, 6));
                tiempoRestanteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(tiempoRestanteLabel);

                // *** AGREGAR INFORMACIÓN DE TIEMPO EXCEDIDO ***
                long minutosExcedidos = Math.abs(minutosRestantes);
                JLabel tiempoExcedidoLabel = new JLabel("Tiempo por exceso: " + (minutosExcedidos / 60) + "h " +
                        (minutosExcedidos % 60) + "m");
                tiempoExcedidoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                tiempoExcedidoLabel.setForeground(new Color(202, 6, 6));
                tiempoExcedidoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(tiempoExcedidoLabel);

                // *** CALCULAR MORA ***
                long horasExcedidas = (minutosExcedidos + 59) / 60; // Redondear hacia arriba
                moraActual = horasExcedidas * Ticket.MORA_POR_HORA;

                JLabel moraLabel = new JLabel("Mora por exceso: S/" + String.format("%.2f", moraActual));
                moraLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                moraLabel.setForeground(new Color(202, 6, 6));
                moraLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(moraLabel);
            }
        } else {
            // *** TIEMPO INDEFINIDO ***
            JLabel indefLabel = new JLabel("Horas contratadas: Indefinido");
            indefLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            indefLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(indefLabel);

            JLabel transcurridoLabel = new JLabel("Tiempo transcurrido: " +
                    Duration.between(ticket.getEntrada(), LocalDateTime.now()).toHours() + "h");
            transcurridoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            transcurridoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(transcurridoLabel);
        }

        panel.add(Box.createVerticalStrut(15));

        // *** SEPARADOR VISUAL ***
        JPanel xd = new JPanel();
        xd.setBackground(new Color(200, 200, 200));
        xd.setMaximumSize(new Dimension(250, 1));
        xd.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(xd);

        panel.add(Box.createVerticalStrut(15));

        // *** INFORMACIÓN DE TARIFA SEGÚN TIPO DE USUARIO ***
        if (esEmpadronado && ticket.getMontoPagado() == 0.00) {
            // *** EMPADRONADO CON MEMBRESÍA GRATUITA ***
            if (moraActual > 0) {
                JLabel tarifaLabel = new JLabel("Tarifa base: GRATIS (Membresía)");
                tarifaLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                tarifaLabel.setForeground(new Color(46, 204, 113));
                tarifaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(tarifaLabel);

                JLabel totalLabel = new JLabel("Total a pagar: S/" + String.format("%.2f", moraActual) + " (Solo mora)");
                totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                totalLabel.setForeground(new Color(25, 118, 210));
                totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(totalLabel);
            } else {
                JLabel tarifaLabel = new JLabel("Tarifa: GRATIS (Membresía)");
                tarifaLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                tarifaLabel.setForeground(new Color(46, 204, 113));
                tarifaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(tarifaLabel);

                JLabel beneficioLabel = new JLabel("Beneficio de membresía aplicado");
                beneficioLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                beneficioLabel.setForeground(new Color(46, 204, 113));
                beneficioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(beneficioLabel);
            }
        } else {
            // *** CLIENTE REGULAR O EMPADRONADO CON TARIFA ***
            double tarifaBase = ticket.calcularTotal();
            double totalConMora = tarifaBase + moraActual;

            if (moraActual > 0) {
                JLabel tarifaLabel = new JLabel("Tarifa base: S/" + String.format("%.2f", tarifaBase));
                tarifaLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                tarifaLabel.setForeground(new Color(25, 118, 210));
                tarifaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(tarifaLabel);

                JLabel totalLabel = new JLabel("Total a pagar: S/" + String.format("%.2f", totalConMora) + " (Incluye mora)");
                totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                totalLabel.setForeground(new Color(25, 118, 210));
                totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(totalLabel);
            } else {
                JLabel tarifaLabel = new JLabel("Tarifa a pagar: S/" + String.format("%.2f", tarifaBase));
                tarifaLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                tarifaLabel.setForeground(new Color(25, 118, 210));
                tarifaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(tarifaLabel);
            }

            if (esEmpadronado) {
                JLabel notaLabel = new JLabel("Nota: Empadronado con tarifa especial");
                notaLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                notaLabel.setForeground(new Color(108, 117, 125));
                notaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(notaLabel);
            }
        }

        // *** MOSTRAR MONTO PAGADO SI ES DIFERENTE AL CALCULADO ***
        if (ticket.getMontoPagado() != ticket.calcularTotal()) {
            JLabel montoPagadoLabel = new JLabel("Monto pagado al ingresar: S/" + String.format("%.2f", ticket.getMontoPagado()));
            montoPagadoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            montoPagadoLabel.setForeground(new Color(108, 117, 125));
            montoPagadoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(montoPagadoLabel);
        }
    } else {
        JLabel noTicketLabel = new JLabel("No se encontró ticket asociado.");
        noTicketLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        noTicketLabel.setForeground(new Color(231, 76, 60));
        noTicketLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(noTicketLabel);
    }

    // *** INFORMACIÓN ADICIONAL DEL HORARIO ***
    panel.add(Box.createVerticalStrut(15));

    java.time.LocalTime ahora = java.time.LocalTime.now();
    boolean enHorarioEspecial = (ahora.isAfter(java.time.LocalTime.of(21, 59)) ||
            ahora.isBefore(java.time.LocalTime.of(6, 1)));

    if (enHorarioEspecial) {
        JLabel horarioLabel = new JLabel("Horario Especial (22:00 - 06:00)");
        horarioLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        horarioLabel.setForeground(new Color(108, 117, 125));
        horarioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(horarioLabel);
    } else {
        JLabel horarioLabel = new JLabel("Horario Normal (06:00 - 22:00)");
        horarioLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        horarioLabel.setForeground(new Color(108, 117, 125));
        horarioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(horarioLabel);
    }

    // Usar el nuevo modal decorado
    DialogUtils.showInfoPanel(this, panel, "Información del Vehículo");
}
    private void agregarEmpadronado() {
        if (!EmpadronadosPanel.validarLimiteEmpadronados(this)) {
            return; // No continuar si se alcanzó el límite
        }
        EmpadronadoDialog dialog = new EmpadronadoDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isConfirmado()) {
            cargarEmpadronados();
        }
    }

    private void editarEmpadronado() {
        int fila = tablaEmpadronados.getSelectedRow();
        if (fila == -1) {
            DialogUtils.showErrorDialog(this, "Seleccione un empadronado para editar.");
            return;
        }

        int id = (Integer) modeloEmpadronados.getValueAt(fila, 0);
        String placa = (String) modeloEmpadronados.getValueAt(fila, 1);
        String nombre = (String) modeloEmpadronados.getValueAt(fila, 2);

        Empadronado emp = new Empadronado(placa, nombre);
        EmpadronadoDialog dialog = new EmpadronadoDialog(this, emp);
        dialog.setVisible(true);

        if (dialog.isConfirmado()) {
            cargarEmpadronados();
        }
    }

    private void eliminarEmpadronado() {
        int fila = tablaEmpadronados.getSelectedRow();
        if (fila == -1) {
            DialogUtils.showErrorDialog(this, "Seleccione un empadronado para eliminar.");
            return;
        }

        int id = (Integer) modeloEmpadronados.getValueAt(fila, 0);
        String nombre = (String) modeloEmpadronados.getValueAt(fila, 2);

        int confirm = DialogUtils.showModernConfirmDialog(this,
                "¿Está seguro de eliminar a " + nombre + "?",
                "Confirmar Eliminación");

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = ConexionBD.getConexion();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM empadronados WHERE id = ?")) {

                ps.setInt(1, id);
                ps.executeUpdate();
                DialogUtils.showSuccessDialog(this, "Empadronado eliminado exitosamente.");
                cargarEmpadronados();

            } catch (SQLException e) {
                DialogUtils.showErrorDialog(this, "Error al eliminar: " + e.getMessage());
            }
        }
    }

private void verTicketSeleccionado() {
    int fila = tablaHistorial.getSelectedRow();
    if (fila == -1) {
        DialogUtils.showErrorDialog(this, "Seleccione un ticket para ver.");
        return;
    }

    int id = (Integer) modeloHistorial.getValueAt(fila, 0);
    
    // *** USAR EL NUEVO DIÁLOGO ***
    VerTicketDialog dialog = new VerTicketDialog(this, id);
    dialog.setVisible(true);
}
    // Métodos de estadísticas (CORREGIDOS PARA MYSQL)
    private double calcularIngresosDia() {
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT SUM(monto + IFNULL(mora, 0)) FROM tickets WHERE DATE(fecha_pago) = CURDATE()")) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private int contarTicketsHoy() {
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM tickets WHERE DATE(fecha_entrada) = CURDATE()")) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String calcularTiempoPromedio() {
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT AVG(TIMESTAMPDIFF(HOUR, fecha_entrada, fecha_salida)) " +
                     "FROM tickets WHERE fecha_salida IS NOT NULL AND DATE(fecha_entrada) = CURDATE()")) {

            if (rs.next()) {
                double horas = rs.getDouble(1);
                return String.format("%.1f", horas);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0.0";
    }

    private int contarEmpadronados() {
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM empadronados")) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}