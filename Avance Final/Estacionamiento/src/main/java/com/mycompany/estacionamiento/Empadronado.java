package com.mycompany.estacionamiento;

public class Empadronado {
    private String placa;
    private String nombre;

    public Empadronado(String placa, String nombre) {
        this.placa = placa;
        this.nombre = nombre;
    }

    public String getPlaca() {
        return placa;
    }

    public String getNombre() {
        return nombre;
    }
}