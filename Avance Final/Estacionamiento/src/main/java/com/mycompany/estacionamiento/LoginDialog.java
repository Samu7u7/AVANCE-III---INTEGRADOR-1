package com.mycompany.estacionamiento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDialog extends JDialog {
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private boolean loginExitoso = false;
    private boolean passwordPlaceholderActive = true; // ← Control de placeholder

    public LoginDialog(Frame parent) {
        super(parent, "Iniciar Sesión", true);
        initComponents();
        setupDialog();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setUndecorated(true);

        // *** PANEL PRINCIPAL CON FONDO AZUL CLARO ***
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo azul claro como en la imagen
                g2.setColor(new Color(220, 235, 250));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                // Borde sutil
                g2.setColor(new Color(200, 220, 240));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 18, 18);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        mainPanel.setOpaque(false);

        // *** PANEL CENTRAL ***
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // *** CÍRCULO CON ICONO DE USUARIO ***
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 80;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                // Círculo blanco de fondo
                g2.setColor(Color.WHITE);
                g2.fillOval(x, y, size, size);
                // Borde azul
                g2.setColor(new Color(41, 128, 185));
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(x, y, size, size);
                // Icono de usuario
                g2.setColor(new Color(41, 128, 185));
                // Cabeza del usuario
                int headSize = 20;
                int headX = x + (size - headSize) / 2;
                int headY = y + 15;
                g2.fillOval(headX, headY, headSize, headSize);
                // Cuerpo del usuario
                int bodyWidth = 35;
                int bodyHeight = 25;
                int bodyX = x + (size - bodyWidth) / 2;
                int bodyY = y + 40;
                g2.fillRoundRect(bodyX, bodyY, bodyWidth, bodyHeight, 20, 20);
            }
        };
        iconPanel.setPreferredSize(new Dimension(100, 100));
        iconPanel.setOpaque(false);
        centerPanel.add(iconPanel);
        centerPanel.add(Box.createVerticalStrut(15));

        // *** CAMPO USERNAME - CORREGIDO ***
        JPanel usernamePanel = createStyledField("👤", "Usuario");
        txtUsuario = (JTextField) usernamePanel.getComponent(1);
        centerPanel.add(usernamePanel);
        centerPanel.add(Box.createVerticalStrut(15));

        // *** CAMPO PASSWORD CON PLACEHOLDER VISIBLE ***
        JPanel passwordPanel = createStyledPasswordField("🔒", "Contraseña");
        txtPassword = (JPasswordField) passwordPanel.getComponent(1);
        centerPanel.add(passwordPanel);
        centerPanel.add(Box.createVerticalStrut(30));

        // *** PANEL DE BOTONES LADO A LADO ***
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(300, 50));

        // *** BOTÓN INGRESAR ***
        JButton btnIngresar = new JButton("INGRESAR") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Color de fondo según estado
                Color bgColor;
                if (getModel().isPressed()) {
                    bgColor = new Color(31, 108, 165);
                } else if (getModel().isRollover()) {
                    bgColor = new Color(51, 138, 195);
                } else {
                    bgColor = new Color(41, 128, 185);
                }
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Texto blanco centrado
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }
        };
        btnIngresar.setPreferredSize(new Dimension(140, 45));
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setContentAreaFilled(false);
        btnIngresar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // *** BOTÓN CERRAR EN ROJO ***
        JButton btnCerrar = new JButton("CERRAR") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Color ROJO según estado
                Color bgColor;
                if (getModel().isPressed()) {
                    bgColor = new Color(180, 40, 40);
                } else if (getModel().isRollover()) {
                    bgColor = new Color(220, 60, 60);
                } else {
                    bgColor = new Color(200, 50, 50);
                }
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Borde rojo más oscuro
                g2.setColor(new Color(150, 30, 30));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                // Texto BLANCO centrado
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }
        };
        btnCerrar.setPreferredSize(new Dimension(140, 45));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setContentAreaFilled(false);
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        buttonPanel.add(btnIngresar);
        buttonPanel.add(btnCerrar);
        centerPanel.add(buttonPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // *** EVENTOS ***
        btnIngresar.addActionListener(e -> validarLogin());
        btnCerrar.addActionListener(e -> {
            loginExitoso = false;
            dispose();
        });

        // Enter para login
        KeyListener enterListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    validarLogin();
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        };

        txtUsuario.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        // ESC para cerrar
        getRootPane().registerKeyboardAction(
            e -> {
                loginExitoso = false;
                dispose();
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    // *** MÉTODO USUARIO CORREGIDO - MISMO TAMAÑO QUE PASSWORD ***
    private JPanel createStyledField(String icon, String placeholder) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(300, 45));

        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo blanco
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Borde azul
                g2.setColor(new Color(41, 128, 185));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 8, 8);
                super.paintComponent(g);
            }
        };

        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // *** CORREGIDO: MISMO PADDING QUE PASSWORD (15 en lugar de 50) ***
        field.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 15));
        field.setOpaque(false);

        // Placeholder effect
        field.setText(placeholder);
        field.setForeground(new Color(41, 128, 185));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(41, 128, 185));
                }
            }
        });

        // *** ICONO IZQUIERDO - VISIBLE ***
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5));

        // *** ESPACIADOR DERECHO PARA IGUALAR TAMAÑO CON PASSWORD ***
        JLabel lblSpacer = new JLabel("");
        lblSpacer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 15)); // ← MISMO ESPACIO QUE EL OJO

        panel.add(lblIcon, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        panel.add(lblSpacer, BorderLayout.EAST); // ← ESPACIADOR INVISIBLE

        return panel;
    }

    private JPanel createStyledPasswordField(String icon, String placeholder) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(300, 45));

        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo blanco
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Borde azul
                g2.setColor(new Color(41, 128, 185));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 8, 8);
                super.paintComponent(g);
            }
        };

        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 15));
        field.setOpaque(false);

        // *** PLACEHOLDER VISIBLE PARA PASSWORD ***
        field.setEchoChar('\0'); // ← INICIALMENTE SIN OCULTAR
        field.setText(placeholder);
        field.setForeground(new Color(41, 128, 185));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (passwordPlaceholderActive) {
                    field.setText("");
                    field.setEchoChar('●'); // ← ACTIVAR OCULTACIÓN
                    field.setForeground(Color.BLACK);
                    passwordPlaceholderActive = false;
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar('\0'); // ← DESACTIVAR OCULTACIÓN
                    field.setText(placeholder);
                    field.setForeground(new Color(41, 128, 185));
                    passwordPlaceholderActive = true;
                }
            }
        });

        // *** ICONO IZQUIERDO - VISIBLE ***
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lblIcon.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 5));

        // *** ICONO DERECHO (OJO) - VISIBLE ***
        JLabel lblEye = new JLabel("👁");
        lblEye.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblEye.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 15));
        lblEye.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Toggle para mostrar/ocultar contraseña
        lblEye.addMouseListener(new java.awt.event.MouseAdapter() {
            boolean visible = false;
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (!passwordPlaceholderActive) { // ← SOLO SI NO ES PLACEHOLDER
                    visible = !visible;
                    field.setEchoChar(visible ? '\0' : '●');
                    lblEye.setText(visible ? "🙈" : "👁");
                }
            }
        });

        panel.add(lblIcon, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        panel.add(lblEye, BorderLayout.EAST);

        return panel;
    }

    private void setupDialog() {
        setSize(400, 450);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Hacer que el diálogo sea arrastrable
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                Point point = evt.getLocationOnScreen();
                setLocation(point.x - evt.getX(), point.y - evt.getY());
            }
        });
    }

    private void validarLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        // *** VALIDACIÓN CORREGIDA ***
        if (usuario.equals("Usuario")) {
            usuario = "";
        }
        if (passwordPlaceholderActive || password.equals("Contraseña")) {
            password = "";
        }

        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor, complete todos los campos.");
            return;
        }

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM usuarios_admin WHERE usuario = ? AND password = ?")) {

            ps.setString(1, usuario);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    loginExitoso = true;
                    dispose();
                } else {
                    mostrarError("Usuario o contraseña incorrectos.");
                    // *** RESETEAR PASSWORD FIELD ***
                    txtPassword.setEchoChar('\0');
                    txtPassword.setText("Contraseña");
                    txtPassword.setForeground(new Color(41, 128, 185));
                    passwordPlaceholderActive = true;
                    txtUsuario.requestFocus();
                }
            }
        } catch (SQLException e) {
            mostrarError("Error de conexión: " + e.getMessage());
            System.out.println("[ERROR] Error en login: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Error de Autenticación",
            JOptionPane.ERROR_MESSAGE
        );
    }

    public boolean isLoginExitoso() {
        return loginExitoso;
    }

    // *** MÉTODO ESTÁTICO PARA USAR FÁCILMENTE ***
    public static boolean mostrarLogin(Frame parent) {
        LoginDialog dialog = new LoginDialog(parent);
        dialog.setVisible(true);
        return dialog.isLoginExitoso();
    }
}