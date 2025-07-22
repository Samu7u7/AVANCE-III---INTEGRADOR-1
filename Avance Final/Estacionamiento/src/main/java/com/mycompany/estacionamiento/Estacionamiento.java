package com.mycompany.estacionamiento;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class Estacionamiento {
    private List<Espacio> espacios;

    public Estacionamiento() {
        espacios = new ArrayList<>();
        cargarEspaciosDesdeBD();
    }

    private void cargarEspaciosDesdeBD() {
        espacios.clear();
        System.out.println("[LOG] Cargando espacios desde la base de datos...");
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT numero, ocupado, placa FROM espacios WHERE numero BETWEEN 1 AND 20 ORDER BY numero")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Espacio e = new Espacio(rs.getInt("numero"));
                if (rs.getBoolean("ocupado")) {
                    String placa = rs.getString("placa");
                    if (placa != null && !placa.trim().isEmpty()) {
                        e.ocupar(placa);
                    }
                }
                espacios.add(e);
            }

            System.out.println("[LOG] Espacios cargados: " + espacios.size());

            // Si no hay exactamente 20 espacios, crear los faltantes
            if (espacios.size() != 20) {
                System.out.println("[WARNING] Se esperaban 20 espacios, se encontraron: " + espacios.size());
                crearEspaciosFaltantes();
            }

        } catch (SQLException ex) {
            System.out.println("[ERROR] Error al cargar espacios: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void crearEspaciosFaltantes() {
        try (Connection conn = ConexionBD.getConexion()) {
            // Crear espacios del 1 al 20 si no existen
            for (int i = 1; i <= 20; i++) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT IGNORE INTO espacios (numero, ocupado, placa, hora_entrada) VALUES (?, 0, NULL, NULL)")) {
                    ps.setInt(1, i);
                    ps.executeUpdate();
                }
            }

            // Recargar espacios después de crear los faltantes
            espacios.clear();
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT numero, ocupado, placa FROM espacios WHERE numero BETWEEN 1 AND 20 ORDER BY numero")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Espacio e = new Espacio(rs.getInt("numero"));
                    if (rs.getBoolean("ocupado")) {
                        String placa = rs.getString("placa");
                        if (placa != null && !placa.trim().isEmpty()) {
                            e.ocupar(placa);
                        }
                    }
                    espacios.add(e);
                }
            }

            System.out.println("[LOG] Espacios después de crear faltantes: " + espacios.size());

        } catch (SQLException ex) {
            System.out.println("[ERROR] Error al crear espacios faltantes: " + ex.getMessage());
        }
    }

    public List<Espacio> getEspacios() {
        return espacios;
    }

    public int getEspaciosDisponibles() {
        return (int) espacios.stream().filter(e -> !e.isOcupado()).count();
    }

    public int getEspaciosOcupados() {
        return (int) espacios.stream().filter(Espacio::isOcupado).count();
    }

    public double getIngresosDiarios() {
        double total = 0;
        try (Connection conn = ConexionBD.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT SUM((CASE WHEN NOT (EXISTS (SELECT 1 FROM empadronados WHERE empadronados.placa = tickets.placa) AND (TIME(fecha_pago) >= '22:00:00' OR TIME(fecha_pago) <= '06:00:00')) THEN monto ELSE 0 END) + IFNULL(mora, 0)) FROM tickets WHERE DATE(fecha_pago) = CURDATE() AND finalizado = 1")) {

            if (rs.next()) {
                total = rs.getDouble(1);
            }

        } catch (SQLException ex) {
            System.out.println("[ERROR] Error al consultar ingresos diarios: " + ex.getMessage());
            ex.printStackTrace();
        }
        return total;
    }

    public boolean existeVehiculoEstacionado(String placa) {
        return espacios.stream()
                .anyMatch(e -> e.isOcupado() && e.getPlaca() != null && e.getPlaca().equalsIgnoreCase(placa));
    }

    public void ocuparEspacio(Espacio espacio, String placa) {
        espacio.ocupar(placa);
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE espacios SET ocupado = 1, placa = ?, hora_entrada = NOW() WHERE numero = ?")) {

            ps.setString(1, placa);
            ps.setInt(2, espacio.getNumero());
            ps.executeUpdate();

            System.out.println("[LOG] Espacio " + espacio.getNumero() + " ocupado por placa " + placa);

        } catch (SQLException ex) {
            System.out.println("[ERROR] Error al ocupar espacio: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // *** MÉTODO CORREGIDO: Ahora calcula y guarda la mora automáticamente ***
    public void liberarEspacio(Espacio espacio) {
        String placa = espacio.getPlaca();
        System.out.println("[LOG] Intentando liberar espacio " + espacio.getNumero() + " para placa: " + placa);

        if (placa == null || placa.isEmpty()) {
            System.out.println("[LOG] No hay placa asociada al espacio. No se realiza ninguna acción.");
            return;
        }

        try (Connection conn = ConexionBD.getConexion()) {
            // 1. Liberar espacio en la tabla espacios
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE espacios SET ocupado = 0, placa = NULL, hora_entrada = NULL WHERE numero = ?")) {
                ps.setInt(1, espacio.getNumero());
                int updated = ps.executeUpdate();
                System.out.println("[LOG] UPDATE espacios ejecutado. Filas afectadas: " + updated);
            }

            // 2. Buscar el ticket activo más reciente
            int ticketId = -1;
            Ticket ticket = null;
            
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM tickets WHERE placa = ? AND finalizado = 0 ORDER BY fecha_entrada DESC LIMIT 1")) {
                ps.setString(1, placa);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ticketId = rs.getInt("id");
                        System.out.println("[LOG] Ticket activo encontrado con id: " + ticketId);
                        
                        // *** NUEVO: Buscar el ticket completo para calcular mora ***
                        ticket = buscarTicketPorPlaca(placa);
                    } else {
                        System.out.println("[LOG] No se encontró ticket activo para la placa: " + placa);
                    }
                }
            }

            // 3. Si existe ticket, calcular mora y actualizar
            if (ticketId != -1 && ticket != null) {
                // *** NUEVO: Registrar salida (esto calcula la mora automáticamente) ***
                ticket.registrarSalida();
                
                // *** NUEVO: Obtener la mora calculada ***
                double moraCalculada = ticket.getMoraPagada();
                
                System.out.println("[LOG] Mora calculada: S/ " + String.format("%.2f", moraCalculada));
                
                // *** NUEVO: Actualizar ticket con fecha de salida Y mora ***
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE tickets SET finalizado = 1, fecha_salida = NOW(), mora = ? WHERE id = ?")) {
                    ps.setDouble(1, moraCalculada);
                    ps.setInt(2, ticketId);
                    int updated = ps.executeUpdate();
                    System.out.println("[LOG] UPDATE tickets ejecutado. Filas afectadas: " + updated);
                    System.out.println("[LOG] Mora guardada en BD: S/ " + String.format("%.2f", moraCalculada));
                }
            }

        } catch (SQLException ex) {
            System.out.println("[ERROR] Error al liberar espacio: " + ex.getMessage());
            ex.printStackTrace();
        }

        // 4. Actualizar en memoria
        espacio.liberar();
        System.out.println("[LOG] Espacio " + espacio.getNumero() + " liberado en memoria.");
    }

    public void guardarTicket(Ticket ticket, String nombrePersona) {
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO tickets (placa, nombre_persona, tipo_servicio, horas, monto, pagado, fecha_entrada, fecha_pago, numero_espacio, finalizado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)")) {

            ps.setString(1, ticket.getPlaca());
            ps.setString(2, nombrePersona);
            ps.setString(3, ticket.getTipoServicio().getDescripcion());
            ps.setInt(4, ticket.getHorasContratadas());
            ps.setDouble(5, ticket.calcularTotal());
            ps.setBoolean(6, true);
            ps.setTimestamp(7, java.sql.Timestamp.valueOf(ticket.getEntrada()));
            ps.setTimestamp(8, new java.sql.Timestamp(System.currentTimeMillis()));
            ps.setInt(9, ticket.getNumeroEspacio());

            ps.executeUpdate();

            System.out.println("[LOG] Ticket guardado para placa: " + ticket.getPlaca() + ", persona: " + nombrePersona);

        } catch (SQLException ex) {
            System.out.println("[ERROR] Error al guardar ticket: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void guardarTicket(Ticket ticket) {
        guardarTicket(ticket, "No especificado");
    }

    public Ticket buscarTicketPorPlaca(String placa) {
        Ticket ticket = null;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM tickets WHERE placa = ? AND finalizado = 0 ORDER BY fecha_entrada DESC LIMIT 1")) {

            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tipoStr = rs.getString("tipo_servicio");
                    Ticket.TipoServicio tipoServicio = tipoStr.equalsIgnoreCase("Tiempo Definido")
                            ? Ticket.TipoServicio.TIEMPO_DEFINIDO
                            : Ticket.TipoServicio.TIEMPO_INDEFINIDO;

                    int horas = rs.getInt("horas");
                    LocalDateTime entrada = rs.getTimestamp("fecha_entrada").toLocalDateTime();
                    double monto = rs.getDouble("monto");
                    int numeroEspacio = rs.getInt("numero_espacio");

                    // *** NUEVO: Obtener nombre de la persona ***
                    String nombrePersona = rs.getString("nombre_persona");
                    if (nombrePersona == null || nombrePersona.trim().isEmpty()) {
                        nombrePersona = "No especificado";
                    }

                    // *** USAR NUEVO CONSTRUCTOR con nombre ***
                    ticket = new Ticket(placa, nombrePersona, tipoServicio, horas, numeroEspacio);

                    // *** USAR SETTERS PÚBLICOS en lugar de reflexión ***
                    ticket.setEntrada(entrada);
                    ticket.registrarPago(monto);

                    // *** OPCIONAL: Cargar mora si existe ***
                    double mora = rs.getDouble("mora");
                    if (mora > 0) {
                        ticket.registrarMora(mora);
                    }

                    // *** OPCIONAL: Cargar fecha de salida si existe ***
                    Timestamp salidaTimestamp = rs.getTimestamp("fecha_salida");
                    if (salidaTimestamp != null) {
                        ticket.setSalida(salidaTimestamp.toLocalDateTime());
                    }

                    System.out.println("[LOG] Ticket cargado para placa: " + placa +
                            ", persona: " + nombrePersona +
                            ", monto: S/" + String.format("%.2f", monto));
                }
            }

        } catch (SQLException ex) {
            System.out.println("[ERROR] Error al buscar ticket por placa: " + ex.getMessage());
            ex.printStackTrace();
        }
        return ticket;
    }

    // *** MÉTODO REQUERIDO POR VentanaPrincipal y AutoPanel ***
    public boolean esEmpadronado(String placa) {
        boolean resultado = false;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM empadronados WHERE placa = ?")) {

            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resultado = rs.getInt(1) > 0;
                }
            }

        } catch (SQLException ex) {
            System.out.println("[ERROR] Error al comprobar empadronado: " + ex.getMessage());
            ex.printStackTrace();
        }
        return resultado;
    }

    public Empadronado obtenerEmpadronadoPorPlaca(String placa) {
        Empadronado empadronado = null;
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT placa, nombre FROM empadronados WHERE placa = ?")) {

            ps.setString(1, placa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    empadronado = new Empadronado(
                            rs.getString("placa"),
                            rs.getString("nombre")
                    );
                }
            }

        } catch (SQLException ex) {
            System.out.println("[ERROR] Error al obtener empadronado: " + ex.getMessage());
            ex.printStackTrace();
        }
        return empadronado;
    }
}
