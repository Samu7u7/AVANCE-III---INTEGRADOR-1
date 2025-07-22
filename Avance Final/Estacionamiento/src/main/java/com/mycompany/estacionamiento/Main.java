package com.mycompany.estacionamiento;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
            ventana.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        });
    }
}