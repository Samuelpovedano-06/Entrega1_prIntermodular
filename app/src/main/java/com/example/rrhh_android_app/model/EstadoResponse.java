package com.example.rrhh_android_app.model;

import com.google.gson.annotations.SerializedName;

public class EstadoResponse {
    @SerializedName("hora_entrada")
    private String horaEntrada;

    @SerializedName("hora_salida")
    private String horaSalida;

    // Está fichado si tiene hora_entrada y NO tiene hora_salida
    public boolean isFichado() {
        return horaEntrada != null && horaSalida == null;
    }
}