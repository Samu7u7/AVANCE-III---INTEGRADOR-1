package com.mycompany.estacionamiento;

import javax.swing.*;
import java.awt.*;

public class EspacioPanel extends JPanel {
    private final Espacio espacio;
    private boolean seleccionado = false;

    public EspacioPanel(Espacio espacio) {
        this.espacio = espacio;
        setPreferredSize(new Dimension(120, 180));
        setOpaque(false);
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Fondo
        g2.setColor(new Color(230, 240, 255));
        g2.fillRoundRect(0, 0, width, height, 30, 30);

        // Color y estado
        Color colorAuto;
        String estadoTexto;
        Color colorTextoEstado;

        if (espacio.isOcupado()) {
            colorAuto = new Color(231, 76, 60);
            estadoTexto = "Ocupado";
            colorTextoEstado = new Color(231, 76, 60);
        } else if (seleccionado) {
            colorAuto = new Color(52, 152, 219);
            estadoTexto = "Seleccionado";
            colorTextoEstado = new Color(52, 152, 219);
        } else {
            colorAuto = new Color(46, 204, 113);
            estadoTexto = "Disponible";
            colorTextoEstado = new Color(46, 204, 113);
        }

        // Dibuja el auto
        int carWidth = width - 30;
        int carHeight = height - 70;
        int x = (width - carWidth) / 2;
        int y = 20;

        // Cuerpo principal
        g2.setColor(colorAuto);
        g2.fillRoundRect(x, y + 20, carWidth, carHeight - 20, 30, 30);

        // Techo
        g2.setColor(colorAuto.darker());
        g2.fillRoundRect(x + 15, y, carWidth - 30, 30, 20, 20);

        // Ventanas
        g2.setColor(new Color(200, 200, 255));
        g2.fillRect(x + 25, y + 5, carWidth - 50, 20);

        // Ruedas
        g2.setColor(Color.BLACK);
        g2.fillOval(x + 10, y + carHeight - 10, 20, 20);
        g2.fillOval(x + carWidth - 30, y + carHeight - 10, 20, 20);

        // --- AQUÍ SE ELIMINA EL NÚMERO DE ESPACIO ---

        // Estado debajo del auto
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.setColor(colorTextoEstado);
        g2.drawString(estadoTexto, (width - g2.getFontMetrics().stringWidth(estadoTexto)) / 2, height - 30);

        // Si está ocupado, muestra la placa
        if (espacio.isOcupado()) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(Color.WHITE);
            String placa = espacio.getPlaca();
            g2.drawString(placa, (width - g2.getFontMetrics().stringWidth(placa)) / 2, y + carHeight / 2 + 20);
        }
    }
}