package com.mycompany.estacionamiento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;


public class VentanaPrincipal extends JFrame {
    private List<AutoPanel> paneles = new ArrayList<>();
    private Estacionamiento estacionamiento;
    private JPanel panelEstacionamiento;
    private Espacio espacioSeleccionado;
    private JButton btnContinuar;
    private JButton btnAdmin;
    private JButton btnEmpadronados;
    private JLabel lblTotal, lblDisponibles, lblOcupados, lblIngresos;
    
    private static final String IMG_FONDO = "/imagenes/estacionamiento.jpg";
    private static final int[][] POSICIONES_ESPACIOS = {
        {240, 355}, {345, 355}, {450, 355}, {555, 355}, {660, 355},
        {765, 355}, {870, 355}, {980, 355}, {1080, 355}, {1185, 355},
        {240, 35}, {350, 35}, {455, 35}, {560, 35}, {660, 35},
        {770, 35}, {870, 35}, {980, 35}, {1080, 35}, {1190, 35}
    };
    private static final int AUTO_ANCHO = 90;
    private static final int AUTO_ALTO = 140;

    public VentanaPrincipal() {
        // Inicializar BD primero
        ConexionBD.inicializarBD();
        
        setTitle("Sistema de Estacionamiento");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        AutoPanel.setParentComponent(this);
        
        estacionamiento = new Estacionamiento();
        EstacionamientoSingleton.setInstance(estacionamiento);

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBackground(new Color(230, 240, 255));

        // Header con gradiente (SIN botón admin)
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
        header.setPreferredSize(new Dimension(1400, 90));
        header.setLayout(new BorderLayout());

        JLabel titulo = new JLabel("Sistema de Gestión de Estacionamiento", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.CENTER);

        panelPrincipal.add(header);

        // Panel de estadísticas
        JPanel panelEstadisticas = new JPanel(new GridLayout(1, 4, 20, 0));
        panelEstadisticas.setBackground(new Color(230, 240, 255));
        panelEstadisticas.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        lblTotal = new JLabel();
        lblDisponibles = new JLabel();
        lblOcupados = new JLabel();
        lblIngresos = new JLabel();

        panelEstadisticas.add(crearTarjetaEstadistica("Espacios Totales", lblTotal, null));
        panelEstadisticas.add(crearTarjetaEstadistica("Disponibles", lblDisponibles, null));
        panelEstadisticas.add(crearTarjetaEstadistica("Ocupados", lblOcupados, null));
        panelEstadisticas.add(crearTarjetaEstadistica("Ingresos del Día", lblIngresos, null));

        panelPrincipal.add(panelEstadisticas);

        // Panel de estacionamiento (CENTRADO, sin panel lateral)
        panelEstacionamiento = new FondoEstacionamientoPanel();
        panelEstacionamiento.setLayout(null); // Absolute positioning
        panelEstacionamiento.setPreferredSize(new Dimension(1350, 620));
        panelEstacionamiento.setBackground(new Color(230, 240, 255));
        panelEstacionamiento.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cargarEspacios();
        
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBackground(new Color(230, 240, 255));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        panelCentral.add(panelEstacionamiento, BorderLayout.CENTER);
        
        panelPrincipal.add(panelCentral);

        // PANEL INFERIOR con los 3 botones
        JPanel panelInferior = new JPanel(new FlowLayout());
        panelInferior.setBackground(new Color(230, 240, 255));

        // Botón Admin (IZQUIERDA)
        btnAdmin = new JButton("Admin");
        btnAdmin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnAdmin.setBackground(new Color(231, 76, 60));
        btnAdmin.setForeground(Color.WHITE);
        btnAdmin.setFocusPainted(false);
        btnAdmin.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btnAdmin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdmin.addActionListener(e -> mostrarPanelAdmin());

        // Espaciador para centrar
        Component espaciador1 = Box.createHorizontalStrut(300);

        // Botón Empadronados (CENTRO)
        btnEmpadronados = new JButton("Empadronados");
        btnEmpadronados.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnEmpadronados.setBackground(new Color(229, 216, 8));
        btnEmpadronados.setForeground(Color.WHITE);
        btnEmpadronados.setFocusPainted(false);
        btnEmpadronados.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btnEmpadronados.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEmpadronados.addActionListener(e -> mostrarListaEmpadronados());

        // Espaciador para centrar
        Component espaciador2 = Box.createHorizontalStrut(300);

        // Botón Continuar (DERECHA)
        btnContinuar = new JButton("Continuar");
        btnContinuar.setEnabled(false);
        btnContinuar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnContinuar.setBackground(new Color(25, 118, 210));
        btnContinuar.setForeground(Color.WHITE);
        btnContinuar.setFocusPainted(false);
        btnContinuar.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btnContinuar.addActionListener(e -> mostrarDialogoRegistro());

        // Agregar botones al panel inferior
        panelInferior.add(btnAdmin);
        panelInferior.add(espaciador1);
        panelInferior.add(btnEmpadronados);
        panelInferior.add(espaciador2);
        panelInferior.add(btnContinuar);

        panelPrincipal.add(panelInferior);

        setContentPane(panelPrincipal);
        actualizarEstadisticas();

        // === Timer para refrescar los AutoPanel cada segundo ===
        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            for (AutoPanel panel : paneles) {
                panel.actualizarPanel();
                panel.repaint();
            }
        });
        timer.start();
        // =======================================================
    }

    private JPanel crearTarjetaEstadistica(String titulo, JLabel lblValor, String icono) {
        JPanel tarjeta = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 118, 210), 
                    getWidth(), getHeight(), new Color(33, 150, 243));
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setOpaque(false);
        tarjeta.setPreferredSize(new Dimension(200, 90));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblIcono = new JLabel(icono);
        lblIcono.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        lblIcono.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblValor.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValor.setForeground(Color.WHITE);
        lblValor.setAlignmentX(Component.CENTER_ALIGNMENT);

        tarjeta.add(lblIcono);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 5)));
        tarjeta.add(lblTitulo);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 5)));
        tarjeta.add(lblValor);

        return tarjeta;
    }

    private void actualizarEstadisticas() {
        lblTotal.setText(String.valueOf(estacionamiento.getEspacios().size()));
        lblDisponibles.setText(String.valueOf(estacionamiento.getEspaciosDisponibles()));
        lblOcupados.setText(String.valueOf(estacionamiento.getEspaciosOcupados()));
        lblIngresos.setText(String.format("S/%.2f", estacionamiento.getIngresosDiarios()));
    }

    private void cargarEspacios() {
        panelEstacionamiento.removeAll();
        paneles.clear(); // Limpia la lista antes de volver a cargar
        AutoPanel.limpiarPaneles(); // ← AGREGAR ESTA LÍNEA AQUÍ

        int idx = 0;
        for (Espacio espacio : estacionamiento.getEspacios()) {
            if (idx >= POSICIONES_ESPACIOS.length) break;

            int x = POSICIONES_ESPACIOS[idx][0];
            int y = POSICIONES_ESPACIOS[idx][1];

            AutoPanel autoPanel = new AutoPanel(espacio, AUTO_ANCHO, AUTO_ALTO);
            autoPanel.setBounds(x, y, AUTO_ANCHO, AUTO_ALTO);
            autoPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && !espacio.isOcupado()) {
                        deseleccionarTodos();
                        espacioSeleccionado = espacio;
                        autoPanel.setSeleccionado(true);
                        btnContinuar.setEnabled(true);
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2 && espacio.isOcupado()) {
                        int confirm = DialogUtils.showModernConfirmDialog(
                            VentanaPrincipal.this,
                            "¿Deseas liberar este espacio?<br>Placa: <b>" + espacio.getPlaca() + "</b>",
                            "Liberar espacio"
                        );
                        if (confirm == JOptionPane.YES_OPTION) {
                            liberarVehiculo(espacio, autoPanel);
                        }
                    }
                    if (SwingUtilities.isRightMouseButton(e) && espacio.isOcupado()) {
                        mostrarInfoAuto(espacio);
                    }
                }
            });

            panelEstacionamiento.add(autoPanel);
            paneles.add(autoPanel); // Agrega el panel a la lista para el timer
            idx++;
        }

        panelEstacionamiento.revalidate();
        panelEstacionamiento.repaint();
        actualizarEstadisticas();
    }

    private void deseleccionarTodos() {
        for (Component c : panelEstacionamiento.getComponents()) {
            if (c instanceof AutoPanel) {
                ((AutoPanel) c).setSeleccionado(false);
            }
        }
    }

    // NUEVO: Mostrar lista de empadronados en diálogo
    private void mostrarListaEmpadronados() {
        EmpadronadosPanel empadronadosPanel = new EmpadronadosPanel();
        empadronadosPanel.setPreferredSize(new Dimension(400, 500));
        
        DialogUtils.showCustomDialog(this, empadronadosPanel, "Lista de Empadronados", null);
    }


    private void mostrarDialogoRegistro() {
        if (espacioSeleccionado == null) return;

        JDialog dialog = new JDialog(this, "Registro de Vehículo", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(420, 400); // Aumentado para el campo nombre
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);


        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(44, 115, 236));
        headerPanel.setPreferredSize(new Dimension(420, 38));

        JLabel lblTitulo = new JLabel("Registro de Vehículo");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(lblTitulo, BorderLayout.CENTER);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 120, 0, 0));

        JButton btnCerrar = new JButton("X");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setBackground(new Color(220, 53, 69));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setOpaque(true);
        btnCerrar.addActionListener(e -> dialog.dispose());

        headerPanel.add(lblTitulo, BorderLayout.WEST);
        headerPanel.add(btnCerrar, BorderLayout.EAST);


        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 245, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblPlaca = new JLabel("Placa:");
        lblPlaca.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblPlaca, gbc);

        JTextField txtPlaca = new JTextField();
        txtPlaca.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(txtPlaca, gbc);


        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(lblNombre, gbc);

        JTextField txtNombre = new JTextField();
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(txtNombre, gbc);

        JLabel lblTipo = new JLabel("Tipo de Servicio:");
        lblTipo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(lblTipo, gbc);

        JComboBox<String> cboTipo = new JComboBox<>(new String[]{"Tiempo Definido", "Tiempo Indefinido"});
        cboTipo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(cboTipo, gbc);

        JLabel lblHoras = new JLabel("Horas (si es definido):");
        lblHoras.setFont(new Font("Segoe UI", Font.BOLD, 15));
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(lblHoras, gbc);

        JSpinner spinnerHoras = new JSpinner(new SpinnerNumberModel(1, 1, 24, 1));
        spinnerHoras.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(spinnerHoras, gbc);

        JLabel lblMonto = new JLabel("Monto: S/0.00");
        lblMonto.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(lblMonto, gbc);

        JButton btnIngresar = new JButton("Ingresar Vehículo");
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnIngresar.setBackground(new Color(46, 204, 113));
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(btnIngresar, gbc);

        // Lógica para ocultar/mostrar horas y monto
        Runnable actualizarVisibilidad = () -> {
            boolean definido = cboTipo.getSelectedIndex() == 0;
            lblHoras.setVisible(definido);
            spinnerHoras.setVisible(definido);
            lblMonto.setVisible(definido);
        };

        // AQUI CORREGÍ LA LOGICA
        Runnable actualizarMonto = () -> {
            String placaActual = txtPlaca.getText().trim().toUpperCase();
            boolean esEmpadronado = false;

            if (!placaActual.isEmpty()) {
                esEmpadronado = EstacionamientoSingleton.getInstance().esEmpadronado(placaActual);
            }

            // Definimos el horario especial
            java.time.LocalTime ahora = java.time.LocalTime.now();
            boolean enHorarioEspecial = (ahora.isAfter(java.time.LocalTime.of(21, 59)) ||
                    ahora.isBefore(java.time.LocalTime.of(6, 1)));

            // *** AQUÍ ESTÁ EL CAMBIO CLAVE: Aplicar beneficio solo si es empadronado Y está en horario especial ***
            if (esEmpadronado && enHorarioEspecial) {
                lblMonto.setText("Monto: Gratis (Empadronado en Horario Especial)");
                lblMonto.setForeground(new Color(46, 204, 113)); // Color verde para indicar beneficio
            } else {
                // Si no cumple la condición, se calcula la tarifa normal para todos
                boolean definido = cboTipo.getSelectedIndex() == 0;
                int horas = (int) spinnerHoras.getValue();
                
                // *** USAR TARIFAS NOCTURNAS SI CORRESPONDE ***
                Ticket.TipoServicio tipo = definido ? Ticket.TipoServicio.TIEMPO_DEFINIDO : Ticket.TipoServicio.TIEMPO_INDEFINIDO;
                double monto = Ticket.calcularTotalActual(tipo, horas);
                
                // *** MOSTRAR INDICADOR DE TARIFA NOCTURNA ***
                boolean esNocturno = Ticket.esHorarioNocturnoActual();
                String indicadorHorario = esNocturno ? " (Tarifa Nocturna)" : "";
                
                lblMonto.setText("Monto: S/" + String.format("%.2f", monto) + indicadorHorario);
                lblMonto.setForeground(esNocturno ? new Color(155, 89, 182) : Color.BLACK); // Morado para nocturno
            }
        };

        txtPlaca.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                actualizarMonto.run();
            }
        });

        cboTipo.addActionListener(e -> {
            actualizarVisibilidad.run();
            actualizarMonto.run();
        });

        spinnerHoras.addChangeListener(e -> actualizarMonto.run());

        actualizarVisibilidad.run();
        actualizarMonto.run();

        btnIngresar.addActionListener(e -> {
            String placa = txtPlaca.getText().trim().toUpperCase();
            String nombrePersona = txtNombre.getText().trim();

            if (placa.isEmpty()) {
                DialogUtils.showErrorDialog(dialog, "Ingrese la placa.");
                return;
            }

            if (nombrePersona.isEmpty()) {
                DialogUtils.showErrorDialog(dialog, "Ingrese el nombre de la persona.");
                return;
            }

            if (estacionamiento.existeVehiculoEstacionado(placa)) {
                DialogUtils.showErrorDialog(dialog, "Ya existe un vehículo estacionado con esa placa.");
                return;
            }

            // --- LÓGICA DE HORARIO Y ESPACIOS (SE MANTIENE IGUAL) ---
            int[] ESPACIOS_RESERVADOS = {6, 7, 8, 9, 10};
            boolean reservado = false;
            for (int num : ESPACIOS_RESERVADOS) {
                if (espacioSeleccionado.getNumero() == num) {
                    reservado = true;
                    break;
                }
            }

            java.time.LocalTime ahora = java.time.LocalTime.now();
            boolean enHorarioEspecial = (ahora.isAfter(java.time.LocalTime.of(21, 59)) ||
                    ahora.isBefore(java.time.LocalTime.of(6, 1)));

            boolean esEmpadronado = EstacionamientoSingleton.getInstance().esEmpadronado(placa);

            if (reservado && enHorarioEspecial && !esEmpadronado) {
                DialogUtils.showErrorDialog(dialog, "Solo empadronados pueden ocupar este espacio en este horario.");
                return;
            }
            // -------------------------------------------------------------------------

            boolean definido = cboTipo.getSelectedIndex() == 0;
            int horas = (int) spinnerHoras.getValue();

            Ticket.TipoServicio tipo = definido ? Ticket.TipoServicio.TIEMPO_DEFINIDO :
                    Ticket.TipoServicio.TIEMPO_INDEFINIDO;

            Ticket ticket = new Ticket(placa, nombrePersona, tipo, horas, espacioSeleccionado.getNumero());

            // *** LÓGICA DE MONTO CORREGIDA AL MOMENTO DE GUARDAR ***
            double monto;
            if (esEmpadronado && enHorarioEspecial) {
                monto = 0.00;
                DialogUtils.showSuccessDialog(dialog,
                        "¡Ingreso de Empadronado con beneficio aplicado!");
            } else {
                monto = ticket.calcularTotal();
            }

            ticket.registrarPago(monto);

            estacionamiento.ocuparEspacio(espacioSeleccionado, placa);
            estacionamiento.guardarTicket(ticket, nombrePersona);

            cargarEspacios();
            btnContinuar.setEnabled(false);
            mostrarTicketFinal(ticket);
            dialog.dispose();
            actualizarEstadisticas();
        });

        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void mostrarTicketFinal(Ticket ticket) {
        DialogUtils.showTicketDialogMejorado(this, ticket);
    }

public void mostrarInfoAuto(Espacio espacio) {
    String placa = espacio.getPlaca();
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

    // Usa el nuevo modal decorado
    DialogUtils.showInfoPanel(this, panel, "Información del Vehículo");
}
private void liberarVehiculo(Espacio espacio, AutoPanel autoPanel) {
    String placa = espacio.getPlaca();
    Ticket ticket = estacionamiento.buscarTicketPorPlaca(placa);

    if (ticket == null) {
        DialogUtils.showErrorDialog(this, "No se encontró ticket asociado.");
        return;
    }

    ticket.registrarSalida();

    // === LÓGICA DE BENEFICIO EMPADRONADO EN HORARIO ESPECIAL ===
    boolean esEmpadronadoSalida = EstacionamientoSingleton.getInstance().esEmpadronado(placa);
    java.time.LocalTime ahora = java.time.LocalTime.now();
    boolean enHorarioEspecial = (ahora.isAfter(java.time.LocalTime.of(21, 59)) || ahora.isBefore(java.time.LocalTime.of(6, 1)));

    // Si es empadronado y está en horario especial, el monto base es 0
    if (esEmpadronadoSalida && enHorarioEspecial) {
        ticket.registrarPago(0.00);
    }

    double mora = ticket.calcularMora();

    // *** VERIFICAR SI ES EMPADRONADO ***
    boolean esEmpadronado = EstacionamientoSingleton.getInstance().esEmpadronado(placa);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(new Color(240, 245, 255));
    panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

    // *** TÍTULO CENTRADO Y MEJORADO ***
    JLabel titulo = new JLabel("Resumen de Salida del Vehículo");
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
    titulo.setForeground(new Color(25, 118, 210));
    titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(titulo);

    panel.add(Box.createVerticalStrut(15));

    // *** INFORMACIÓN DEL VEHÍCULO ***
    JLabel lblPlaca = new JLabel("Placa: " + ticket.getPlaca());
    lblPlaca.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblPlaca.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(lblPlaca);

    // *** NOMBRE DE LA PERSONA ***
    JLabel lblNombre = new JLabel("Nombre: " + (ticket.getNombrePersona() != null ? ticket.getNombrePersona() : "No especificado"));
    lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(lblNombre);

    // *** INFORMACIÓN DEL ESPACIO ***
    JLabel lblEspacio = new JLabel("Espacio: E-" + String.format("%02d", espacio.getNumero()));
    lblEspacio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblEspacio.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(lblEspacio);

    panel.add(Box.createVerticalStrut(10));

    // *** INFORMACIÓN DEL SERVICIO ***
    JLabel lblTipo = new JLabel("Tipo de Servicio: " + ticket.getTipoServicio().getDescripcion());
    lblTipo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblTipo.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(lblTipo);

    JLabel lblEntrada = new JLabel("Hora de Entrada: " + ticket.getEntrada()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    lblEntrada.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblEntrada.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(lblEntrada);

    JLabel lblSalida = new JLabel("Hora de Salida: " + ticket.getSalida()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    lblSalida.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblSalida.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(lblSalida);

    if (ticket.getTipoServicio() == Ticket.TipoServicio.TIEMPO_DEFINIDO) {
        JLabel lblHoras = new JLabel("Horas Contratadas: " + ticket.getHorasContratadas());
        lblHoras.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblHoras.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblHoras);
    }
    
    panel.add(Box.createVerticalStrut(10));
    // *** INFORMACIÓN DE PAGO ***
    JLabel lblMontoInicial;
    if (esEmpadronadoSalida && enHorarioEspecial) {
        lblMontoInicial = new JLabel("Monto Base: Libre (Membresía)");
    } else {
        lblMontoInicial = new JLabel("Monto Base: S/" + String.format("%.2f", ticket.getMontoPagado()));
    }
    lblMontoInicial.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    lblMontoInicial.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(lblMontoInicial);

    if (mora > 0) {
        JLabel lblMora = new JLabel("Mora por Exceso: S/" + String.format("%.2f", mora));
        lblMora.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMora.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblMora);

        JLabel lblTiempoExcedido = new JLabel("Tiempo Excedido: " + ticket.getMinutosExcedidos() + " minutos");
        lblTiempoExcedido.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTiempoExcedido.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTiempoExcedido);

        // *** TOTAL CON MORA ***
        panel.add(Box.createVerticalStrut(10));
        double totalAPagar = ticket.getMontoPagado() + mora;
        JLabel lblTotal = new JLabel("Tarifa Total: S/" + String.format("%.2f", totalAPagar));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(new Color(25, 118, 210));
        lblTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lblTotal);
        // *** NOTA ESPECIAL PARA EMPADRONADOS CON MORA ***
        if (esEmpadronadoSalida && enHorarioEspecial) {
            JLabel lblNotaEmpadronado = new JLabel("ℹ️ Nota: Solo se cobra la mora por tiempo excedido");
            lblNotaEmpadronado.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblNotaEmpadronado.setForeground(new Color(108, 117, 125));
            lblNotaEmpadronado.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(lblNotaEmpadronado);
        }
    } else {
        // *** TOTAL SIN MORA ***
        panel.add(Box.createVerticalStrut(5));
        if (esEmpadronadoSalida && enHorarioEspecial) {
            JLabel lblTotal = new JLabel("Tarifa Total: Empadronado");
            lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTotal.setForeground(new Color(46, 204, 113));
            lblTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(lblTotal);
            JLabel lblNotaGratis = new JLabel("Beneficio de membresía aplicado");
            lblNotaGratis.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblNotaGratis.setForeground(new Color(46, 204, 113));
            lblNotaGratis.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(lblNotaGratis);
        } else {
            JLabel lblTotal = new JLabel("Tarifa Total: S/" + String.format("%.2f", ticket.getMontoPagado()));
            lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblTotal.setForeground(new Color(46, 204, 113));
            lblTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(lblTotal);
        }
    }

    panel.add(Box.createVerticalStrut(20));

    // *** BOTONES DE ACCIÓN ***
    JPanel panelBotones = new JPanel(new FlowLayout());
    panelBotones.setOpaque(false);

    // *** BOTÓN PAGAR PERSONALIZADO SEGÚN TIPO DE USUARIO ***
    JButton btnPagar;
    if (esEmpadronadoSalida && ticket.getMontoPagado() == 0.00 && mora == 0) {
        btnPagar = new JButton("Retirar Vehículo");
    } else {
        btnPagar = new JButton("Pagar y Retirar Vehículo");
    }
    
    btnPagar.setBackground(new Color(46, 204, 113));
    btnPagar.setForeground(Color.WHITE);
    btnPagar.setFocusPainted(false);
    btnPagar.setFont(new Font("Segoe UI", Font.BOLD, 16));
    btnPagar.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
    btnPagar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    JButton btnImprimir = new JButton("Imprimir Ticket");
    btnImprimir.setBackground(new Color(52, 152, 219));
    btnImprimir.setForeground(Color.WHITE);
    btnImprimir.setFocusPainted(false);
    btnImprimir.setFont(new Font("Segoe UI", Font.BOLD, 14));
    btnImprimir.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    btnImprimir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    panelBotones.add(btnPagar);
    panel.add(panelBotones);

    // *** CREAR DIÁLOGO ***
    JDialog dialog = new JDialog(this, "Retiro de Vehículo", true);
    dialog.setUndecorated(true); // ← MOVER AQUÍ (ANTES DE pack())
    dialog.setContentPane(panel);
    dialog.pack();
    dialog.setLocationRelativeTo(this);

    // *** ACCIÓN DEL BOTÓN PAGAR ***
    btnPagar.addActionListener(e -> {
        ticket.registrarMora(mora);

        // CORREGIDO: Llama a estacionamiento.liberarEspacio(espacio) para actualizar base de datos y logs
        System.out.println("[LOG] Liberando espacio " + espacio.getNumero() + 
                          " - Placa: " + espacio.getPlaca() + 
                          " - Persona: " + ticket.getNombrePersona() +
                          " - Empadronado: " + (esEmpadronadoSalida ? "SÍ" : "NO"));

        estacionamiento.liberarEspacio(espacio);
        cargarEspacios();
        actualizarEstadisticas();
        dialog.dispose();

        mostrarTicketFinal(ticket);
        
        // *** MENSAJE PERSONALIZADO SEGÚN TIPO DE USUARIO ***
        String mensajeExito;
        if (esEmpadronadoSalida && ticket.getMontoPagado() == 0.00 && mora == 0) {
            mensajeExito = "¡Vehículo retirado exitosamente!";
        } else if (esEmpadronadoSalida && mora > 0) {
            mensajeExito = "¡Vehículo retirado exitosamente!";
        } else {
            mensajeExito = "¡Vehículo retirado exitosamente!";
        }
        
        DialogUtils.showSuccessDialog(this, mensajeExito);
    });

    // *** ACCIÓN DEL BOTÓN IMPRIMIR ***
    btnImprimir.addActionListener(e -> {
        PDFGenerator.generarYAbrirTicketPDF(ticket);
    });

    dialog.setVisible(true);
}
private void mostrarPanelAdmin() {
    LoginDialog loginDialog = new LoginDialog(this);
    loginDialog.setVisible(true);
    
    if (loginDialog.isLoginExitoso()) { // ← CORREGIDO: isLoginExitoso()
        AdminPanel adminPanel = new AdminPanel(this);
        adminPanel.setVisible(true);
    }
}

    private class FondoEstacionamientoPanel extends JPanel {
        private Image fondo;

        public FondoEstacionamientoPanel() {
            try {
                fondo = new ImageIcon(getClass().getResource(IMG_FONDO)).getImage();
            } catch (Exception e) {
                fondo = null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (fondo != null) {
                g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
            ventana.setExtendedState(JFrame.MAXIMIZED_BOTH);
        });
    }
}

