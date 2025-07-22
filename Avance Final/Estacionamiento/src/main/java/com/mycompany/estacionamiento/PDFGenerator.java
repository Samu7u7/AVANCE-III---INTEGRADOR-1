package com.mycompany.estacionamiento;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PDFGenerator {

    public static void generarYAbrirTicketPDF(Ticket ticket) {
        try {
            // Crear archivo HTML temporal
            File tempFile = File.createTempFile("ticket_", ".html");
            tempFile.deleteOnExit();

            String htmlContent = generarHTMLTicket(ticket);

            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(htmlContent);
            }

            // Abrir en el navegador
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(tempFile.toURI());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generarHTMLTicket(Ticket ticket) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html lang='es'>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>Ticket de Estacionamiento</title>");
        html.append("<style>");
        
        // CSS mejorado
        html.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }");
        html.append(".ticket { max-width: 450px; margin: 0 auto; background: white; border-radius: 15px; box-shadow: 0 15px 35px rgba(0,0,0,0.3); overflow: hidden; }");
        html.append(".header { background: linear-gradient(135deg, #1976d2, #2196f3); color: white; padding: 25px; text-align: center; position: relative; }");
        html.append(".header::before { content: ''; position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: url('data:image/svg+xml,<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\"><circle cx=\"20\" cy=\"20\" r=\"2\" fill=\"white\" opacity=\"0.1\"/><circle cx=\"80\" cy=\"80\" r=\"2\" fill=\"white\" opacity=\"0.1\"/><circle cx=\"40\" cy=\"60\" r=\"1\" fill=\"white\" opacity=\"0.1\"/></svg>'); }");
        html.append(".header h1 { margin: 0; font-size: 26px; font-weight: bold; position: relative; z-index: 1; }");
        html.append(".header p { margin: 5px 0 0 0; opacity: 0.9; position: relative; z-index: 1; }");
        html.append(".content { padding: 30px; }");
        html.append(".info-section { margin-bottom: 25px; }");
        html.append(".section-title { font-size: 16px; font-weight: bold; color: #1976d2; margin-bottom: 15px; border-bottom: 2px solid #e3f2fd; padding-bottom: 5px; }");
        html.append(".info-row { display: flex; justify-content: space-between; margin: 10px 0; padding: 10px 0; border-bottom: 1px solid #f5f5f5; }");
        html.append(".info-row:last-child { border-bottom: none; }");
        html.append(".label { font-weight: 600; color: #333; display: flex; align-items: center; }");
        html.append(".value { color: #666; font-weight: 500; text-align: right; }");
        html.append(".highlight { background: linear-gradient(135deg, #4caf50, #45a049); color: white; padding: 20px; border-radius: 10px; text-align: center; margin: 25px 0; box-shadow: 0 4px 15px rgba(76, 175, 80, 0.3); }");
        html.append(".highlight .amount { font-size: 32px; font-weight: bold; margin: 8px 0; }");
        html.append(".highlight .subtitle { font-size: 14px; opacity: 0.9; }");
        html.append(".barcode { text-align: center; margin: 25px 0; font-family: 'Courier New', monospace; font-size: 11px; letter-spacing: 1px; background: #f8f9fa; padding: 15px; border-radius: 8px; }");
        html.append(".footer { text-align: center; color: #666; font-size: 12px; margin-top: 25px; padding-top: 20px; border-top: 2px dashed #ddd; line-height: 1.6; }");
        html.append(".status { display: inline-block; padding: 6px 16px; border-radius: 25px; font-size: 12px; font-weight: bold; text-transform: uppercase; }");
        html.append(".status.finalizado { background: #4caf50; color: white; }");
        html.append(".status.activo { background: #ff9800; color: white; }");
        html.append(".mora-alert { background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 15px; margin: 15px 0; }");
        html.append(".mora-alert .mora-title { color: #856404; font-weight: bold; margin-bottom: 5px; }");
        html.append(".mora-alert .mora-details { color: #856404; font-size: 14px; }");
        
        // *** NUEVOS ESTILOS PARA BOTONES ***
        html.append(".print-section { text-align: center; margin: 25px 0; padding: 20px; background: #f8f9fa; border-radius: 10px; }");
        html.append(".print-btn { background: linear-gradient(135deg, #2196f3, #1976d2); color: white; border: none; padding: 12px 30px; border-radius: 25px; font-size: 16px; font-weight: bold; cursor: pointer; margin: 0 10px; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(33, 150, 243, 0.3); }");
        html.append(".print-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(33, 150, 243, 0.4); }");
        html.append(".print-btn:active { transform: translateY(0); }");
        html.append(".save-btn { background: linear-gradient(135deg, #ff9800, #f57c00); color: white; border: none; padding: 12px 30px; border-radius: 25px; font-size: 16px; font-weight: bold; cursor: pointer; margin: 0 10px; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(76, 175, 80, 0.3); }");
        html.append(".save-btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(76, 175, 80, 0.4); }");
        html.append(".print-instructions { margin-top: 15px; font-size: 14px; color: #666; }");
        
        html.append("@media print { body { background: white; } .ticket { box-shadow: none; } .print-section { display: none; } }");
        
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<div class='ticket'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>Alquiler de Estacionamiento</h1>");
        html.append("<p>UTP Sur, Lima - Perú</p>");
        html.append("<p>📞 +51 922928818</p>");
        html.append("<p>🌐 www.grupointegradorutp.com</p>");
        html.append("</div>");

        // Content
        html.append("<div class='content'>");

        // Información del Cliente
        html.append("<div class='info-section'>");
        html.append("<div class='section-title'>👤 INFORMACIÓN DEL CLIENTE</div>");
        
        html.append("<div class='info-row'>");
        html.append("<span class='label'>🎫 Ticket ID:</span>");
        html.append("<span class='value'>#").append(String.format("%06d", System.currentTimeMillis() % 1000000)).append("</span>");
        html.append("</div>");

        html.append("<div class='info-row'>");
        html.append("<span class='label'>🚗 Placa:</span>");
        html.append("<span class='value'><strong>").append(ticket.getPlaca()).append("</strong></span>");
        html.append("</div>");

        // *** NOMBRE DE LA PERSONA ***
        html.append("<div class='info-row'>");
        html.append("<span class='label'>👤 Nombre:</span>");
        html.append("<span class='value'>").append(ticket.getNombrePersona() != null ? ticket.getNombrePersona() : "No especificado").append("</span>");
        html.append("</div>");

        html.append("<div class='info-row'>");
        html.append("<span class='label'>🅿️ Espacio:</span>");
        html.append("<span class='value'><strong>E-").append(String.format("%02d", ticket.getNumeroEspacio())).append("</strong></span>");
        html.append("</div>");
        html.append("</div>");

        // Información del Servicio
        html.append("<div class='info-section'>");
        html.append("<div class='section-title'>⏱️ INFORMACIÓN DEL SERVICIO</div>");

        html.append("<div class='info-row'>");
        html.append("<span class='label'>📅 Entrada:</span>");
        html.append("<span class='value'>").append(formatearFecha(ticket.getEntrada())).append("</span>");
        html.append("</div>");

        if (ticket.getSalida() != null) {
            html.append("<div class='info-row'>");
            html.append("<span class='label'>📅 Salida:</span>");
            html.append("<span class='value'>").append(formatearFecha(ticket.getSalida())).append("</span>");
            html.append("</div>");

            // Tiempo total
            long minutosTotal = java.time.Duration.between(ticket.getEntrada(), ticket.getSalida()).toMinutes();
            long horas = minutosTotal / 60;
            long minutos = minutosTotal % 60;
            html.append("<div class='info-row'>");
            html.append("<span class='label'>⏰ Tiempo Total:</span>");
            html.append("<span class='value'>").append(horas).append("h ").append(minutos).append("m</span>");
            html.append("</div>");
        }

        html.append("<div class='info-row'>");
        html.append("<span class='label'>⏱️ Tipo:</span>");
        html.append("<span class='value'>").append(ticket.getTipoServicio().getDescripcion()).append("</span>");
        html.append("</div>");

        if (ticket.getTipoServicio() == Ticket.TipoServicio.TIEMPO_DEFINIDO) {
            html.append("<div class='info-row'>");
            html.append("<span class='label'>🕐 Horas Contratadas:</span>");
            html.append("<span class='value'>").append(ticket.getHorasContratadas()).append(" horas</span>");
            html.append("</div>");
        }

        // Estado
        html.append("<div class='info-row'>");
        html.append("<span class='label'>📊 Estado:</span>");
        html.append("<span class='value'>");
        if (ticket.getSalida() != null) {
            html.append("<span class='status finalizado'>✅ Finalizado</span>");
        } else {
            html.append("<span class='status activo'>🔄 Activo</span>");
        }
        html.append("</span>");
        html.append("</div>");
        html.append("</div>");

        // Información de Pago
        html.append("<div class='info-section'>");
        html.append("<div class='section-title'>💰 INFORMACIÓN DE PAGO</div>");

        // Monto destacado
        double totalPagado = ticket.getMontoPagado() + ticket.getMoraPagada();
        html.append("<div class='highlight'>");
        html.append("<div class='subtitle'>💳 Total Pagado</div>");
        html.append("<div class='amount'>S/ ").append(String.format("%.2f", totalPagado)).append("</div>");
        
        if (ticket.getMoraPagada() > 0) {
            html.append("<div style='font-size: 14px; margin-top: 10px; opacity: 0.9;'>");
            html.append("Monto base: S/ ").append(String.format("%.2f", ticket.getMontoPagado()));
            html.append(" + Mora: S/ ").append(String.format("%.2f", ticket.getMoraPagada()));
            html.append("</div>");
        }
        html.append("</div>");

        // Alerta de mora si existe
        if (ticket.getMoraPagada() > 0) {
            html.append("<div class='mora-alert'>");
            html.append("<div class='mora-title'>⚠️ MORA POR TIEMPO EXCEDIDO</div>");
            html.append("<div class='mora-details'>");
            html.append("Tiempo excedido: ").append(ticket.getMinutosExcedidos()).append(" minutos<br>");
            html.append("Mora aplicada: S/ ").append(String.format("%.2f", ticket.getMoraPagada()));
            html.append("</div>");
            html.append("</div>");
        }
        html.append("</div>");

        // *** NUEVA SECCIÓN: BOTONES DE IMPRESIÓN ***
        html.append("<div class='print-section'>");
        html.append("<h3 style='margin-top: 0; color: #1976d2;'>🖨️ Acciones del Documento</h3>");
        html.append("<button class='print-btn' onclick='imprimirTicket()'>🖨️ Imprimir Ticket</button>");
        html.append("<button class='save-btn' onclick='guardarPDF()'>💾 Guardar como PDF</button>");
        html.append("<div class='print-instructions'>");
        html.append("💼 Usa estas opciones para guardar o imprimir las veces que necesites :D");
        html.append("</div>");
        html.append("</div>");

        // Código de barras simulado
        html.append("<div class='barcode'>");
        html.append("||||| |||| | |||| ||||| || ||| |||||| ||| ||||||<br>");
        html.append("").append(ticket.getPlaca()).append("-").append(ticket.getNumeroEspacio()).append("-").append(System.currentTimeMillis() % 10000);
        html.append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("⭐ <strong>¡Gracias por su preferencia!</strong> ⭐<br>");
        html.append("🚫 No pierda este ticket - Costo por pérdida: <strong>S/ 20.00</strong><br>");
        html.append("🌐 www.grupointegradorutp.com<br>");
        html.append("📧 soporte@grupointegradorutp.com<br>");
        html.append("📱 WhatsApp: +51 922928818<br><br>");
        html.append("<strong>Generado el:</strong> ").append(formatearFecha(LocalDateTime.now()));
        html.append("</div>");

        html.append("</div>"); // content
        html.append("</div>"); // ticket

        // *** SCRIPT MEJORADO CON MÁS OPCIONES ***
        html.append("<script>");
        html.append("let impresionRealizada = false;");
        
        // Función para imprimir
        html.append("function imprimirTicket() {");
        html.append("  window.print();");
        html.append("  impresionRealizada = true;");
        html.append("}");
        
        // Función para guardar como PDF
        // Pregunta inicial mejorada
        
        // Recordatorio antes de cerrar
        html.append("window.addEventListener('beforeunload', function(e) {");
        html.append("  if (!impresionRealizada) {");
        html.append("    e.preventDefault();");
        html.append("    e.returnValue = '¿Está seguro de cerrar sin imprimir el ticket?';");
        html.append("  }");
        html.append("});");
        
        html.append("</script>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    private static String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) return "N/A";
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}