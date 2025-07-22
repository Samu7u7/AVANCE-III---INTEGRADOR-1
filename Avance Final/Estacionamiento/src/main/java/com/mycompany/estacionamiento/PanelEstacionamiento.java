package com.mycompany.estacionamiento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class PanelEstacionamiento extends JPanel {

    private List<Espacio> espacios;
    private AutoPanel seleccionado;

    public PanelEstacionamiento(List<Espacio> espacios) {
        this.espacios = espacios;
        setLayout(new GridLayout(4, 5, 15, 15));
        setBackground(new Color(245, 247, 250));

        int ancho = 150;
        int alto = 80;

        for (Espacio espacio : espacios) {
            AutoPanel panel = new AutoPanel(espacio, ancho, alto);

            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    if (!espacio.isOcupado()) {
                        deseleccionarTodos();
                        panel.setSeleccionado(true);
                        seleccionado = panel;
                        // Aquí puedes abrir el diálogo de registro si lo deseas
                    }
                }

                @Override
                public void mousePressed(MouseEvent evt) {
                    // Doble clic para liberar espacio ocupado
                    if (evt.getClickCount() == 2 && espacio.isOcupado()) {
                        int confirm = JOptionPane.showConfirmDialog(
                                PanelEstacionamiento.this,
                                "¿Deseas liberar este espacio?\nPlaca: " + espacio.getPlaca(),
                                "Liberar espacio",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (confirm == JOptionPane.YES_OPTION) {
                            espacio.liberar();
                            panel.setSeleccionado(false);
                            if (seleccionado == panel) {
                                seleccionado = null;
                            }
                            repaint();
                        }
                    }
                }
            });

            add(panel);
        }
    }

    private void deseleccionarTodos() {
        for (Component c : getComponents()) {
            if (c instanceof AutoPanel) {
                ((AutoPanel) c).setSeleccionado(false);
            }
        }
    }

    /**
     * Devuelve el espacio actualmente seleccionado, o null si ninguno.
     */
    public Espacio getEspacioSeleccionado() {
        if (seleccionado != null) {
            return seleccionado.getEspacio();
        }
        return null;
    }
}