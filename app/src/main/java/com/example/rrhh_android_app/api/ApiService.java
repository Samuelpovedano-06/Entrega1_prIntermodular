package com.example.rrhh_android_app.api;

import com.example.rrhh_android_app.model.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/api/presencia/entrada")
    Call<Void> ficharEntrada(@Header("Authorization") String token, @Body LocationRequest location);

    @POST("/api/presencia/salida")
    Call<Void> ficharSalida(@Header("Authorization") String token);

    @GET("/api/presencia/estado")
    Call<EstadoResponse> getEstado(@Header("Authorization") String token);
}