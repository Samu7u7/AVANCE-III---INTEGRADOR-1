package com.mycompany.estacionamiento;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.Desktop;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExportadorHistorial {

    /**
     * Exporta el historial completo con formato Excel
     */
    public static void exportarHistorialCSV(Component parent) {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar Reporte de Historial");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (*.xls)", "xls"));
            String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
            fileChooser.setSelectedFile(new java.io.File("Tickets_Historial_" + fechaActual + ".xls"));

            int userSelection = fileChooser.showSaveDialog(parent);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().toLowerCase().endsWith(".xls")) {
                    fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".xls");
                }

                exportarDatosHTML(fileToSave.getAbsolutePath());

                // *** DIÁLOGO MEJORADO Y MODERNO ***
                mostrarDialogoExitoModerno(parent, fileToSave);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(parent,
                    "❌ Error al exportar: " + e.getMessage(),
                    "Error de Exportación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra un diálogo moderno y atractivo para confirmar la exportación exitosa
     */
    private static void mostrarDialogoExitoModerno(Component parent, java.io.File archivo) {
        // Crear panel principal con diseño moderno
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BorderLayout(15, 15));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // *** HEADER CON ÍCONO Y TÍTULO ***
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(Color.WHITE);

        // Ícono de éxito (usando emoji grande)
        JLabel iconoExito = new JLabel("✅", SwingConstants.CENTER);
        iconoExito.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconoExito.setPreferredSize(new Dimension(70, 70));

        // Título principal
        JLabel titulo = new JLabel("¡Exportación Completada!");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(new Color(34, 139, 34)); // Verde éxito

        headerPanel.add(iconoExito, BorderLayout.WEST);
        headerPanel.add(titulo, BorderLayout.CENTER);

        // *** CONTENIDO PRINCIPAL ***
        JPanel contenidoPanel = new JPanel();
        contenidoPanel.setLayout(new BoxLayout(contenidoPanel, BoxLayout.Y_AXIS));
        contenidoPanel.setBackground(Color.WHITE);

        // Mensaje principal
        JLabel mensajePrincipal = new JLabel("¡Su reporte ha sido generado exitosamente!");
        mensajePrincipal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        mensajePrincipal.setForeground(new Color(60, 60, 60));
        mensajePrincipal.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel de información del archivo
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(248, 249, 250));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Información del archivo
        JLabel etiquetaArchivo = new JLabel("Archivo:");
        etiquetaArchivo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        etiquetaArchivo.setForeground(new Color(80, 80, 80));

        JLabel nombreArchivo = new JLabel(archivo.getName());
        nombreArchivo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nombreArchivo.setForeground(new Color(100, 100, 100));

        JLabel etiquetaUbicacion = new JLabel("Ubicación:");
        etiquetaUbicacion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        etiquetaUbicacion.setForeground(new Color(80, 80, 80));

        JLabel rutaArchivo = new JLabel("<html><div style='width: 300px;'>" + archivo.getAbsolutePath() + "</div></html>");
        rutaArchivo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rutaArchivo.setForeground(new Color(100, 100, 100));

        infoPanel.add(etiquetaArchivo);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(nombreArchivo);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(etiquetaUbicacion);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(rutaArchivo);

        contenidoPanel.add(mensajePrincipal);
        contenidoPanel.add(Box.createVerticalStrut(15));
        contenidoPanel.add(infoPanel);

        // *** PANEL DE BOTONES MODERNOS ***
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        botonesPanel.setBackground(Color.WHITE);

        // Botón "Solo Cerrar"
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCerrar.setPreferredSize(new Dimension(100, 35));
        btnCerrar.setBackground(new Color(255, 0, 0));
        btnCerrar.setForeground(new Color(255, 255, 255));
        btnCerrar.setBorder(BorderFactory.createLineBorder(new Color(221, 31, 31), 1));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Botón "Abrir Archivo" (principal)
        JButton btnAbrir = new JButton("Abrir en Excel");
        btnAbrir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAbrir.setPreferredSize(new Dimension(140, 35));
        btnAbrir.setBackground(new Color(0, 120, 215)); // Azul moderno
        btnAbrir.setForeground(Color.WHITE);
        btnAbrir.setBorder(BorderFactory.createEmptyBorder());
        btnAbrir.setFocusPainted(false);
        btnAbrir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Efectos hover para los botones
        btnAbrir.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnAbrir.setBackground(new Color(0, 100, 190));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnAbrir.setBackground(new Color(0, 120, 215));
            }
        });

        btnCerrar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCerrar.setBackground(new Color(183, 30, 30));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCerrar.setBackground(new Color(255, 0, 0));
            }
        });

        botonesPanel.add(btnAbrir);
        botonesPanel.add(btnCerrar);

        // Ensamblar panel principal
        panelPrincipal.add(headerPanel, BorderLayout.NORTH);
        panelPrincipal.add(contenidoPanel, BorderLayout.CENTER);
        panelPrincipal.add(botonesPanel, BorderLayout.SOUTH);

        // Crear y mostrar diálogo
        JDialog dialogo = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "Exportación Exitosa", true);
        dialogo.setContentPane(panelPrincipal);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialogo.setResizable(false);
        dialogo.pack();
        dialogo.setLocationRelativeTo(parent);

        // Acciones de los botones
        btnCerrar.addActionListener(e -> dialogo.dispose());

        btnAbrir.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(archivo);
                dialogo.dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialogo,
                        "No se pudo abrir el archivo automáticamente.\n" +
                        "Por favor, ábralo manualmente desde la ubicación mostrada.",
                        "Aviso",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Mostrar el diálogo
        dialogo.setVisible(true);
    }

    /**
     * Exporta datos con formato HTML que simula Excel
     */
    private static void exportarDatosHTML(String rutaArchivo) throws SQLException, IOException {
        try (Connection conn = ConexionBD.getConexion();
             FileWriter writer = new FileWriter(rutaArchivo)) {

            // ===== ESTRUCTURA HTML CON ESTILO EXCEL =====
            escribirEncabezadoHTML(writer);

            // ===== TÍTULO PRINCIPAL =====
            writer.write("<h1>SISTEMA DE ESTACIONAMIENTO</h1>\n");
            writer.write("<h2>HISTORIAL COMPLETO DE TICKETS</h2>\n");
            writer.write("<div class='fecha-generacion'>\n");
            writer.write("<p><strong>Fecha de generación:</strong> " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "</p>\n");
            writer.write("</div>\n");

            // ===== RESUMEN EJECUTIVO =====
            EstadisticasReporte stats = obtenerEstadisticas(conn);

            // ===== TABLA PRINCIPAL =====
            escribirTablaHistorial(writer, conn, stats);

            writer.write("</div>\n");
            writer.write("<p>");

            escribirResumenEjecutivo(writer, stats);

            // ===== CIERRE HTML =====
            writer.write("</body>\n</html>");
            writer.flush();
        }
    }

    /**
     * Escribe el encabezado HTML con estilos tipo Excel
     */
    private static void escribirEncabezadoHTML(FileWriter writer) throws IOException {
        writer.write("<!DOCTYPE html>\n");
        writer.write("<html>\n<head>\n");
        writer.write("<meta charset='UTF-8'>\n");
        writer.write("<title>Reporte de Historial - Sistema de Estacionamiento</title>\n");
        writer.write("<style>\n");

        // Estilo tipo Excel
        writer.write("body { \n");
        writer.write(" font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; \n");
        writer.write(" margin: 0; padding: 20px; \n");
        writer.write(" background-color: #f0f0f0; \n");
        writer.write(" color: #333; \n");
        writer.write("}\n");

        writer.write("h1 { \n");
        writer.write(" color: #1f4e79; \n");
        writer.write(" text-align: center; \n");
        writer.write(" font-size: 24px; \n");
        writer.write(" margin-bottom: 5px; \n");
        writer.write(" font-weight: bold; \n");
        writer.write("}\n");

        writer.write("h2 { \n");
        writer.write(" color: #2f5f8f; \n");
        writer.write(" text-align: center; \n");
        writer.write(" font-size: 18px; \n");
        writer.write(" margin-top: 0; \n");
        writer.write(" margin-bottom: 20px; \n");
        writer.write("}\n");

        writer.write(".fecha-generacion { \n");
        writer.write(" text-align: center; \n");
        writer.write(" margin-bottom: 40px; \n");
        writer.write(" font-size: 14px; \n");
        writer.write("}\n");

        // Tablas estilo Excel
        writer.write("table { \n");
        writer.write(" border-collapse: collapse; \n");
        writer.write(" width: 100%; \n");
        writer.write(" margin: 20px 0; \n");
        writer.write(" background-color: white; \n");
        writer.write(" box-shadow: 0 2px 4px rgba(0,0,0,0.1); \n");
        writer.write("}\n");

        writer.write("th { \n");
        writer.write(" background-color: #4472c4; \n");
        writer.write(" color: white; \n");
        writer.write(" padding: 12px 8px; \n");
        writer.write(" text-align: center; \n");
        writer.write(" border: 1px solid #2f5f8f; \n");
        writer.write(" font-weight: bold; \n");
        writer.write(" font-size: 12px; \n");
        writer.write("}\n");

        writer.write("td { \n");
        writer.write(" padding: 8px; \n");
        writer.write(" border: 1px solid #d0d7de; \n");
        writer.write(" font-size: 11px; \n");
        writer.write(" text-align: center; \n");
        writer.write("}\n");

        writer.write("tr:nth-child(even) { \n");
        writer.write(" background-color: #f8f9fa; \n");
        writer.write("}\n");

        writer.write("tr:nth-child(odd) { \n");
        writer.write(" background-color: white; \n");
        writer.write("}\n");

        // ===== TABLA RESUMEN CENTRADA PERFECTAMENTE =====
        writer.write(".contenedor-resumen { \n");
        writer.write(" display: flex; \n");
        writer.write(" justify-content: center; \n");
        writer.write(" align-items: center; \n");
        writer.write(" text-align: center; \n");
        writer.write(" width: 100%; \n");
        writer.write(" margin: 30px 0 40px 0; \n");
        writer.write("}\n");

        writer.write(".resumen-tabla { \n");
        writer.write(" width: 450px; \n");
        writer.write(" border: 2px solid #507e32; \n");
        writer.write(" margin: 0; \n");
        writer.write("}\n");

        writer.write(".resumen-tabla th { \n");
        writer.write(" background-color: #70ad47; \n");
        writer.write(" border: 1px solid #507e32; \n");
        writer.write(" font-size: 14px; \n");
        writer.write(" padding: 15px 8px; \n");
        writer.write("}\n");

        writer.write(".resumen-tabla td { \n");
        writer.write(" padding: 10px 15px; \n");
        writer.write(" font-size: 12px; \n");
        writer.write("}\n");

        writer.write(".total-row { \n");
        writer.write(" background-color: #ffc000 !important; \n");
        writer.write(" font-weight: bold; \n");
        writer.write(" color: #333; \n");
        writer.write("}\n");

        writer.write(".activo { \n");
        writer.write(" background-color: #c6efce; \n");
        writer.write(" color: #006100; \n");
        writer.write(" font-weight: bold; \n");
        writer.write("}\n");

        writer.write(".finalizado { \n");
        writer.write(" background-color: #ffc7ce; \n");
        writer.write(" color: #9c0006; \n");
        writer.write(" font-weight: bold; \n");
        writer.write("}\n");

        writer.write(".monto { \n");
        writer.write(" text-align: right; \n");
        writer.write(" font-weight: bold; \n");
        writer.write("}\n");

        // ===== ESTILOS CORREGIDOS PARA MORA =====
        writer.write(".mora-alta { \n");
        writer.write(" background-color: #ffc7ce !important; \n");
        writer.write(" color: #9c0006 !important; \n");
        writer.write(" font-weight: bold !important; \n");
        writer.write("}\n");

        // Especificidad adicional para asegurar que funcione
        writer.write("td.mora-alta { \n");
        writer.write(" font-weight: bold !important; \n");
        writer.write("}\n");

        writer.write(".monto.mora-alta { \n");
        writer.write(" font-weight: bold !important; \n");
        writer.write("}\n");

        writer.write("tr:nth-child(even) td.mora-alta { \n");
        writer.write(" background-color: #ffb3ba !important; \n");
        writer.write(" font-weight: bold !important; \n");
        writer.write("}\n");

        writer.write("tr:nth-child(odd) td.mora-alta { \n");
        writer.write(" background-color: #ffc7ce !important; \n");
        writer.write(" font-weight: bold !important; \n");
        writer.write("}\n");

        writer.write("</style>\n");
        writer.write("</head>\n<body>\n");
    }

    /**
     * Escribe la tabla de resumen ejecutivo CENTRADA
     */
    private static void escribirResumenEjecutivo(FileWriter writer, EstadisticasReporte stats) throws IOException {
        writer.write("<div class='contenedor-resumen'>\n");
        writer.write("<table class='resumen-tabla'>\n");
        writer.write("<tr><th colspan='2'>📊 RESUMEN EJECUTIVO</th></tr>\n");
        writer.write("<tr><td><strong>Total de Tickets</strong></td><td><strong>" + stats.totalTickets + "</strong></td></tr>\n");
        writer.write("<tr><td><strong>Ingresos Totales</strong></td><td class='monto'><strong>S/ " + String.format("%.2f", stats.ingresosTotales) + "</strong></td></tr>\n");
        writer.write("<tr><td><strong>Ingresos por Mora</strong></td><td class='monto'><strong>S/ " + String.format("%.2f", stats.ingresosMora) + "</strong></td></tr>\n");
        writer.write("<tr><td><strong>Tickets Activos</strong></td><td><strong>" + stats.ticketsActivos + "</strong></td></tr>\n");
        writer.write("<tr><td><strong>Tickets Finalizados</strong></td><td><strong>" + stats.ticketsFinalizados + "</strong></td></tr>\n");
        writer.write("<tr><td><strong>Tiempo Promedio</strong></td><td><strong>" + String.format("%.1f horas", stats.tiempoPromedio) + "</strong></td></tr>\n");
        writer.write("</table>\n");
        writer.write("</div>\n");
    }

    /**
     * Escribe la tabla principal del historial usando la columna 'mora' de la BD
     */
    private static void escribirTablaHistorial(FileWriter writer, Connection conn, EstadisticasReporte stats) throws IOException, SQLException {
        writer.write("<table>\n");
        writer.write("<tr>\n");
        writer.write("<th>ID</th>\n");
        writer.write("<th>PLACA</th>\n");
        writer.write("<th>CLIENTE</th>\n");
        writer.write("<th>ESPACIO</th>\n");
        writer.write("<th>FECHA ENTRADA</th>\n");
        writer.write("<th>FECHA SALIDA</th>\n");
        writer.write("<th>TIPO SERVICIO</th>\n");
        writer.write("<th>HORAS CONTRATADAS</th>\n");
        writer.write("<th>MONTO BASE</th>\n");
        writer.write("<th>MORA</th>\n");
        writer.write("<th>TOTAL</th>\n");
        writer.write("<th>ESTADO</th>\n");
        writer.write("</tr>\n");

        // ===== CONSULTA USANDO LA COLUMNA 'mora' DE LA BASE DE DATOS =====
        String consultaSQL =
                "SELECT id, placa, nombre_persona, numero_espacio, fecha_entrada, fecha_salida, " +
                "tipo_servicio, horas, monto, mora, finalizado " +
                "FROM tickets " +
                "GROUP BY id " +
                "ORDER BY fecha_entrada ASC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(consultaSQL)) {

            double totalGeneral = 0;

            while (rs.next()) {
                double monto = rs.getDouble("monto");
                double mora = rs.getDouble("mora"); // ✅ Usando la columna mora de la BD
                double total = monto + mora;
                totalGeneral += total;

                boolean finalizado = rs.getBoolean("finalizado");
                String estadoClass = finalizado ? "finalizado" : "activo";
                String estadoTexto = finalizado ? "FINALIZADO" : "ACTIVO";

                writer.write("<tr>\n");
                writer.write("<td>" + rs.getInt("id") + "</td>\n");
                writer.write("<td><strong>" + rs.getString("placa") + "</strong></td>\n");
                writer.write("<td>" + (rs.getString("nombre_persona") != null ? rs.getString("nombre_persona") : "No especificado") + "</td>\n");
                writer.write("<td><strong>E-" + String.format("%02d", rs.getInt("numero_espacio")) + "</strong></td>\n");

                // Fecha entrada
                writer.write("<td>" + rs.getTimestamp("fecha_entrada").toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</td>\n");

                // Fecha salida
                if (rs.getTimestamp("fecha_salida") != null) {
                    writer.write("<td>" + rs.getTimestamp("fecha_salida").toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</td>\n");
                } else {
                    writer.write("<td class='activo'>EN CURSO</td>\n");
                }

                writer.write("<td>" + rs.getString("tipo_servicio") + "</td>\n");

                // Horas contratadas
                int horasContratadas = rs.getInt("horas");
                if (horasContratadas > 0) {
                    writer.write("<td><strong>" + horasContratadas + "h</strong></td>\n");
                } else {
                    writer.write("<td>N/A</td>\n");
                }

                writer.write("<td class='monto'>S/ " + String.format("%.2f", monto) + "</td>\n");

                // ===== MORA CON FORMATO CORREGIDO =====
                if (mora > 0) {
                    writer.write("<td class='monto mora-alta'><strong>S/ " + String.format("%.2f", mora) + "</strong></td>\n");
                } else {
                    writer.write("<td class='monto'>S/ " + String.format("%.2f", mora) + "</td>\n");
                }

                writer.write("<td class='monto'><strong>S/ " + String.format("%.2f", total) + "</strong></td>\n");
                writer.write("<td class='" + estadoClass + "'>" + estadoTexto + "</td>\n");
                writer.write("</tr>\n");
            }

            // Fila de totales
            writer.write("<tr class='total-row'>\n");
            writer.write("<td colspan='8'><strong>TOTALES GENERALES</strong></td>\n");
            writer.write("<td class='monto'><strong>S/ " + String.format("%.2f", stats.montoBase) + "</strong></td>\n");
            writer.write("<td class='monto'><strong>S/ " + String.format("%.2f", stats.ingresosMora) + "</strong></td>\n");
            writer.write("<td class='monto'><strong>S/ " + String.format("%.2f", stats.ingresosTotales) + "</strong></td>\n");
            writer.write("<td></td>\n");
            writer.write("</tr>\n");
        }

        writer.write("</table>\n");
    }

    // ===== CLASES Y MÉTODOS AUXILIARES =====
    private static class EstadisticasReporte {
        int totalTickets;
        double ingresosTotales;
        double ingresosMora;
        double montoBase;
        int ticketsActivos;
        int ticketsFinalizados;
        double tiempoPromedio;
    }

    private static EstadisticasReporte obtenerEstadisticas(Connection conn) throws SQLException {
        EstadisticasReporte stats = new EstadisticasReporte();

        // Total tickets
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM tickets")) {
            if (rs.next()) stats.totalTickets = rs.getInt(1);
        }

        // ===== INGRESOS USANDO LA COLUMNA 'mora' =====
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT SUM(monto) as total_monto, SUM(mora) as total_mora " +
                     "FROM tickets")) {
            if (rs.next()) {
                stats.montoBase = rs.getDouble("total_monto");
                stats.ingresosMora = rs.getDouble("total_mora");
                stats.ingresosTotales = stats.montoBase + stats.ingresosMora;
            }
        }

        // Tickets activos y finalizados
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT finalizado, COUNT(*) FROM tickets GROUP BY finalizado")) {
            while (rs.next()) {
                if (rs.getBoolean(1)) {
                    stats.ticketsFinalizados = rs.getInt(2);
                } else {
                    stats.ticketsActivos = rs.getInt(2);
                }
            }
        }

        // Tiempo promedio
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT AVG(TIMESTAMPDIFF(HOUR, fecha_entrada, fecha_salida)) " +
                     "FROM tickets WHERE fecha_salida IS NOT NULL")) {
            if (rs.next()) stats.tiempoPromedio = rs.getDouble(1);
        }

        return stats;
    }
}