package com.example.rrhh_android_app.api;

import com.example.rrhh_android_app.model.*;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // Auth
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    // Presencia
    @POST("/api/presencia/entrada")
    Call<Void> ficharEntrada(@Header("Authorization") String token, @Body LocationRequest location);

    @POST("/api/presencia/salida")
    Call<Void> ficharSalida(@Header("Authorization") String token);

    @GET("/api/presencia/estado")
    Call<EstadoResponse> getEstado(@Header("Authorization") String token);

    @GET("/api/presencia/mis-registros")
    Call<List<EstadoResponse>> getMisRegistros(
            @Header("Authorization") String token,
            @Query("desde") String desde,
            @Query("hasta") String hasta);

    @GET("/api/presencia/resumen-mensual")
    Call<ResumenMensualResponse> getResumenMensual(
            @Header("Authorization") String token,
            @Query("mes") String mes);

    // Horario
    @GET("/api/trabajador/horario")
    Call<HorarioResponse> getMiHorario(@Header("Authorization") String token);

    // Incidencias
    @POST("/api/incidencias")
    Call<IncidenciaResponse> crearIncidencia(
            @Header("Authorization") String token,
            @Body IncidenciaRequest incidencia);

    @GET("/api/incidencias")
    Call<List<IncidenciaResponse>> getMisIncidencias(@Header("Authorization") String token);

    // Cambio de contraseña
    @POST("/api/password/solicitar")
    Call<Void> solicitarCambioPassword(@Header("Authorization") String token);

    // Admin - empleados
    @GET("/api/trabajadores")
    Call<List<EmpleadoResponse>> getEmpleados(@Header("Authorization") String token);
}