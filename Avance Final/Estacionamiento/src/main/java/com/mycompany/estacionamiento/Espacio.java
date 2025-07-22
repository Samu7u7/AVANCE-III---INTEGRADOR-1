package com.mycompany.estacionamiento;

import java.time.LocalDateTime;

public class Espacio {
    private int numero;
    private boolean ocupado;
    private String placa;
    private LocalDateTime horaEntrada;

    public Espacio(int numero) {
        this.numero = numero;
        this.ocupado = false;
    }

    public void ocupar(String placa) {
        this.placa = placa;
        this.ocupado = true;
        this.horaEntrada = LocalDateTime.now();
    }

    public void liberar() {
        this.placa = null;
        this.ocupado = false;
        this.horaEntrada = null;
    }

    public int getNumero() { return numero; }
    public boolean isOcupado() { return ocupado; }
    public String getPlaca() { return placa; }
    public LocalDateTime getHoraEntrada() { return horaEntrada; }

    /**
     * Devuelve el código del espacio en formato E-01, E-02, ..., E-20
     */
    public String getCodigo() {
        return String.format("E-%02d", numero);
    }
}