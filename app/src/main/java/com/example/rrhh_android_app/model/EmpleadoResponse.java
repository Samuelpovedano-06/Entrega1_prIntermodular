package com.example.rrhh_android_app.model;

import com.google.gson.annotations.SerializedName;

public class EmpleadoResponse {
    @SerializedName("id_trabajador")
    private int idTrabajador;

    private String nif;
    private String nombre;
    private String apellidos;
    private String email;

    public int getIdTrabajador() { return idTrabajador; }
    public String getNif() { return nif; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getEmail() { return email; }
}