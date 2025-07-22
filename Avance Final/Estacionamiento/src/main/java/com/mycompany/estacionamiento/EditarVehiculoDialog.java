package com.mycompany.estacionamiento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditarVehiculoDialog extends JDialog {
    private JTextField txtPlaca;
    private JTextField txtNombre;
    private JComboBox<String> cboTipoServicio;
    private JSpinner spinnerHoras;
    private boolean confirmado = false;
    
    private String nuevaPlaca;
    private String nuevoNombre;
    private String nuevoTipo;
    private int nuevasHoras;
    private String placaOriginal;

    public EditarVehiculoDialog(Window parent, String placa, String nombreActual, String tipoActual, int horasActuales) {
        super(parent, "Editar Vehículo - " + placa, ModalityType.APPLICATION_MODAL);
        
        this.placaOriginal = placa;
        this.nuevaPlaca = placa;
        this.nuevoNombre = nombreActual;
        this.nuevoTipo = tipoActual;
        this.nuevasHoras = horasActuales;
        
        initComponents(placa, nombreActual, tipoActual, horasActuales);
    }

    private void initComponents(String placaActual, String nombreActual, String tipoActual, int horasActuales) {
        setSize(450, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

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

        JLabel titulo = new JLabel("Editar Información del Vehículo", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        header.add(titulo, BorderLayout.CENTER);

        mainPanel.add(header, BorderLayout.NORTH);

        // Panel de formulario
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Campo Placa
        JLabel lblPlaca = new JLabel("Placa del vehículo:");
        lblPlaca.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPlaca.setForeground(new Color(25, 118, 210));
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(lblPlaca, gbc);

        txtPlaca = new JTextField(placaActual, 20);
        txtPlaca.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPlaca.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        // Convertir a mayúsculas automáticamente
        txtPlaca.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String text = txtPlaca.getText().toUpperCase();
                txtPlaca.setText(text);
            }
        });
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtPlaca, gbc);

        // Campo Nombre
        JLabel lblNombre = new JLabel("Nombre de la persona:");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNombre.setForeground(new Color(25, 118, 210));
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(lblNombre, gbc);

        txtNombre = new JTextField(nombreActual, 20);
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtNombre, gbc);

        // Campo Tipo de Servicio
        JLabel lblTipo = new JLabel("Tipo de Servicio:");
        lblTipo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTipo.setForeground(new Color(25, 118, 210));
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(lblTipo, gbc);

        String[] tiposServicio = {"Tiempo Definido", "Tiempo Indefinido"};
        cboTipoServicio = new JComboBox<>(tiposServicio);
        cboTipoServicio.setSelectedItem(tipoActual);
        cboTipoServicio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(cboTipoServicio, gbc);

        // Campo Horas
        JLabel lblHoras = new JLabel("Horas contratadas:");
        lblHoras.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHoras.setForeground(new Color(25, 118, 210));
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(lblHoras, gbc);

        // *** CORREGIDO: Manejar el caso cuando horasActuales = 0 ***
        int valorInicialHoras = (horasActuales <= 0) ? 1 : horasActuales;
        spinnerHoras = new JSpinner(new SpinnerNumberModel(valorInicialHoras, 1, 24, 1));
        spinnerHoras.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ((JSpinner.DefaultEditor) spinnerHoras.getEditor()).getTextField().setEditable(false);
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(spinnerHoras, gbc);

        // Lógica para habilitar/deshabilitar horas según el tipo
        cboTipoServicio.addActionListener(e -> {
            boolean esDefinido = cboTipoServicio.getSelectedItem().equals("Tiempo Definido");
            spinnerHoras.setEnabled(esDefinido);
            lblHoras.setEnabled(esDefinido);
            
            // *** NUEVO: Mostrar mensaje informativo para Tiempo Indefinido ***
            if (!esDefinido) {
                lblHoras.setText("Horas contratadas: (No aplica)");
            } else {
                lblHoras.setText("Horas contratadas:");
            }
        });

        // Configurar estado inicial
        boolean esDefinido = tipoActual.equals("Tiempo Definido");
        spinnerHoras.setEnabled(esDefinido);
        lblHoras.setEnabled(esDefinido);
        
        // *** NUEVO: Configurar texto inicial del label ***
        if (!esDefinido) {
            lblHoras.setText("Horas contratadas: (No aplica)");
        }

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        JButton btnGuardar = createStyledButton("Guardar Cambios", new Color(46, 204, 113));
        btnGuardar.addActionListener(e -> guardarCambios());

        JButton btnCancelar = createStyledButton("Cancelar", new Color(231, 76, 60));
        btnCancelar.addActionListener(e -> dispose());

        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void guardarCambios() {
        // Validar placa
        String placa = txtPlaca.getText().trim().toUpperCase();
        if (placa.isEmpty()) {
            DialogUtils.showErrorDialog(this, "La placa no puede estar vacía.");
            txtPlaca.requestFocus();
            return;
        }

        // Validar nombre
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            DialogUtils.showErrorDialog(this, "El nombre no puede estar vacío.");
            txtNombre.requestFocus();
            return;
        }

        // Validar si la nueva placa ya existe (solo si cambió)
        if (!placa.equals(placaOriginal)) {
            Estacionamiento estacionamiento = EstacionamientoSingleton.getInstance();
            if (estacionamiento.existeVehiculoEstacionado(placa)) {
                DialogUtils.showErrorDialog(this, "Ya existe un vehículo estacionado con la placa: " + placa);
                txtPlaca.requestFocus();
                return;
            }
        }

        // Obtener valores
        this.nuevaPlaca = placa;
        this.nuevoNombre = nombre;
        this.nuevoTipo = (String) cboTipoServicio.getSelectedItem();
        
        // *** CORREGIDO: Manejar horas según el tipo de servicio ***
        if (nuevoTipo.equals("Tiempo Indefinido")) {
            this.nuevasHoras = 0;  // Para tiempo indefinido, siempre 0
        } else {
            this.nuevasHoras = (Integer) spinnerHoras.getValue();  // Para tiempo definido, usar valor del spinner
        }

        this.confirmado = true;
        dispose();
    }

    // Getters
    public boolean isConfirmado() {
        return confirmado;
    }

    public String getNuevaPlaca() {
        return nuevaPlaca;
    }

    public String getNuevoNombre() {
        return nuevoNombre;
    }

    public String getNuevoTipo() {
        return nuevoTipo;
    }

    public int getNuevasHoras() {
        return nuevasHoras;
    }

    public String getPlacaOriginal() {
        return placaOriginal;
    }
}