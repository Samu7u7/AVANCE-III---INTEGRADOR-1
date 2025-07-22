package com.mycompany.estacionamiento;

import java.sql.*;

public class ConexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/estacionamiento";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Cambia si tienes contraseña

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void inicializarBD() {
        try (Connection conn = getConexion()) {
            // Crear base de datos si no existe
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE DATABASE IF NOT EXISTS estacionamiento");
                stmt.execute("USE estacionamiento");
            }

            // Tabla espacios - CORREGIDO: Asegurar solo 20 espacios
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS espacios (" +
                "numero INT PRIMARY KEY, " +
                "ocupado BOOLEAN DEFAULT FALSE, " +
                "placa VARCHAR(20), " +
                "hora_entrada TIMESTAMP NULL)"
            );

            // Tabla tickets - CORREGIDO: Agregar campo mora
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS tickets (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "placa VARCHAR(20) NOT NULL, " +
                "nombre_persona VARCHAR(100), " +
                "tipo_servicio VARCHAR(50) NOT NULL, " +
                "horas INT, " +
                "monto DECIMAL(10,2) NOT NULL, " +
                "pagado BOOLEAN DEFAULT FALSE, " +
                "fecha_entrada TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "fecha_salida TIMESTAMP NULL, " +
                "fecha_pago TIMESTAMP NULL, " +
                "numero_espacio INT, " +
                "finalizado BOOLEAN DEFAULT FALSE, " +
                "mora DECIMAL(10,2) DEFAULT 0)"
            );

            // Tabla empadronados - CORREGIDO: Crear tabla completa con telefono
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS empadronados (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "placa VARCHAR(20) UNIQUE NOT NULL, " +
                "nombre VARCHAR(100) NOT NULL, " +
                "telefono VARCHAR(20), " +
                "fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            // Tabla usuarios admin
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS usuarios_admin (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "usuario VARCHAR(50) UNIQUE NOT NULL, " +
                "password VARCHAR(50) NOT NULL)"
            );

            // Insertar usuario admin por defecto
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT IGNORE INTO usuarios_admin (usuario, password) VALUES (?, ?)")) {
                ps.setString(1, "pepeadmin");
                ps.setString(2, "12345");
                ps.executeUpdate();
            }

            // CORREGIDO: Insertar EXACTAMENTE 20 espacios (1-20)
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT IGNORE INTO espacios (numero, ocupado, placa, hora_entrada) VALUES (?, 0, NULL, NULL)")) {
                for (int i = 1; i <= 20; i++) {
                    ps.setInt(1, i);
                    ps.executeUpdate();
                }
            }
            verificarColumnaTelefono(conn);
            
            System.out.println("[LOG] Base de datos MySQL inicializada correctamente (sin datos de prueba)");

        } catch (SQLException e) {
            System.out.println("[ERROR] Error al inicializar BD MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void verificarColumnaTelefono(Connection conn) {
        try {
            // Verificar si la columna telefono existe
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "empadronados", "telefono");

            if (!columns.next()) {
                // La columna no existe, agregarla
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE empadronados ADD COLUMN telefono VARCHAR(20)");
                    System.out.println("[LOG] Columna 'telefono' agregada a la tabla empadronados");
                }
            }
            columns.close();

        } catch (SQLException e) {
            System.out.println("[WARNING] No se pudo verificar/agregar columna telefono: " + e.getMessage());
        }
    }
}