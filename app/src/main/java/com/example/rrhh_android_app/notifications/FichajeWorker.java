package com.example.rrhh_android_app.notifications;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.rrhh_android_app.api.RetrofitClient;
import com.example.rrhh_android_app.model.EstadoResponse;
import com.example.rrhh_android_app.model.HorarioResponse;
import com.example.rrhh_android_app.model.FranjaHoraria;

import java.util.Calendar;
import java.util.List;

import retrofit2.Response;

public class FichajeWorker extends Worker {

    public FichajeWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences pref = context.getSharedPreferences("RRHH_PREFS", Context.MODE_PRIVATE);
        String token = pref.getString("token", null);

        if (token == null) return Result.success();

        String bearerToken = "Bearer " + token;

        try {
            Response<EstadoResponse> estadoResp = RetrofitClient.getApiService()
                    .getEstado(bearerToken).execute();

            Response<HorarioResponse> horarioResp = RetrofitClient.getApiService()
                    .getMiHorario(bearerToken).execute();

            if (!horarioResp.isSuccessful() || horarioResp.body() == null) return Result.success();

            List<FranjaHoraria> franjas = horarioResp.body().getFranjas();
            if (franjas == null || franjas.isEmpty()) return Result.success();

            Calendar cal = Calendar.getInstance();
            // Calendar: SUNDAY=1, MONDAY=2... convertir a ISO (Lunes=1, Domingo=7)
            int diaCal = cal.get(Calendar.DAY_OF_WEEK);
            int diaSemana = (diaCal == Calendar.SUNDAY) ? 7 : diaCal - 1;

            int horaActualMin = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

            boolean fichado = estadoResp.isSuccessful()
                    && estadoResp.body() != null
                    && estadoResp.body().isFichado();

            for (FranjaHoraria franja : franjas) {
                if (franja.getIdDia() != diaSemana) continue;

                int minEntrada = parsearMinutos(franja.getHoraEntrada());
                int minSalida = parsearMinutos(franja.getHoraSalida());

                // 15 min después de entrada y antes de 60 min, sin fichar
                if (horaActualMin > minEntrada + 15 &&
                        horaActualMin < minEntrada + 60 && !fichado) {
                    NotificationHelper.enviarNotificacion(context, 1,
                            "⚠️ No has fichado la entrada",
                            "Tu hora de entrada era las " + franja.getHoraEntrada());
                }

                // 15 min después de salida y antes de 60 min, aún fichado
                if (horaActualMin > minSalida + 15 &&
                        horaActualMin < minSalida + 60 && fichado) {
                    NotificationHelper.enviarNotificacion(context, 2,
                            "⚠️ No has fichado la salida",
                            "Tu hora de salida era las " + franja.getHoraSalida());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.success();
    }

    // Convierte "HH:mm" a minutos totales desde medianoche
    private int parsearMinutos(String horaStr) {
        try {
            String[] partes = horaStr.split(":");
            return Integer.parseInt(partes[0]) * 60 + Integer.parseInt(partes[1]);
        } catch (Exception e) {
            return 0;
        }
    }
}