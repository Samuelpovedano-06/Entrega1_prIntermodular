package com.example.rrhh_android_app.model;

public class LoginRequest {
    private String nif;
    private String password;

    public LoginRequest(String nif, String password) {
        this.nif = nif;
        this.password = password;
    }
}