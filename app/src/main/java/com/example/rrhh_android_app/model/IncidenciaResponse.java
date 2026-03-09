package com.example.rrhh_android_app.model;

import com.google.gson.annotations.SerializedName;

public class IncidenciaResponse {
    @SerializedName("id_incidencia")
    private int idIncidencia;

    @SerializedName("fecha_hora")
    private String fechaHora;

    private String descripcion;

    @SerializedName("id_trabajador")
    private int idTrabajador;

    public int getIdIncidencia() { return idIncidencia; }
    public String getFechaHora() { return fechaHora; }
    public String getDescripcion() { return descripcion; }
    public int getIdTrabajador() { return idTrabajador; }
}