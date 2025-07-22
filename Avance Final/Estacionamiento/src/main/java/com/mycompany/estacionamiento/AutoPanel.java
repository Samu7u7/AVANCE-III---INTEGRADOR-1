package com.mycompany.estacionamiento;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.Duration;

public class AutoPanel extends JPanel {

    private Espacio espacio;
    private boolean seleccionado = false;
    private JLabel iconoLabel;
    private JLabel placaLabel;
    private JLabel estadoLabel;

    // Rutas de las imágenes del carro
    private static final String IMG_DISPONIBLE = "/imagenes/verde.png";
    private static final String IMG_OCUPADO = "/imagenes/rojo.png";
    private static final String IMG_SELECCIONADO = "/imagenes/azul.png";
    private static final String IMG_RESERVADO_LIBRE = "/imagenes/amarillo.png";
    private static final String IMG_RESERVADO_OCUPADO_EMP = "/imagenes/naranja.png";
    private static final String IMG_RESERVADO_OCUPADO_NOEMP = "/imagenes/rojo.png";
    private static final String IMG_MORA = "/imagenes/morado.png";

    private int ancho;
    private int alto;

    // *** VARIABLES ESTÁTICAS PARA EL MENSAJE DE ESTACIONAMIENTO LLENO ***
    private static List<AutoPanel> todosLosPaneles = new ArrayList<>();
    private static Component parentComponent = null;
    private static boolean mensajeMostrado = false;
    private static long ultimaLimpieza = 0; // *** NUEVO: Para evitar verificación inmediata ***

    // Definir los espacios reservados (puedes ajustar esto según tu sistema)
    private static final int[] ESPACIOS_RESERVADOS = {6, 7, 8, 9, 10};

    public AutoPanel(Espacio espacio, int ancho, int alto) {
        if (espacio == null) {
            throw new IllegalArgumentException("El objeto Espacio no puede ser null");
        }

        this.espacio = espacio;
        this.ancho = ancho;
        this.alto = alto;
        setOpaque(false);
        setPreferredSize(new Dimension(ancho, alto));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // *** AGREGAR ESTE PANEL A LA LISTA ESTÁTICA ***
        todosLosPaneles.add(this);
        System.out.println("[DEBUG] Panel agregado. Total paneles: " + todosLosPaneles.size() + " - Espacio: " + espacio.getNumero());

        // Inicializar labels ANTES de llamar a actualizarPanel()
        placaLabel = new JLabel();
        placaLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        placaLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        placaLabel.setOpaque(false);
        placaLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        iconoLabel = new JLabel();
        iconoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconoLabel.setVerticalAlignment(SwingConstants.CENTER);
        iconoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        estadoLabel = new JLabel();
        estadoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        estadoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        estadoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        estadoLabel.setOpaque(false);

        add(placaLabel);
        add(Box.createRigidArea(new Dimension(0, 2)));
        add(iconoLabel);
        add(Box.createVerticalGlue());
        add(estadoLabel);

        // Ahora sí, después de inicializar los labels
        actualizarPanel();
    }

    // *** MÉTODOS ESTÁTICOS PARA EL MENSAJE DE ESTACIONAMIENTO LLENO ***
    public static void setParentComponent(Component parent) {
        parentComponent = parent;
        System.out.println("[DEBUG] Parent component establecido: " + (parent != null ? parent.getClass().getSimpleName() : "null"));
    }

    private static int contarEspaciosOcupados() {
        if (todosLosPaneles.isEmpty()) {
            return 0;
        }

        int ocupados = 0;
        for (AutoPanel panel : todosLosPaneles) {
            if (panel.getEspacio().isOcupado()) {
                ocupados++;
            }
        }
        return ocupados;
    }

    private static boolean todosLosEspaciosOcupados() {
        if (todosLosPaneles.isEmpty()) {
            System.out.println("[DEBUG] Lista de paneles vacía");
            return false;
        }

        int ocupados = contarEspaciosOcupados();
        int total = todosLosPaneles.size();
        
        boolean todosOcupados = (ocupados == total);
        System.out.println("[DEBUG] Espacios ocupados: " + ocupados + "/" + total + " - Todos ocupados: " + todosOcupados);
        
        return todosOcupados;
    }

    // *** MÉTODO MEJORADO: Verificar si se acaba de llenar el estacionamiento ***
    public static void verificarEstacionamientoLleno() {
        if (todosLosPaneles.isEmpty()) {
            return;
        }

        // *** NUEVO: No verificar inmediatamente después de limpiar paneles ***
        long tiempoActual = System.currentTimeMillis();
        if (tiempoActual - ultimaLimpieza < 2000) { // Esperar 2 segundos después de limpiar
            System.out.println("[DEBUG] Saltando verificación - muy pronto después de limpiar paneles");
            return;
        }

        int ocupados = contarEspaciosOcupados();
        int total = todosLosPaneles.size();
        
        System.out.println("[DEBUG] Verificando estacionamiento - Ocupados: " + ocupados + "/" + total + " - Mensaje mostrado: " + mensajeMostrado);
        
        // Si todos están ocupados y no se ha mostrado el mensaje
        if (ocupados == total && !mensajeMostrado) {
            System.out.println("[DEBUG] ¡ESTACIONAMIENTO LLENO! Mostrando mensaje...");
            mensajeMostrado = true;
            mostrarMensajeEstacionamientoLleno();
        }
        
        // Si se liberó un espacio, resetear el mensaje
        if (ocupados < total && mensajeMostrado) {
            System.out.println("[DEBUG] Se liberó un espacio, reseteando mensaje");
            mensajeMostrado = false;
        }
    }

    private static void mostrarMensajeEstacionamientoLleno() {
        System.out.println("[DEBUG] Intentando mostrar mensaje de estacionamiento lleno...");
        SwingUtilities.invokeLater(() -> {
            System.out.println("[DEBUG] Mostrando diálogo de estacionamiento lleno");
            JOptionPane.showMessageDialog(
                parentComponent,
                "¡ESTACIONAMIENTO COMPLETO!\n\nTodos los espacios están ocupados.\nPor favor, espere a que se libere un espacio.",
                "Estacionamiento Lleno",
                JOptionPane.WARNING_MESSAGE
            );
        });
    }

    public static void resetearMensaje() {
        mensajeMostrado = false;
        System.out.println("[DEBUG] Mensaje reseteado - puede volver a mostrarse");
    }

    public static void limpiarPaneles() {
        int cantidadAnterior = todosLosPaneles.size();
        todosLosPaneles.clear();
        mensajeMostrado = false;
        ultimaLimpieza = System.currentTimeMillis(); // *** NUEVO: Registrar cuándo se limpiaron los paneles ***
        System.out.println("[DEBUG] Paneles limpiados. Cantidad anterior: " + cantidadAnterior + " - Mensaje reseteado - Timestamp: " + ultimaLimpieza);
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
        actualizarPanel();
        repaint();
    }

    private boolean esReservado() {
        int num = espacio.getNumero();
        for (int reservado : ESPACIOS_RESERVADOS) {
            if (num == reservado) return true;
        }
        return false;
    }

    // Consulta si la placa es empadronada (robusto)
    public boolean esEmpadronado(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            System.out.println("[LOG] Placa vacía o nula - NO es empadronado");
            return false;
        }

        String placaLimpia = placa.trim().toUpperCase();
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM empadronados WHERE UPPER(TRIM(placa)) = ?")) {

            stmt.setString(1, placaLimpia);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean esEmpadronado = rs.getInt(1) > 0;
                System.out.println("[LOG] Placa " + placaLimpia + " ¿Es empadronado? " + esEmpadronado);
                return esEmpadronado;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar empadronado: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[LOG] Error en consulta - NO es empadronado por defecto");
        return false;
    }

    // *** NUEVO: Método para verificar si un auto tiene mora ***
    public boolean tieneAutoMora(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            System.out.println("[LOG MORA] Placa vacía o nula");
            return false;
        }

        String placaLimpia = placa.trim().toUpperCase();
        System.out.println("[LOG MORA] Verificando mora para placa: " + placaLimpia);
        
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT tipo_servicio, horas, fecha_entrada FROM tickets " +
                 "WHERE UPPER(TRIM(placa)) = ? AND fecha_salida IS NULL ORDER BY fecha_entrada DESC LIMIT 1")) {

            stmt.setString(1, placaLimpia);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String tipoServicio = rs.getString("tipo_servicio");
                int horasContratadas = rs.getInt("horas");
                LocalDateTime entrada = rs.getTimestamp("fecha_entrada").toLocalDateTime();

                System.out.println("[LOG MORA] Ticket encontrado - Tipo: " + tipoServicio + 
                                 ", Horas: " + horasContratadas + ", Entrada: " + entrada);

                // Solo verificar mora para servicio de tiempo definido
                if ("Tiempo Definido".equals(tipoServicio) || "TIEMPO_DEFINIDO".equals(tipoServicio)) {
                    LocalDateTime ahora = LocalDateTime.now();
                    long minutosTranscurridos = Duration.between(entrada, ahora).toMinutes();
                    long minutosContratados = horasContratadas * 60L;

                    boolean tieneMora = minutosTranscurridos > minutosContratados;
                    System.out.println("[LOG MORA] Placa " + placaLimpia + " - Minutos transcurridos: " + 
                                     minutosTranscurridos + ", Contratados: " + minutosContratados + 
                                     ", ¿Tiene mora? " + tieneMora);
                    return tieneMora;
                } else {
                    System.out.println("[LOG MORA] Tipo de servicio no es TIEMPO_DEFINIDO: " + tipoServicio);
                }
            } else {
                System.out.println("[LOG MORA] No se encontró ticket activo para placa: " + placaLimpia);
            }

        } catch (SQLException e) {
            System.err.println("[ERROR MORA] Error al verificar mora: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[LOG MORA] Retornando false para placa: " + placaLimpia);
        return false;
    }

    @Override
    public void repaint() {
        super.repaint();
    }

    public void actualizarPanel() {
        if (espacio == null || placaLabel == null || iconoLabel == null || estadoLabel == null) {
            // Protección extra, aunque el constructor ya lo valida
            return;
        }

        // Placa arriba
        if (espacio.isOcupado() && espacio.getPlaca() != null) {
            placaLabel.setText(espacio.getPlaca());
            placaLabel.setForeground(new Color(255, 215, 0)); // Amarillo
        } else {
            placaLabel.setText(" ");
            placaLabel.setForeground(new Color(255, 215, 0));
        }

        // Imagen y estado
        String rutaCarro;
        String estadoTexto;
        Color colorTextoEstado;

        boolean reservado = esReservado();
        boolean ocupado = espacio.isOcupado();
        boolean empadronado = ocupado && esEmpadronado(espacio.getPlaca());
        // *** NUEVO: Verificar si el auto tiene mora ***
        boolean tieneMora = ocupado && tieneAutoMora(espacio.getPlaca());

        System.out.println("[LOG PANEL] Espacio " + espacio.getNumero() + " - Ocupado: " + ocupado + 
                          ", Tiene mora: " + tieneMora + ", Placa: " + espacio.getPlaca());

        // Obtener hora actual
        java.time.LocalTime ahora = java.time.LocalTime.now();
        boolean enHorarioEspecial = (ahora.isAfter(java.time.LocalTime.of(21, 59)) ||
                ahora.isBefore(java.time.LocalTime.of(6, 1))); // 22:00 a 06:00

        if (seleccionado) {
            rutaCarro = IMG_SELECCIONADO;
            estadoTexto = "Seleccionado";
            colorTextoEstado = new Color(52, 152, 219);
            System.out.println("[LOG PANEL] Espacio " + espacio.getNumero() + " - SELECCIONADO");
        } else if (ocupado && tieneMora) {
            // *** NUEVA LÓGICA: Si el auto tiene mora, mostrar imagen morada ***
            rutaCarro = IMG_MORA;
            estadoTexto = "Con Mora";
            colorTextoEstado = new Color(155, 89, 182); // Color morado
            System.out.println("[LOG PANEL] Espacio " + espacio.getNumero() + " - CON MORA (MORADO)");
        } else if (reservado && enHorarioEspecial) {
            // SOLO en horario especial se muestran colores especiales
            if (!ocupado) {
                rutaCarro = IMG_RESERVADO_LIBRE;
                estadoTexto = "Empadronado";
                colorTextoEstado = new Color(241, 196, 15); // Amarillo
            } else if (empadronado) {
                // *** NUEVA LÓGICA: Si es empadronado pero tiene mora, mostrar como auto normal ***
                if (tieneMora) {
                    rutaCarro = IMG_OCUPADO; // Verde normal en lugar de naranja
                    estadoTexto = "Ocupado";
                    colorTextoEstado = new Color(231, 76, 60); // Rojo normal
                    System.out.println("[LOG PANEL] Espacio " + espacio.getNumero() + " - EMPADRONADO CON MORA (ROJO NORMAL)");
                } else {
                    rutaCarro = IMG_RESERVADO_OCUPADO_EMP;
                    estadoTexto = "Empadronado";
                    colorTextoEstado = new Color(243, 156, 18); // Naranja
                    System.out.println("[LOG PANEL] Espacio " + espacio.getNumero() + " - EMPADRONADO SIN MORA (NARANJA)");
                }
            } else {
                rutaCarro = IMG_RESERVADO_OCUPADO_NOEMP;
                estadoTexto = "No Empadronado";
                colorTextoEstado = new Color(231, 76, 60); // Rojo
            }
            System.out.println("[LOG PANEL] Espacio " + espacio.getNumero() + " - RESERVADO EN HORARIO ESPECIAL");
        } else {
            // Fuera de horario especial, los reservados se ven como comunes
            if (ocupado) {
                rutaCarro = IMG_OCUPADO;
                estadoTexto = "Ocupado";
                colorTextoEstado = new Color(231, 76, 60);
                System.out.println("[LOG PANEL] Espacio " + espacio.getNumero() + " - OCUPADO (ROJO)");
            } else {
                rutaCarro = IMG_DISPONIBLE;
                estadoTexto = "Disponible";
                colorTextoEstado = new Color(46, 204, 113);
                System.out.println("[LOG PANEL] Espacio " + espacio.getNumero() + " - DISPONIBLE (VERDE)");
            }
        }

        setScaledIcon(iconoLabel, rutaCarro, ancho - 10, alto - 50);
        estadoLabel.setText(estadoTexto);
        estadoLabel.setForeground(colorTextoEstado);

        // *** NUEVA LÓGICA: Verificar después de actualizar la UI ***
        verificarEstacionamientoLleno();
    }

    private void setScaledIcon(JLabel label, String ruta, int w, int h) {
        java.net.URL url = getClass().getResource(ruta);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
            label.setText("");
            System.out.println("[LOG IMAGEN] Imagen cargada correctamente: " + ruta);
        } else {
            label.setIcon(null);
            label.setText("X");
            System.out.println("[ERROR IMAGEN] No se pudo cargar la imagen: " + ruta);
        }
    }

    public Espacio getEspacio() {
        return this.espacio;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
}