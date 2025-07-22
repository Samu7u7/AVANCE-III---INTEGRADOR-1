package com.mycompany.estacionamiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EstacionamientoSingleton {
    private static Estacionamiento instancia;

    // Devuelve la instancia global
    public static Estacionamiento getInstance() {
        if (instancia == null) {
            instancia = new Estacionamiento();
        }
        return instancia;
    }

    // Permite setear la instancia desde fuera (por ejemplo, desde VentanaPrincipal)
    public static void setInstance(Estacionamiento est) {
        instancia = est;
    }
    // AGREGAR este método en EstacionamientoSingleton.java
public int contarEmpadronadosActivos() {
    try (Connection conn = ConexionBD.getConexion();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM empadronados")) {
        
        if (rs.next()) {
            return rs.getInt("total");
        }
    } catch (SQLException e) {
        System.out.println("[ERROR] Error al contar empadronados: " + e.getMessage());
    }
    return 0;
}
}