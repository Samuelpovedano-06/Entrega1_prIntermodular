package com.example.rrhh_android_app.model;

import com.google.gson.annotations.SerializedName;

public class ResumenMensualResponse {
    private String mes;

    @SerializedName("horas_trabajadas")
    private double horasTrabajadas;

    @SerializedName("horas_teoricas")
    private double horasTeoicas;

    @SerializedName("horas_extra")
    private double horasExtra;

    private int registros;

    public String getMes() { return mes; }
    public double getHorasTrabajadas() { return horasTrabajadas; }
    public double getHorasTeoicas() { return horasTeoicas; }
    public double getHorasExtra() { return horasExtra; }
    public int getRegistros() { return registros; }
}