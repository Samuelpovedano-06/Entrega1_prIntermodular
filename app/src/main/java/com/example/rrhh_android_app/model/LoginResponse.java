package com.example.rrhh_android_app.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("access_token")
    private String token;
    private String nombre;
    private String rol;

    public String getToken() { return token; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }
}