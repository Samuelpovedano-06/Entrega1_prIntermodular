package com.example.rrhh_android_app.model;

import com.google.gson.annotations.SerializedName;

public class FranjaHoraria {
    @SerializedName("id_dia")
    private int idDia;

    private String dia;

    @SerializedName("hora_entrada")
    private String horaEntrada;

    @SerializedName("hora_salida")
    private String horaSalida;

    public int getIdDia() { return idDia; }
    public String getDia() { return dia; }
    public String getHoraEntrada() { return horaEntrada; }
    public String getHoraSalida() { return horaSalida; }
}