package com.example.rrhh_android_app.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.rrhh_android_app.api.RetrofitClient;
import com.example.rrhh_android_app.model.EstadoResponse;
import com.example.rrhh_android_app.model.FranjaHoraria;
import com.example.rrhh_android_app.model.HorarioResponse;

import java.util.Calendar;
import java.util.List;

import retrofit2.Response;

public class FichajeWorker extends Worker {

    private static final String TAG = "FichajeWorker";

    public FichajeWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Worker ejecutado: " + new java.util.Date());

        Context context = getApplicationContext();
        SharedPreferences pref = context.getSharedPreferences("RRHH_PREFS", Context.MODE_PRIVATE);
        String token = pref.getString("token", null);

        if (token == null) {
            Log.d(TAG, "Sin token, saliendo.");
            return Result.success();
        }

        String bearerToken = "Bearer " + token;

        try {
            Response<EstadoResponse> estadoResp = RetrofitClient.getApiService()
                    .getEstado(bearerToken).execute();

            Response<HorarioResponse> horarioResp = RetrofitClient.getApiService()
                    .getMiHorario(bearerToken).execute();

            if (!horarioResp.isSuccessful() || horarioResp.body() == null) {
                Log.d(TAG, "Error obteniendo horario: " + horarioResp.code());
                return Result.success();
            }

            List<FranjaHoraria> franjas = horarioResp.body().getFranjas();
            if (franjas == null || franjas.isEmpty()) {
                Log.d(TAG, "Sin franjas horarias.");
                return Result.success();
            }

            Calendar cal = Calendar.getInstance();
            int diaCal = cal.get(Calendar.DAY_OF_WEEK);
            int diaSemana = (diaCal == Calendar.SUNDAY) ? 7 : diaCal - 1;
            int horaActualMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

            boolean fichado = estadoResp.isSuccessful()
                    && estadoResp.body() != null
                    && estadoResp.body().isFichado();

            Log.d(TAG, "Dia: " + diaSemana + " | HoraMin: " + horaActualMin + " | Fichado: " + fichado);

            for (FranjaHoraria franja : franjas) {
                if (franja.getIdDia() != diaSemana) continue;

                int minEntrada = parsearMinutos(franja.getHoraEntrada());
                int minSalida = parsearMinutos(franja.getHoraSalida());
                String horaEntradaStr = formatearHora(franja.getHoraEntrada());
                String horaSalidaStr = formatearHora(franja.getHoraSalida());

                Log.d(TAG, "Franja activa - entrada: " + minEntrada + " salida: " + minSalida);

                boolean dentroVentanaEntrada = horaActualMin >= minEntrada + 15
                        && horaActualMin <= minEntrada + 180;
                boolean dentroVentanaSalida = horaActualMin >= minSalida
                        && horaActualMin <= minSalida + 180;

                if (dentroVentanaEntrada && !fichado) {
                    Log.d(TAG, "Activando alarma recordatorio ENTRADA");
                    FichajeAlarmManager.activarRecordatorioEntrada(context, horaEntradaStr);
                } else {
                    FichajeAlarmManager.cancelarRecordatorioEntrada(context);
                }

                if (dentroVentanaSalida && fichado) {
                    Log.d(TAG, "Activando alarma recordatorio SALIDA");
                    FichajeAlarmManager.activarRecordatorioSalida(context, horaSalidaStr);
                } else {
                    FichajeAlarmManager.cancelarRecordatorioSalida(context);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error en worker: " + e.getMessage(), e);
        }

        return Result.success();
    }

    private int parsearMinutos(String horaStr) {
        try {
            if (horaStr == null) return 0;
            String[] partes = horaStr.split(":");
            return Integer.parseInt(partes[0]) * 60 + Integer.parseInt(partes[1]);
        } catch (Exception e) {
            Log.e(TAG, "Error parseando hora: " + horaStr);
            return 0;
        }
    }

    private String formatearHora(String horaStr) {
        if (horaStr == null) return "";
        String[] partes = horaStr.split(":");
        if (partes.length >= 2) return partes[0] + ":" + partes[1];
        return horaStr;
    }
}